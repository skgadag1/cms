package com.tbscg.doosan.magnolia.cloudinary.monitoring;

import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.*;
import lombok.Builder;

@Builder(setterPrefix = "with")
public class CloudWatchService {
    private final String namespace;
    private final String metricName;
    private final String dimensionName;
    private final String dimensionValue;
    private final StandardUnit unit;
    private final CloudWatchClient cloudWatchClient = createCloudWatchClient();;

    private CloudWatchClient createCloudWatchClient() {
        return CloudWatchClient.create();
    }

    private Dimension createDimension(String name, String value) {
        return Dimension.builder().name(name).value(value)
                .build();
    }

    private MetricDatum createMetricDatum(String metricName, double value, StandardUnit unit, Dimension dimension) {
        return MetricDatum.builder()
                .metricName(metricName)
                .value(value)
                .unit(unit)
                .dimensions(dimension)
                .timestamp(new java.util.Date().toInstant())
                .storageResolution(1)
                .build();
    }

    private PutMetricDataRequest createRequest(String nameSpace, MetricDatum metricDatum) {
        return PutMetricDataRequest.builder()
                .namespace(nameSpace)
                .metricData(metricDatum)
                .build();
    }

    public PutMetricDataResponse putMetricData(Double value) {
        PutMetricDataRequest request = createRequest(
                namespace,
                createMetricDatum(metricName, value, unit, createDimension(dimensionName, dimensionValue)));
        return cloudWatchClient.putMetricData(request);
    }
}
