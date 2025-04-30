package com.vrtx.scheduler_service.executers;

import com.vrtx.scheduler_service.model.entity.JobEntity;

public interface JobExecutor {
    void execute(JobEntity jobEntity);
}
