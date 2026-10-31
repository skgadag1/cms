package com.tbscg.doosan.magnolia.dxp.core.action.ui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tbscg.doosan.magnolia.dxp.core.Component;
import com.tbscg.doosan.magnolia.dxp.core.DxpMagnolia;
import com.tbscg.doosan.magnolia.dxp.core.aws.EventBridge;
import com.tbscg.doosan.magnolia.dxp.core.util.TracingUtil;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;

import java.util.Date;
import java.util.List;

import static com.tbscg.doosan.magnolia.dxp.core.aws.EventBridge.*;

@Slf4j
public abstract class DxpActionEventHandler implements DxpActionEvent.Handler {

    protected final EventBridgeClient eventBridgeClient;

    public DxpActionEventHandler() {
        this.eventBridgeClient = EventBridgeClient.builder().build();
    }

    @Override
    public void onActionCommitted(DxpActionEvent actionEvent) {
        onActionEvent(actionEvent, DxpMagnolia.AUTHOR_INSTANCE, getCommitDetailType(actionEvent.getWorkspace()), "DxpActionCommittedEvent");
    }

    @Override
    public void onActionDeleted(DxpActionEvent actionEvent) {
        onActionEvent(actionEvent, DxpMagnolia.AUTHOR_INSTANCE, getDeleteDetailType(actionEvent.getWorkspace()), "DxpActionDeletedEvent");
    }

    @Override
    public void onActionPublished(DxpActionEvent actionEvent) {
        onActionEvent(actionEvent, DxpMagnolia.PUBLIC_INSTANCE, getPublishDetailType(actionEvent.getWorkspace()), "DxpActionPublishedEvent");
    }

    @Override
    public void onActionUnpublished(DxpActionEvent actionEvent) {
        onActionEvent(actionEvent, DxpMagnolia.PUBLIC_INSTANCE, getUnpublishDetailType(actionEvent.getWorkspace()), "DxpActionUnpublishedEvent");
    }

    protected void onActionEvent(DxpActionEvent actionEvent, String instance, String detailType, String eventName) {
        if (shouldApplyHandler(actionEvent)) {
            log.info("Got {} {}", eventName, actionEvent);
            Event event = buildEvent(actionEvent, instance);
            try {
                putEvents(event, detailType);
            } catch (Exception e) {
                log.error("Cannot publish {} to EventBridge for event {}, because of {}", eventName, event, e);
                throw new UnprocessedEventException("EventBridge event not published", e);
            }
        }
    }

    protected void putEvents(Event event, String detailType) throws JsonProcessingException {
        PutEventsRequestEntry entry = buildEntry(event, detailType);
        PutEventsRequest request = PutEventsRequest.builder().entries(entry)
                .build();
        PutEventsResponse result = TracingUtil.callWithTracing(traceID -> eventBridgeClient.putEvents(request));
        log.info("Got result {}", result);
    }

    protected PutEventsRequestEntry buildEntry(Event event, String detailType) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String detail = objectMapper.writeValueAsString(event);
        PutEventsRequestEntry entry = PutEventsRequestEntry.builder()
                .time(new Date().toInstant())
                .source(EventBridge.SOURCE)
                .detailType(detailType)
                .eventBusName(EventBridge.EVENT_BUS_NAME)
                .detail(detail)
                .build();
        return entry;
    }

    protected Event buildEvent(DxpActionEvent actionEvent, String instance) {
        return Event.builder()
                .withId(actionEvent.getId())
                .withModelId(actionEvent.getModelId())
                .withParentId(actionEvent.getParentId())
                .withPath(actionEvent.getPath())
                .withCustomLinks(actionEvent.getCustomLinks())
                .withNodeType(actionEvent.getNodeType())
                .withInstance(instance)
                .withMagnoliaApp(getMagnoliaApp(actionEvent.getWorkspace()))
                .withLastModified(actionEvent.getLastModifiedInMillis())
                .withComponents(actionEvent.getComponents())
                .withSite(actionEvent.getSite())
                .build();
    }

    protected abstract String getMagnoliaDetailType(String workspace);

    protected abstract String getMagnoliaApp(String workspace);

    private String getDetailType(String detailType, String workspace) {
        return String.join("-", getMagnoliaDetailType(workspace), detailType);
    }

    protected String getCommitDetailType(String workspace) {
        return getDetailType(MAGNOLIA_COMMIT_ACTION, workspace);
    }

    protected String getPublishDetailType(String workspace) {
        return getDetailType(MAGNOLIA_PUBLISH_ACTION, workspace);
    }

    protected String getUnpublishDetailType(String workspace) {
        return getDetailType(MAGNOLIA_UNPUBLISH_ACTION, workspace);
    }

    protected String getDeleteDetailType(String workspace) {
        return getDetailType(MAGNOLIA_DELETE_ACTION, workspace);
    }

    @Getter
    @SuperBuilder(setterPrefix = "with")
    protected static class Event {
        final String id;
        final String modelId;
        final String parentId;
        final String path;
        final String nodeType;
        final String instance;
        final String magnoliaApp;
        final List<String> customLinks;
        final long lastModified;
        final List<Component> components;
        final String site;
    }
}
