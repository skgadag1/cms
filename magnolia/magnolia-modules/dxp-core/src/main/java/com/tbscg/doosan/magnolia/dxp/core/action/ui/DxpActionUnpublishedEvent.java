package com.tbscg.doosan.magnolia.dxp.core.action.ui;

import com.tbscg.doosan.magnolia.dxp.core.Component;

import java.util.Collections;
import java.util.List;

public class DxpActionUnpublishedEvent extends DxpActionEvent {

    public DxpActionUnpublishedEvent(String id,
                                     String modelId,
                                     String parentId,
                                     String parentNodeType,
                                     String path,
                                     List<String> customLinks,
                                     String nodeType,
                                     String workspace,
                                     long lastModified,
                                     String site) {
        super(id, modelId, parentId, parentNodeType, path, customLinks, nodeType, workspace, lastModified, Collections.emptyList(), site);
    }

    public DxpActionUnpublishedEvent(String id,
                                     String modelId,
                                     String parentId,
                                     String parentNodeType,
                                     String path,
                                     List<String> customLinks,
                                     String nodeType,
                                     String workspace,
                                     long lastModified,
                                     List<Component> components,
                                     String site) {
        super(id, modelId, parentId, parentNodeType,  path, customLinks, nodeType, workspace, lastModified, components, site);
    }

    @Override
    public void dispatch(Handler handler) {
        handler.onActionUnpublished(this);
    }
}
