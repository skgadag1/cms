package com.tbscg.doosan.magnolia.custom.resources.vaadin;

import com.vaadin.annotations.JavaScript;
import com.vaadin.server.AbstractClientConnector;
import com.vaadin.server.AbstractJavaScriptExtension;
import com.vaadin.server.ClientConnector;
import com.vaadin.ui.UI;

@JavaScript({ "custom.js.connector.js" })
public class JsResourcesComponent extends AbstractJavaScriptExtension {
    @Override
    public CustomResourcesComponentState getState() {
        return (CustomResourcesComponentState) super.getState();
    }

    @Override
    protected Class<? extends ClientConnector> getSupportedParentType() {
        return UI.class;
    }

    @Override
    public void extend(AbstractClientConnector target) {
        super.extend(target);
    }
}