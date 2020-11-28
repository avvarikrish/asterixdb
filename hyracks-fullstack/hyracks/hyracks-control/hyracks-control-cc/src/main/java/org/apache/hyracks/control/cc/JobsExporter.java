package org.apache.hyracks.control.cc;

import io.prometheus.client.Collector;
import io.prometheus.client.Counter;
import io.prometheus.client.GaugeMetricFamily;
import org.apache.hyracks.api.job.JobStatus;
import org.apache.hyracks.control.cc.job.IJobManager;
import org.apache.hyracks.control.cc.job.JobRun;

import java.util.*;

public class JobsExporter extends Collector {
    private IJobManager jobManager;
    private HashMap<String, Double> jobsCount;
    private HashSet<Long> jobsRunIds;

    private static final Counter jobsSecondsCounter = Counter.build()
            .name("asterix_job_total_milliseconds").help("total milliseconds taken by jobs")
            .labelNames("status").register();

    public JobsExporter(IJobManager jobManager){
        this.jobManager = jobManager;
        this.jobsCount = new HashMap<>();
        this.jobsRunIds = new HashSet<Long>();

        for (JobStatus status : JobStatus.values()) {
            jobsSecondsCounter.labels(status.toString()).inc(0);
        }
        resetJobCount();
    }

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
        GaugeMetricFamily jobsCountGauge = new GaugeMetricFamily("asterix_job_count", "current count of jobs", Arrays.asList("status"));

        HashSet<Long> temp = new HashSet<Long>();
        for (JobRun job : jobManager.getArchivedJobs()) {
            long jobId = job.getJobId().getId();
            if (!jobsRunIds.contains(jobId)) {
                System.out.println("TIME TAKEN: " + (job.getEndTime()-job.getStartTime()));
                jobsSecondsCounter.labels(job.getStatus().toString()).inc(job.getEndTime()-job.getStartTime());
            }
            temp.add(jobId);
        }
        jobsRunIds = temp;

        resetJobCount();
        updateJobCount(jobManager.getRunningJobs());
        updateJobCount(jobManager.getPendingJobs());
        updateJobCount(jobManager.getArchivedJobs());
        for (Map.Entry jobElement : jobsCount.entrySet()) {
            jobsCountGauge.addMetric(Arrays.asList((String)jobElement.getKey()), (Double)jobElement.getValue());
        }

        mfs.add(jobsCountGauge);
        return mfs;
    }

    private void updateJobCount(Collection<JobRun> jobs) {
        for (JobRun job : jobs) {
            String jobStatus = job.getStatus().toString();
            if (jobsCount.containsKey(jobStatus)) {
                jobsCount.put(jobStatus, jobsCount.get(jobStatus) + 1);
            }
        }
    }

    private void resetJobCount() {
        for (JobStatus status : JobStatus.values()) {
            this.jobsCount.put(status.toString(), 0.0);
        }
    }
}
