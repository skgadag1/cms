package com.tbscg.doosan.magnolia.localizations.service;

import com.tbscg.doosan.magnolia.dxp.workflow.util.WorkflowUtil;
import com.tbscg.doosan.magnolia.localizations.model.status.LocalizationPackageStatus;
import com.tbscg.doosan.magnolia.localizations.model.status.LocalizationPublicationInfo;
import info.magnolia.context.MgnlContext;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.framework.message.MessagesManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.kie.api.runtime.process.WorkItem;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Calendar;

@Slf4j
public class LocalizationPackageStatusService {

    private MessagesManager messagesManager;

    @Inject
    public LocalizationPackageStatusService(MessagesManager messagesManager) {
        this.messagesManager = messagesManager;
    }

    public void reportSuccess(Node node) {
        LocalizationPublicationInfo publicationInfo = new LocalizationPublicationInfo(
                LocalizationPackageStatus.PUBLISHED_TO_SQS,
                Calendar.getInstance(),
                StringUtils.EMPTY
        );
        try {
            publicationInfo.save(node);
        } catch(RepositoryException e) {
            log.debug("Error while storing localization package info", e);
        }
    }

    public void reportFailure(WorkItem workItem, String message, Throwable cause) {
        try {
            Node node = WorkflowUtil.getNodeFromWorkItem(workItem, MgnlContext.getInstance());

            LocalizationPublicationInfo publicationInfo = new LocalizationPublicationInfo(
                LocalizationPackageStatus.ERROR,
                Calendar.getInstance(),
                message
            );

            publicationInfo.save(node);
            sendNotification(workItem, message, cause);
        } catch(RepositoryException e) {
            log.debug("Error while storing localization package info", e);
        }
    }

    private void sendNotification(WorkItem workItem, String error, Throwable e) {

        // Creating message
        final Message message = new Message();

        // Getting message values
        final String path = WorkflowUtil.getNodePathFromWorkItem(workItem);
        final String taskId = WorkflowUtil.getTaskIdFromWorkItem(workItem);
        final String subject = "Error while publishing localization package for path: " + path + ".";
        final String bodyMessage = generateBodyMessage(path, taskId, error, e);

        // Configuring message
        message.setSubject(subject);
        message.setMessage(bodyMessage);
        message.setType(MessageType.ERROR);

        // Sending message
        messagesManager.sendLocalMessage(message);
    }

    private String generateBodyMessage(String path, String taskId, String error, Throwable throwable) {

        final StringBuilder messageBody = new StringBuilder();
        messageBody.append("Error while publishing localization package ");
        messageBody.append(StringUtils.LF);
        messageBody.append("Path: ");
        messageBody.append(path);
        messageBody.append(StringUtils.LF);
        messageBody.append("TaskId: ");
        messageBody.append(taskId);
        messageBody.append(StringUtils.LF);
        messageBody.append("Details: ");
        messageBody.append(StringUtils.LF);
        messageBody.append(error);
        messageBody.append(StringUtils.LF);
        messageBody.append(ExceptionUtils.getStackTrace(throwable));

        return messageBody.toString();
    }

}
