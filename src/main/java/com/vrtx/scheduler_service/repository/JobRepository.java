package com.vrtx.scheduler_service.repository;

import com.vrtx.scheduler_service.model.entity.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<JobEntity, UUID> {

    @Query("SELECT j FROM JobEntity j WHERE j.id <> :id AND j.scheduleType = :scheduleType " +
            "AND ((j.scheduleType = 'CRON' AND j.cronExpression = :cronExpression) OR " +
            "(j.scheduleType = 'FIXED' AND j.fixedRateMs = :fixedRateMs))")
    Optional<JobEntity> findByNameAndSchedule(
            @Param("id") UUID id,
            @Param("scheduleType") String scheduleType,
            @Param("cronExpression") String cronExpression,
            @Param("fixedRateMs") Long fixedRateMs);

    @Query("SELECT j FROM JobEntity j WHERE j.status IN :statuses")
    List<JobEntity> findByStatusIn(@Param("statuses") List<String> statuses);
}
