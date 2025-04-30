package com.vrtx.scheduler_service.utils;

import com.vrtx.scheduler_service.model.enums.JobStatusEnum;

import java.util.List;

public class Constants {
    public static final List<String> SCHEDULABLE_JOB_STATUS = List.of(
            JobStatusEnum.PENDING.toString(),
            JobStatusEnum.RUNNING.toString()
    );

    public static final String SKIP_MISSED_EXEC_STRATEGY = "skip";

}
