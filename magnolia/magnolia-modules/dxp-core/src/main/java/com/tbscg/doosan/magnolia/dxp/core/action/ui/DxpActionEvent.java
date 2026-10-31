package com.tbscg.doosan.magnolia.dxp.core.action.ui;

import com.tbscg.doosan.magnolia.dxp.core.Component;
import info.magnolia.event.Event;
import info.magnolia.event.EventHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Getter
@AllArgsConstructor
@ToString
@Slf4j
public abstract class DxpActionEvent implements Event<DxpActionEvent.Handler> {
    private final String id;
    private final String modelId;
    private final String parentId;
    private final String parentNodeType;
    private final String path;
    private final List<String> customLinks;
    private final String nodeType;
    private final String workspace;
    private final long lastModifiedInMillis;
    private final List<Component> components;
    private final String site;

    public interface Handler extends EventHandler {
        void onActionCommitted(DxpActionEvent event);

        void onActionPublished(DxpActionEvent event);

        void onActionUnpublished(DxpActionEvent event);

        void onActionDeleted(DxpActionEvent event);

        boolean shouldApplyHandler(DxpActionEvent event);
    }
}
