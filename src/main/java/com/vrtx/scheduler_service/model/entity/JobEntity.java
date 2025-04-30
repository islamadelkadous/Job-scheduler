package com.vrtx.scheduler_service.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "jobs")
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    @JsonProperty("id")
    private UUID id;

    @Column(name = "name", nullable = false, length = 255)
    @JsonProperty("name")
    private String name;

    @Column(name = "schedule_type", nullable = false, length = 50)
    @JsonProperty("schedule_type")
    private String scheduleType;

    @Column(name = "cron_expression", length = 100)
    @JsonProperty("cron_expression")
    private String cronExpression;

    @Column(name = "fixed_rate_ms")
    @JsonProperty("fixed_rate_ms")
    private Long fixedRateMs;

    @Type(JsonType.class)
    @Column(name = "payload", columnDefinition = "jsonb")
    @JsonProperty("payload")
    private Map<String, Object> payload;

    @Type(JsonType.class)
    @Column(name = "retry_policy", columnDefinition = "jsonb")
    @JsonProperty("retry_policy")
    private Map<String, Object> retryPolicy;

    @Column(name = "status", nullable = false, length = 20)
    @JsonProperty("status")
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonProperty("created_at")
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @JsonProperty("updated_at")
    private ZonedDateTime updatedAt;

    @Column(name = "last_run_at")
    @JsonProperty("last_run_at")
    private ZonedDateTime lastRunAt;

    @Column(name = "next_run_at")
    @JsonProperty("next_run_at")
    private ZonedDateTime nextRunAt;

    @Column(name = "version", nullable = false)
    @JsonProperty("version")
    private Integer version;
}
