package com.vrtx.scheduler_service.executers;

import com.vrtx.scheduler_service.exceptions.BusinessException;
import com.vrtx.scheduler_service.model.entity.JobEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SimpleJobExecutor implements JobExecutor {

    @Value("${executer.job-execution-time}")
    private long executionTime;

    @Override
    public void execute(JobEntity job) {
        log.info("Started Executing job: {} (ID: {}) with payload: {}", job.getName(), job.getId(), job.getPayload());
        try {
            Thread.sleep(executionTime);
        } catch (Exception e) {
            log.info("Failed Executing job: {} (ID: {}) with payload: {}", job.getName(), job.getId(), job.getPayload());
            throw new BusinessException(500, "INTERNAL_SERVER_ERROR", "Job Execution Failed");
        }
        log.info("Finished Executing job: {} (ID: {}) with payload: {}", job.getName(), job.getId(), job.getPayload());
    }
}
