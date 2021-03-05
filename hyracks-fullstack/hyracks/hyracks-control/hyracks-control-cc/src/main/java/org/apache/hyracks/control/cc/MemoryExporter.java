package org.apache.hyracks.control.cc;

import java.util.ArrayList;
import java.util.List;

import org.apache.hyracks.control.cc.scheduler.IResourceManager;

import io.prometheus.client.Collector;
import io.prometheus.client.Gauge;

public class MemoryExporter extends Collector {
    private IResourceManager resourceManager;
    private static final Gauge clusterMemorySizeBytes =
            Gauge.build().name("asterix_cluster_memory_bytes").help("cluster aggregate bytes available").register();

    public MemoryExporter(IResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();

        clusterMemorySizeBytes.set(resourceManager.getMaximumCapacity().getAggregatedMemoryByteSize()
                - resourceManager.getCurrentCapacity().getAggregatedMemoryByteSize());

        return mfs;
    }
}
