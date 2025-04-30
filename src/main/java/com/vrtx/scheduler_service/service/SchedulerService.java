package com.vrtx.scheduler_service.service;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.vrtx.scheduler_service.exceptions.BusinessException;
import com.vrtx.scheduler_service.executers.SimpleJobExecutor;
import com.vrtx.scheduler_service.model.entity.JobEntity;
import com.vrtx.scheduler_service.model.enums.JobScheduleTypeEnum;
import com.vrtx.scheduler_service.model.enums.JobStatusEnum;
import com.vrtx.scheduler_service.repository.JobRepositoryFacade;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@EnableScheduling
@Slf4j
public class SchedulerService {

    private final JobRepositoryFacade jobRepositoryFacade;

    private final SimpleJobExecutor jobExecutor;

    @Value("${scheduler.missed-execution-strategy}")
    private String missedExecutionStrategy;

    private final CronParser cronParser;

    private static final String SKIP_MISSED_EXEC_STRATEGY = "skip";

    @Autowired
    public SchedulerService(JobRepositoryFacade jobRepositoryFacade, SimpleJobExecutor jobExecutor) {
        this.jobRepositoryFacade = jobRepositoryFacade;
        this.jobExecutor = jobExecutor;
        this.cronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
    }

    @PostConstruct
    @Transactional
    public void initializeJobs() {
        log.info("Initializing scheduler with existing jobs that are PENDING or RUNNING");
        List<JobEntity> jobs = jobRepositoryFacade
                .getJobsByStatusList(List.of(JobStatusEnum.PENDING.toString(), JobStatusEnum.RUNNING.toString()));
        for (JobEntity job : jobs) {
            if (job.getStatus().equals(JobStatusEnum.PENDING.toString()) || job.getStatus().equals(JobStatusEnum.RUNNING.toString())) {
                scheduleJob(job);
            }
        }
        log.info("Initialized {} jobs", jobs.size());
    }

    // Periodically check for jobs to execute
    @Scheduled(fixedRateString = "${scheduler.check-and-execute-rate}")
    public void checkAndExecuteJobs() {
        log.info("Start Checking and Execution");
        // Get executable Jobs (With Status PENDING or RUNNING)
        List<JobEntity> jobs = jobRepositoryFacade
                .getJobsByStatusList(List.of(JobStatusEnum.PENDING.toString(), JobStatusEnum.RUNNING.toString()));
        ZonedDateTime now = ZonedDateTime.now();

        for (JobEntity job : jobs) {

            ZonedDateTime nextRunAt = job.getNextRunAt();
            if (nextRunAt == null) {
                // If nextRunAt is not set, calculate it based on the schedule
                scheduleJob(job);
                continue;
            }

            // Check if it's time to execute the job
            if (nextRunAt.isBefore(now) || nextRunAt.isEqual(now)) {
                handleMissedExecutions(job, now);
                executeJob(job);
                log.info("Schedule Next Execution for the Job");
                scheduleJob(job);
            }
        }
    }

    // Schedule a job (set the nextRunAt timestamp)
    private void scheduleJob(JobEntity job) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime nextRunAt;

        if (job.getScheduleType().equals(JobScheduleTypeEnum.CRON.toString())) {
            ExecutionTime executionTime = ExecutionTime.forCron(cronParser.parse(job.getCronExpression()));
            Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(now);
            nextRunAt = nextExecution.orElseThrow(() -> new BusinessException(500, "INTERNAL_SERVER_ERROR", "Cannot compute next execution time for cron expression: " + job.getCronExpression()));
        } else if (job.getScheduleType().equals(JobScheduleTypeEnum.FIXED.toString())) {
            Long fixedRateMs = job.getFixedRateMs();
            if (fixedRateMs == null || fixedRateMs <= 0) {
                throw new BusinessException(500, "INTERNAL_SERVER_ERROR", "Fixed rate must be a positive number for job: " + job.getId());
            }
            // If lastRunAt exists, schedule from there; otherwise, start from now
            ZonedDateTime baseTime = job.getLastRunAt() != null ? job.getLastRunAt() : now;
            nextRunAt = baseTime.plus(fixedRateMs, ChronoUnit.MILLIS);
            if (nextRunAt.isBefore(now)) {
                nextRunAt = now.plus(fixedRateMs, ChronoUnit.MILLIS);
            }
        } else {
            throw new BusinessException(500, "INTERNAL_SERVER_ERROR", "Unsupported schedule type: " + job.getScheduleType());
        }

        job.setNextRunAt(nextRunAt);
        job.setStatus(JobStatusEnum.PENDING.toString());
        jobRepositoryFacade.updateJob(job);
        log.info("Scheduled job {} (ID: {}) to run at {}", job.getName(), job.getId(), nextRunAt);
    }

    // Handle missed executions (catch-up or skip)
    private void handleMissedExecutions(JobEntity job, ZonedDateTime now) {
        log.info("Start Handling Missed Executions");
        if (SKIP_MISSED_EXEC_STRATEGY.equalsIgnoreCase(missedExecutionStrategy)) {
            log.info("Handling Missed Executions is SKIP");
            return; // Skip missed executions
        }

        log.info("Handling Missed Executions is CATCH-UP");
        // Catch-up: Execute the job for each missed run
        ZonedDateTime nextRunAt = job.getNextRunAt();
        if (job.getScheduleType().equals(JobScheduleTypeEnum.CRON.toString())) {
            ExecutionTime executionTime = ExecutionTime.forCron(cronParser.parse(job.getCronExpression()));
            ZonedDateTime currentRunAt = nextRunAt;

            while (currentRunAt.isBefore(now) || currentRunAt.isEqual(now)) {
                log.info("Catching up missed execution for job {} (ID: {}) at {}",
                        job.getName(), job.getId(), currentRunAt);
                executeJob(job);
                Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(currentRunAt);
                currentRunAt = nextExecution.orElseThrow(() -> new BusinessException(500, "INTERNAL_SERVER_ERROR",
                        "Cannot compute next execution time for cron expression: " + job.getCronExpression()));
            }
            job.setNextRunAt(currentRunAt);
        } else if (job.getScheduleType().equals(JobScheduleTypeEnum.FIXED.toString())) {
            Long fixedRateMs = job.getFixedRateMs();
            ZonedDateTime currentRunAt = nextRunAt;

            while (currentRunAt.isBefore(now) || currentRunAt.isEqual(now)) {
                log.info("Catching up missed execution for job {} (ID: {}) at {}",
                        job.getName(), job.getId(), currentRunAt);
                executeJob(job);
                currentRunAt = currentRunAt.plus(fixedRateMs, ChronoUnit.MILLIS);
            }
            job.setNextRunAt(currentRunAt);
        }
    }

    // Execute the job and update timestamps
    private void executeJob(JobEntity job) {
        log.info("Start Executing Job [{}]", job);
        try {
            job.setStatus(JobStatusEnum.RUNNING.toString());
            job.setLastRunAt(ZonedDateTime.now());
            log.info("before setting job running");
            JobEntity response = jobRepositoryFacade.updateJob(job);
            log.info("Job after being set to running [{}]", response);
            log.info("after setting job running");

            jobExecutor.execute(job);

            job.setStatus(JobStatusEnum.PENDING.toString()); // Reset to PENDING after execution
            jobRepositoryFacade.updateJob(job);
            log.info("Successfully executed job {} (ID: {})", job.getName(), job.getId());
        } catch (Exception e) {
            log.error("Failed to execute job {} (ID: {}): {}", job.getName(), job.getId(), e.getMessage(), e);
            job.setStatus(JobStatusEnum.FAILED.toString());
            jobRepositoryFacade.updateJob(job);
        }
    }

    // Public method to schedule a new jobs
    @Transactional
    public void scheduleNewJob(JobEntity job) {
        scheduleJob(job);
    }
}
