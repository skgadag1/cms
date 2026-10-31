package com.tbscg.doosan.magnolia.bobcat.commands.monitoring;

import software.amazon.awssdk.services.scheduler.SchedulerClient;
import software.amazon.awssdk.services.scheduler.model.*;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import com.tbscg.doosan.magnolia.dxp.core.util.TracingUtil;
import lombok.extern.slf4j.Slf4j;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class BackupMonitor {
    private static final String TARGET_ARN = "arn:aws:scheduler:::aws-sdk:cloudwatch:putMetricData";
    private static final String METRIC_NAME = "MagnoliaBackup";
    private static final String METRIC_NAMESPACE = "Custom/Bobcat/Monitoring";
    private final String groupNamePath;
    private final String roleArnPath;

    private final SchedulerClient client;
    private final SsmClient ssmClient;
    private String roleArn;
    private String groupName;
    private Optional<String> lastScheduleName = Optional.empty();

    public BackupMonitor(String groupNamePath, String roleArnPath) {
        this.groupNamePath = groupNamePath;
        this.roleArnPath = roleArnPath;
        ssmClient = SsmClient.create();
        client = SchedulerClient.create();
    }

    public boolean clearSchedule() {
        try {
            return lastScheduleName.map(scheduleName -> {
                DeleteScheduleRequest request = getDeleteRequest(lastScheduleName.get());
                TracingUtil.callWithTracing(traceID -> client.deleteSchedule(request));
                lastScheduleName = Optional.empty();
                return true;
            }).orElse(false);
        } catch (Exception e) {
            log.error("Failed to clear alarm", e);
            return false;
        }
    }

    private DeleteScheduleRequest getDeleteRequest(String lastScheduleName) {
        return DeleteScheduleRequest.builder()
                .name(lastScheduleName)
                .groupName(groupName)
                .build();
    }

    public boolean scheduleAlarm(String[] workspacesToSave) {
        try {
            groupName = getSsmParameter(groupNamePath);
            roleArn = getSsmParameter(roleArnPath);
            CreateScheduleRequest request = getScheduleRequest(workspacesToSave);
            TracingUtil.callWithTracing(traceID -> client.createSchedule(request));
            lastScheduleName = Optional.of(request.name());
            return true;
        } catch (Exception e) {
            log.error("Failed to schedule alarm", e);
            return false;
        }

    }

    private CreateScheduleRequest getScheduleRequest(String[] workspacesToSave) {
        return CreateScheduleRequest.builder()
                .name(createScheduleName(workspacesToSave))
                .groupName(groupName)
                .scheduleExpression(String.format("at(%s)", getNextHourDate()))
                .scheduleExpressionTimezone(TimeZone.getDefault().getID())
                .flexibleTimeWindow(createFlexibleTimeWindow())
                .target(createTarget())
                .build();
    }

    private String getNextHourDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR, 1);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return dateFormat.format(calendar.getTime());
    }

    private String createScheduleName(String[] workspacesToSave) {
        Random random = new Random();
        String name = random.nextInt(9999) + ".backup.alarm." + String.join(".", workspacesToSave);
        return name.substring(0, Math.min(name.length(), 63));
    }

    private FlexibleTimeWindow createFlexibleTimeWindow() {
        return FlexibleTimeWindow.builder().mode(FlexibleTimeWindowMode.OFF.toString())
                .build();
    }

    private Target createTarget() {
        return Target.builder()
                .arn(TARGET_ARN)
                .roleArn(roleArn)
                .input(String.format("{\"MetricData\":[{\"MetricName\":\"%s\",\"Value\": 1,\"Unit\":\"Count\"}],\"Namespace\":\"%s\"}", METRIC_NAME, METRIC_NAMESPACE))
                .build();
    }

    private String getSsmParameter(String path) {
        final GetParameterRequest getParameterRequest = GetParameterRequest.builder().name(path)
                .build();
        final GetParameterResponse parameter = TracingUtil.callWithTracing(traceID -> ssmClient.getParameter(getParameterRequest));

        log.info("Got parameter: {} = '{}'", path, parameter);
        return parameter.parameter().value();
    }
}
