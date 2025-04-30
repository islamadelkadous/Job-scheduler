package com.vrtx.scheduler_service.service;

import com.vrtx.scheduler_service.exceptions.BusinessException;
import com.vrtx.scheduler_service.model.dto.JobRequest;
import com.vrtx.scheduler_service.model.entity.JobEntity;
import com.vrtx.scheduler_service.model.enums.JobScheduleTypeEnum;
import com.vrtx.scheduler_service.model.enums.JobStatusEnum;
import com.vrtx.scheduler_service.repository.JobRepositoryFacade;
import com.vrtx.scheduler_service.utils.CronValidator;
import com.vrtx.scheduler_service.utils.EnumValidator;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class JobService {

    private final SchedulerService schedulerService;
    private final JobRepositoryFacade jobRepositoryFacade;

    private final ModelMapper modelMapper;

    @Autowired
    public JobService(SchedulerService schedulerService, JobRepositoryFacade jobRepositoryFacade, ModelMapper modelMapper) {
        this.schedulerService = schedulerService;
        this.jobRepositoryFacade = jobRepositoryFacade;
        this.modelMapper = modelMapper;
    }

    public List<JobEntity> getJobs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return jobRepositoryFacade.getJobs(pageable).getContent();
    }

    public JobEntity getJobById(UUID id) {
        return jobRepositoryFacade.getJobById(id);
    }

    public JobEntity createJob(JobRequest jobRequest) {
        validateCreateJobRequest(jobRequest);
        JobEntity jobEntity = modelMapper.map(jobRequest, JobEntity.class);
        jobEntity.setVersion(1);
        validateIfJobDuplicate(jobEntity);
        JobEntity response =  jobRepositoryFacade.createJob(jobEntity);
        schedulerService.scheduleNewJob(response);
        return response;
    }

    public JobEntity updateJob(UUID id, JobRequest jobRequest) {
        JobEntity currentEntity = jobRepositoryFacade.getJobById(id);

        validateUpdateInsertedFields(currentEntity, jobRequest);

        validateIfJobDuplicate(currentEntity);

        JobEntity response = jobRepositoryFacade.updateJob(currentEntity);
        schedulerService.scheduleNewJob(response);
        return response;
    }

    private void validateUpdateInsertedFields(JobEntity currentEntity, JobRequest request) {
        if (Objects.nonNull(request.getName())) {
            if (request.getName().isEmpty()) {
                throw new BusinessException(400, "BAD_REQUEST_VALIDATION_ERROR", "Name Could Not be Empty.");
            }
            currentEntity.setName(request.getName());
        }

        if (Objects.nonNull(request.getScheduleType())) {
            if (!EnumValidator.isValidEnumValue(JobScheduleTypeEnum.class, request.getScheduleType())) {
                throw new BusinessException(400, "BAD_REQUEST_VALIDATION_ERROR", "Invalid Schedule Type.");
            }
            currentEntity.setScheduleType(request.getScheduleType());
        }

        if (currentEntity.getScheduleType().equals(JobScheduleTypeEnum.CRON.toString())) {
            if (Objects.nonNull(currentEntity.getCronExpression()) || Objects.nonNull(request.getCronExpression())) {
                if (Objects.nonNull(request.getCronExpression())){
                    CronValidator.validateCronExpression(request.getCronExpression());
                    currentEntity.setCronExpression(request.getCronExpression());
                }
            } else {
                throw new BusinessException(400, "BAD_REQUEST_VALIDATION_ERROR", "Cron Expression Need to be Specified.");
            }
        }

        if (currentEntity.getScheduleType().equals(JobScheduleTypeEnum.FIXED.toString())) {
            if (Objects.nonNull(currentEntity.getFixedRateMs()) || Objects.nonNull(request.getFixedRateMs())) {
                if (Objects.nonNull(request.getFixedRateMs())) {
                    if (request.getFixedRateMs() <= 0) {
                        throw new BusinessException(400, "BAD_REQUEST_VALIDATION_ERROR", "Invalid Fixed Rate.");
                    }
                    currentEntity.setFixedRateMs(request.getFixedRateMs());
                }
            } else {
                throw new BusinessException(400, "BAD_REQUEST_VALIDATION_ERROR", "Fixed Rate Need to be Specified.");
            }
        }

        if (Objects.nonNull(request.getPayload())) {
            currentEntity.setPayload(request.getPayload());
        }

        if (Objects.nonNull(request.getRetryPolicy())) {
            currentEntity.setRetryPolicy(request.getRetryPolicy());
        }

        if (Objects.nonNull(request.getStatus())) {
            if (!EnumValidator.isValidEnumValue(JobStatusEnum.class, request.getStatus())) {
                throw new BusinessException(400, "BAD_REQUEST_VALIDATION_ERROR", "Invalid Status.");
            }
            currentEntity.setStatus(request.getStatus());
        }

        currentEntity.setVersion(currentEntity.getVersion() + 1);
    }

    private void validateCreateJobRequest(JobRequest request) {
        if (Objects.isNull(request.getName()) || request.getName().isEmpty()) {
            throw new BusinessException(400, "BAD_REQUEST_VALIDATION_ERROR", "Name Could Not be Null.");
        }
        if (Objects.isNull(request.getScheduleType())
                || !EnumValidator.isValidEnumValue(JobScheduleTypeEnum.class, request.getScheduleType())) {
            throw new BusinessException(400, "BAD_REQUEST_VALIDATION_ERROR", "Invalid Schedule Type.");
        }
        if (request.getScheduleType().equals(JobScheduleTypeEnum.CRON.toString())) {
            CronValidator.validateCronExpression(request.getCronExpression());
        }
        if (request.getScheduleType().equals(JobScheduleTypeEnum.FIXED.toString())
                && (Objects.isNull(request.getFixedRateMs()) || request.getFixedRateMs() <= 0)) {
            throw new BusinessException(400, "BAD_REQUEST_VALIDATION_ERROR", "Invalid Fixed Rate.");
        }
        if (Objects.isNull(request.getPayload())) {
            throw new BusinessException(400, "BAD_REQUEST_VALIDATION_ERROR", "Payload Could Not be Null.");
        }
        if (Objects.isNull(request.getRetryPolicy())) {
            throw new BusinessException(400, "BAD_REQUEST_VALIDATION_ERROR", "Retry Policy Could Not be Null.");
        }
        if (Objects.isNull(request.getStatus())
                || !EnumValidator.isValidEnumValue(JobStatusEnum.class, request.getStatus())) {
            throw new BusinessException(400, "BAD_REQUEST_VALIDATION_ERROR", "Invalid Status.");
        }
    }

    private void validateIfJobDuplicate(JobEntity requestJob) {
        Optional<JobEntity> duplicateJob = jobRepositoryFacade.findIfJobHasDuplicate(requestJob);
        if (duplicateJob.isPresent()) {
            throw new BusinessException(400, "BAD_REQUEST_VALIDATION_ERROR", "Job Already Exists");
        }
    }
}
