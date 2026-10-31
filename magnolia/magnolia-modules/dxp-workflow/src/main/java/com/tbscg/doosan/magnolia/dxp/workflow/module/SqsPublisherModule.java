package com.tbscg.doosan.magnolia.dxp.workflow.module;

import com.amazon.sqs.javamessaging.AmazonSQSExtendedClient;
import com.amazon.sqs.javamessaging.ExtendedClientConfiguration;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;

/**
 * SQS publisher common configuration module class.
 *
 * @author apuigescobar
 */
public class SqsPublisherModule {
    private final SsmClient ssmClient;
    private final ExtendedClientConfiguration extClientConfig;
    private final S3Client s3Client;
    private final SqsClient sqsClient;

    private SqsClient sqsExtendedClient;

    private String sqsQueueParamName;
    private String sqsS3BucketParamName;
    private String queueUrl;
    private String s3BucketName;

    public SqsPublisherModule() {
        this.ssmClient = SsmClient.create();
        this.s3Client = S3Client.create();
        this.sqsClient = SqsClient.create();
        this.sqsExtendedClient = this.sqsClient;
        this.extClientConfig = new ExtendedClientConfiguration();
    }

    public String getSqsQueueParamName() {
        return sqsQueueParamName;
    }

    public void setSqsQueueParamName(String sqsQueueParamName) {
        this.sqsQueueParamName = sqsQueueParamName;
        this.configureQueueUrl();
    }

    public String getSqsS3BucketParamName() {
        return sqsS3BucketParamName;
    }

    public void setSqsS3BucketParamName(String sqsS3BucketParamName) {
        this.sqsS3BucketParamName = sqsS3BucketParamName;
        this.configureS3BucketName();
    }

    public String getQueueUrl() {
        return queueUrl;
    }

    public String getS3BucketName() {
        return s3BucketName;
    }

    public SqsClient getSqsClient() {
        return this.sqsExtendedClient;
    }

    private void configureQueueUrl() {
        final GetParameterRequest request = GetParameterRequest.builder().name(this.sqsQueueParamName)
                .build();
        final GetParameterResponse result = this.ssmClient.getParameter(request);
        this.queueUrl = result.parameter().value();
    }

    private void configureS3BucketName() {
        final GetParameterRequest request = GetParameterRequest.builder().name(this.sqsS3BucketParamName)
                .build();
        final GetParameterResponse result = this.ssmClient.getParameter(request);
        this.s3BucketName = result.parameter().value();
        this.extClientConfig.setLargePayloadSupportEnabled(this.s3Client, this.s3BucketName);

        //each time there is a configuration update we need to create
        // a new client due to how the constructor is implemented
        this.sqsExtendedClient = new AmazonSQSExtendedClient(this.sqsClient, this.extClientConfig);
    }
}
