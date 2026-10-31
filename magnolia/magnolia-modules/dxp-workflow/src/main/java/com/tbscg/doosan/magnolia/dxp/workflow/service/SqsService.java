package com.tbscg.doosan.magnolia.dxp.workflow.service;

import software.amazon.awssdk.services.sqs.model.InvalidMessageContentsException;

/**
 * Defines the services available to interact with AWS SQS queue.
 *
 * @author apuigescobar
 */
public interface SqsService {

    /**
     * Sends a message to SQS queue. It uses under-the-hood extended SQS client library to bypass max limit 256KB of SQS.
     * If message is bigger than 256KB, the S3 object is created with the content of message, otherwise it is sent directly to SQS as it is.
     *
     * @param message        defines the content which is going to be published to SQS.
     * @param messageGroupId is the tag that specifies that a message belongs to a specific message group. Messages that
     *                       belong to the same message group are always processed one by one, in a strict order relative to the message group.
     * @return messageId defined by SQS.
     * @throws InvalidMessageContentsException The message contains characters outside the allowed set.
     * @throws UnsupportedOperationException   Error code 400. Unsupported operation.
     */
    String sendMessage(String message, String messageGroupId);

    /**
     * Sends a message to SQS queue. It uses under-the-hood extended SQS client library to bypass max limit 256KB of SQS.
     * If message is bigger than 256KB, the S3 object is created with the content of message, otherwise it is sent directly to SQS as it is.
     *
     * @param message        defines the content which is going to be published to SQS.

     * @return messageId defined by SQS.
     * @throws InvalidMessageContentsException The message contains characters outside the allowed set.
     * @throws UnsupportedOperationException   Error code 400. Unsupported operation.
     */
    String sendMessage(String message);

}
