package org.apache.hyracks.control.cc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.hyracks.api.job.JobStatus;
import org.apache.hyracks.control.cc.job.IJobManager;
import org.apache.hyracks.control.cc.job.JobRun;

import io.prometheus.client.Collector;
import io.prometheus.client.Counter;
import io.prometheus.client.GaugeMetricFamily;

public class JobsExporter extends Collector {
    private final IJobManager jobManager;
    private HashMap<String, Double> jobsCount;
    private HashSet<Long> jobsTerminatedIds;

    private static final Counter jobsSecondsCounter = Counter.build().name("asterix_job_total_milliseconds")
            .help("total milliseconds taken by jobs").labelNames("status").register();

    private static final Counter jobsCountCounter = Counter.build().name("asterix_job_total_count")
            .help("current total count of jobs").labelNames("status").register();

    public JobsExporter(IJobManager jobManager) {
        this.jobManager = jobManager;
        this.jobsCount = new HashMap<>();
        this.jobsTerminatedIds = new HashSet<>();

        // initialize counter to 0
        for (JobStatus status : JobStatus.values()) {
            if (status.equals(JobStatus.FAILURE) || status.equals(JobStatus.FAILURE_BEFORE_EXECUTION)
                    || status.equals(JobStatus.TERMINATED)) {
                jobsSecondsCounter.labels(status.toString()).inc(0);
            }
            jobsCountCounter.labels(status.toString()).inc(0);
        }
        resetJobCount();
    }

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();

        HashSet<Long> tempTerminatedIds = new HashSet<Long>();
        for (JobRun job : jobManager.getArchivedJobs()) {
            long jobId = job.getJobId().getId();
            if (!jobsTerminatedIds.contains(jobId)) {
                String status = job.getStatus().toString();
                jobsSecondsCounter.labels(status).inc(job.getEndTime() - job.getStartTime());
                jobsCountCounter.labels(status).inc();
            }
            tempTerminatedIds.add(jobId);
        }
        jobsTerminatedIds = tempTerminatedIds;

        GaugeMetricFamily jobsCountGauge =
                new GaugeMetricFamily("asterix_job_count", "current count of jobs", Arrays.asList("status"));

        resetJobCount();
        updateJobCount(jobManager.getRunningJobs());
        updateJobCount(jobManager.getPendingJobs());
        for (String jobElement : jobsCount.keySet()) {
            jobsCountGauge.addMetric(Arrays.asList(jobElement), jobsCount.get(jobElement));
        }

        mfs.add(jobsCountGauge);
        return mfs;
    }

    private void updateJobCount(Collection<JobRun> jobs) {
        for (JobRun job : jobs) {
            String jobStatus = job.getStatus().toString();
            jobsCount.put(jobStatus, jobsCount.get(jobStatus) + 1);
        }
    }

    private void resetJobCount() {
        for (JobStatus status : JobStatus.values()) {
            if (status.equals(JobStatus.RUNNING) || (status.equals(JobStatus.PENDING))) {
                this.jobsCount.put(status.toString(), 0.0);
            }
        }
    }
}
