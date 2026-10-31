package com.tbscg.doosan.magnolia.forms.configuration;

import com.tbscg.doosan.magnolia.dxp.core.util.TracingUtil;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;


@Data
public class FormsConfigurationModule implements ModuleLifecycle {
    private static SsmClient ssmClient = SsmClient.create();
    private DownloadFormSummaryConfig downloadFormSummaryConfig;
    private S3Presigner s3Presigner = S3Presigner.create();
    private String contractCodesApiBaseUrl;

    @Override
    public void start(ModuleLifecycleContext moduleLifecycleContext) {

    }

    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
    }

    @Data
    @Slf4j
    public static class DownloadFormSummaryConfig {
        private String bucketName;
        private String pathToDynamoDBTableName;
        private String dynamoDBTableName;

        public void setPathToDynamoDBTableName(String pathToDynamoDBTableName) {
            this.pathToDynamoDBTableName = pathToDynamoDBTableName;
            this.dynamoDBTableName = getSsmParameter(pathToDynamoDBTableName);
        }

        private String getSsmParameter(String pathToDynamoDBTableName) {
            final GetParameterRequest getParameterRequest = GetParameterRequest.builder().name(pathToDynamoDBTableName)
                    .build();
            final GetParameterResponse parameter = TracingUtil.callWithTracing(traceID -> ssmClient.getParameter(getParameterRequest));

            log.info("Got parameter: {} = '{}'", pathToDynamoDBTableName, parameter);
            return parameter.parameter().value();
        }
    }

}
