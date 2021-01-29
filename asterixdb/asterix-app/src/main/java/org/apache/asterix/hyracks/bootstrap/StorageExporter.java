package org.apache.asterix.hyracks.bootstrap;

import static com.fasterxml.jackson.databind.MapperFeature.SORT_PROPERTIES_ALPHABETICALLY;
import static com.fasterxml.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.asterix.common.storage.ResourceStorageStats;
import org.apache.asterix.transaction.management.resource.PersistentLocalResourceRepository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;
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
        GaugeMetricFamily storageSizeGauge = new GaugeMetricFamily("asterix_storage_size", "current size of storage",
                Arrays.asList("index", "dataset", "partition"));
        
        updateStorageStats();
        for (int i = 0; i < storageStats.size(); i++) {
            JsonNode node = storageStats.get(i);
            Double nodeSize = node.get("totalSize").asDouble();
            storageSizeGauge.addMetric(Arrays.asList(node.get("index").toString(), node.get("dataset").toString(),
                    node.get("partition").toString()), nodeSize);
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
