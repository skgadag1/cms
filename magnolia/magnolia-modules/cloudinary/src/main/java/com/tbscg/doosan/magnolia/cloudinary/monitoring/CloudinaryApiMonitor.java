package com.tbscg.doosan.magnolia.cloudinary.monitoring;

import com.tbscg.doosan.magnolia.dxp.core.util.TracingUtil;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit;

import javax.inject.Singleton;

@Slf4j
@Singleton
public class CloudinaryApiMonitor {
    private final CloudWatchService cloudWatchService;

    public CloudinaryApiMonitor() {
        this.cloudWatchService = CloudWatchService.builder()
                .withNamespace("BOBCAT/CLOUDINARY_API")
                .withMetricName("ApiResponse")
                .withDimensionName("API")
                .withDimensionValue("ResponseTime")
                .withUnit(StandardUnit.MILLISECONDS)
                .build();
    }

    public void logExecutionTime(long executingTime) {
        log.info("Cloudinary API execution time: {} ms", executingTime);
        TracingUtil.callWithTracing(traceID -> cloudWatchService.putMetricData((double) executingTime));
    }
}
