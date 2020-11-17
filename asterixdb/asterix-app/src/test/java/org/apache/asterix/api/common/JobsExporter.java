package org.apache.asterix.api.common;

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;
import org.apache.hyracks.control.cc.job.IJobManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JobsExporter extends Collector {
    private IJobManager jobManager;

    public JobsExporter(IJobManager jobManager){
        this.jobManager = jobManager;
    }

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
        GaugeMetricFamily jobsCount = new GaugeMetricFamily("asterix_job_count", "current count of jobs", Arrays.asList("labelname"));
        jobsCount.addMetric(Arrays.asList("running_jobs"), jobManager.getRunningJobs().size());
        jobsCount.addMetric(Arrays.asList("archived_jobs"), jobManager.getArchivedJobs().size());
        jobsCount.addMetric(Arrays.asList("pending_jobs"), jobManager.getPendingJobs().size());
        mfs.add(jobsCount);
        return mfs;
    }
}
