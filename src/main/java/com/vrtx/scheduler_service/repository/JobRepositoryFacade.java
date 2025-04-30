package com.vrtx.scheduler_service.repository;

import com.vrtx.scheduler_service.exceptions.BusinessException;
import com.vrtx.scheduler_service.model.entity.JobEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
public class JobRepositoryFacade {

    private final JobRepository jobRepository;

    @Autowired
    public JobRepositoryFacade(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public Page<JobEntity> getJobs(Pageable pageable) {
        try {
            return jobRepository.findAll(pageable);
        } catch (Exception e) {
            throw new BusinessException(
                    500,
                    "DATABASE_CONNECTION_ERROR",
                    "Couldn't retrieve Jobs"
            );
        }
    }

    public JobEntity getJobById(UUID id) {
        try {
            return jobRepository.findById(id)
                    .orElseThrow(() -> new BusinessException(404, "NOT_FOUND_ERROR", "Job not found with ID: " + id));
        } catch (Exception e) {
            throw new BusinessException(
                    500,
                    "DATABASE_CONNECTION_ERROR",
                    "Couldn't retrieve Job having Id: "+ id
            );
        }
    }

    public JobEntity createJob(JobEntity jobEntity) {
        try {
            return jobRepository.save(jobEntity);
        } catch (Exception e) {
            throw new BusinessException(
                    500,
                    "DATABASE_CONNECTION_ERROR",
                    "Couldn't create Job"
            );
        }
    }

    public JobEntity updateJob(JobEntity jobEntity) {
        try {
            return jobRepository.save(jobEntity);
        } catch (Exception e) {
            throw new BusinessException(
                    500,
                    "DATABASE_CONNECTION_ERROR",
                    "Couldn't update Job having Id: "+ jobEntity.getId()
            );
        }
    }

    public Optional<JobEntity> findIfJobHasDuplicate(JobEntity jobEntity) {
        try {
            UUID id = Objects.nonNull(jobEntity.getId()) ? jobEntity.getId() : UUID.randomUUID();
            return jobRepository.findByNameAndSchedule(id, jobEntity.getScheduleType(),
                            jobEntity.getCronExpression(), jobEntity.getFixedRateMs());
        } catch (Exception e) {
            throw new BusinessException(
                    500,
                    "DATABASE_CONNECTION_ERROR",
                    "Couldn't retrieve Job From Database."
            );
        }
    }

    public List<JobEntity> getJobsByStatusList(List<String> statusEnumList) {
        try {
            return jobRepository.findByStatusIn(statusEnumList);
        } catch (Exception e) {
            throw new BusinessException(
                    500,
                    "DATABASE_CONNECTION_ERROR",
                    "Couldn't retrieve Jobs"
            );
        }
    }
}
