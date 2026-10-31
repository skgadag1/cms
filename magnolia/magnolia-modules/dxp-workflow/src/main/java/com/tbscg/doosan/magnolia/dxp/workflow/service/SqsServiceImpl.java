package com.tbscg.doosan.magnolia.dxp.workflow.service;

import com.tbscg.doosan.magnolia.dxp.workflow.module.SqsPublisherModule;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import javax.inject.Inject;

@Slf4j
public class SqsServiceImpl implements SqsService {
    private final SqsClient sqs;
    private final String queueUrl;

    @Inject
    public SqsServiceImpl(SqsPublisherModule config) {
        this(config.getSqsClient(), config.getQueueUrl());
    }

    public SqsServiceImpl(SqsClient sqs, String queueUrl) {
        this.queueUrl = queueUrl;
        this.sqs = sqs;
    }

    @Override
    public String sendMessage(String message, String messageGroupId) {
        final SendMessageRequest messageRequest = createSendMessageRequest(message, messageGroupId);
        final SendMessageResponse result = this.sqs.sendMessage(messageRequest);
        return result.messageId();
    }

    @Override
    public String sendMessage(String message) {
        log.info("Sending message to SQS: {}", message);
        final SendMessageRequest messageRequest = createSendMessageRequest(message);
        final SendMessageResponse result = this.sqs.sendMessage(messageRequest);
        return result.messageId();
    }

    private SendMessageRequest createSendMessageRequest(String message, String messageGroupId) {
        return SendMessageRequest.builder().queueUrl(this.queueUrl)
                .messageGroupId(messageGroupId)
                .messageBody(message)
                .build();
    }

    private SendMessageRequest createSendMessageRequest(String message) {
        return SendMessageRequest.builder().queueUrl(this.queueUrl)
                .messageBody(message)
                .build();
    }
}
