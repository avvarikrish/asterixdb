package org.apache.asterix.hyracks.bootstrap;

import static com.fasterxml.jackson.databind.MapperFeature.SORT_PROPERTIES_ALPHABETICALLY;
import static com.fasterxml.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.File;

import org.apache.asterix.common.storage.ResourceStorageStats;
import org.apache.asterix.common.storage.ResourceReference;
import org.apache.asterix.transaction.management.resource.PersistentLocalResourceRepository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;
import org.apache.commons.lang3.StringUtils;
import org.apache.hyracks.api.exceptions.HyracksDataException;

public class StorageExporter extends Collector {
    private ArrayNode storageStats;
    private PersistentLocalResourceRepository storageRepository;
    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
        OBJECT_MAPPER.configure(SORT_PROPERTIES_ALPHABETICALLY, true);
        OBJECT_MAPPER.configure(ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    public StorageExporter(PersistentLocalResourceRepository storageRepository) {
        this.storageStats = OBJECT_MAPPER.createArrayNode();
        this.storageRepository = storageRepository;

    }

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
        GaugeMetricFamily storageSizeGauge = new GaugeMetricFamily("asterix_storage_bytes", "current size of storage",
                Arrays.asList("partition", "dataverse", "dataset", "index"));

        updateStorageStats();
        for (int i = 0; i < storageStats.size(); i++) {
            JsonNode node = storageStats.get(i);
            String index = node.get("index").toString();
            String dataset = node.get("dataset").toString();
            String partition = node.get("partition").toString();
            Double nodeSize = node.get("totalSize").asDouble();
            String[] tokens = StringUtils.split(node.get("path").toString(), File.separatorChar);
            System.out.println("PATH: " + node.get("path").toString());
//            System.out.println("DATAVERSE: " + ResourceReference.of(node.get("path").toString()).getDataverse());
            storageSizeGauge.addMetric(Arrays.asList(partition, tokens[2], dataset, index), nodeSize);
        }
        mfs.add(storageSizeGauge);
        return mfs;
    }

    private void updateStorageStats() {
        ArrayNode storageStats = OBJECT_MAPPER.createArrayNode();

        try {
            storageRepository.getStorageStats().stream().map(ResourceStorageStats::asJson).forEach(storageStats::add);
            this.storageStats = storageStats;
        } catch (HyracksDataException e) {
            e.printStackTrace();
        }
    }
}
