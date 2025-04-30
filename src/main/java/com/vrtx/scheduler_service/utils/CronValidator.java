package com.vrtx.scheduler_service.utils;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import com.vrtx.scheduler_service.exceptions.BusinessException;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

@Component
public class CronValidator {

    private static boolean isValidCronExpression(String cronExpression) {
        if (cronExpression == null || cronExpression.isEmpty()) {
            return false;
        }
        return CronExpression.isValidExpression(cronExpression.trim());
    }

    public static void validateCronExpression(String cronExpression) {
        if (!isValidCronExpression(cronExpression)) {
            throw new BusinessException(400, "BAD_REQUEST_VALIDATION_ERROR", "Invalid CRON Expression.");
        }
        try {
            CronParser cronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
            cronParser.parse(cronExpression);
        } catch (Exception e) {
            throw new BusinessException(400, "BAD_REQUEST_VALIDATION_ERROR", "Invalid CRON Expression.");
        }
    }
}
