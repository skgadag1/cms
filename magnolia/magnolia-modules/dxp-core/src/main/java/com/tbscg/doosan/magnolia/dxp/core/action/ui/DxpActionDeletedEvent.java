package com.tbscg.doosan.magnolia.dxp.core.action.ui;

import java.util.Collections;
import java.util.List;

public class DxpActionDeletedEvent extends DxpActionEvent {

    public DxpActionDeletedEvent(String id,
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

    @Override
    public void dispatch(Handler handler) {
        handler.onActionDeleted(this);
    }
}
