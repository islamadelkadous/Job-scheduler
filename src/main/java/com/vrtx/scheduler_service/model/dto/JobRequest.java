package com.vrtx.scheduler_service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class JobRequest {
    @NotBlank
    @JsonProperty("name")
    private String name;

    @JsonProperty("schedule_type")
    private String scheduleType;

    @JsonProperty("cron_expression")
    private String cronExpression;

    @JsonProperty("fixed_rate_ms")
    private Long fixedRateMs;

    @JsonProperty("payload")
    private Map<String, Object> payload;

    @JsonProperty("retry_policy")
    private Map<String, Object> retryPolicy;

    @JsonProperty("status")
    private String status;
}
