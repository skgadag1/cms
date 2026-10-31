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

import com.vaadin.shared.JavaScriptExtensionState;
import lombok.Data;

/**
 * {@link JavaScriptExtensionState} for {@link CssResourcesComponent}.
 *
 * @author Magnolia International Ltd.
 */
@Data
public class CustomResourcesComponentState extends JavaScriptExtensionState {
    private String content;
}
