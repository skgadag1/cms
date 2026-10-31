/**
 * This file Copyright (c) 2021 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This program and the accompanying materials are made
 * available under the terms of the Magnolia Network Agreement
 * which accompanies this distribution, and is available at
 * http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package com.tbscg.doosan.magnolia.custom.resources.vaadin;

import com.vaadin.annotations.JavaScript;
import com.vaadin.server.AbstractClientConnector;
import com.vaadin.server.AbstractJavaScriptExtension;
import com.vaadin.server.ClientConnector;
import com.vaadin.ui.UI;

/**
 * {@link AbstractJavaScriptExtension} for thec custom CSS JS component.
 *
 * @author Magnolia International Ltd.
 */
@JavaScript({ "custom.css.connector.js"})
public class CssResourcesComponent extends AbstractJavaScriptExtension {

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
