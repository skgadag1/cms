/**
 * This file Copyright (c) 2022 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package com.tbscg.doosan.magnolia.bobcat.ui.contentapp.column.definition;

import info.magnolia.ui.contentapp.configuration.column.ConfiguredColumnDefinition;
import info.magnolia.ui.contentapp.configuration.column.renderer.HtmlCleaningRenderer;

import org.apache.commons.text.StringEscapeUtils;

import com.vaadin.data.ValueProvider;
import com.vaadin.ui.renderers.HtmlRenderer;

/**
 * Base class for column definitions rendering HTML.
 * Accepts only {@link com.tbscg.doosan.magnolia.bobcat.ui.contentapp.column.definition.AbstractSafeHtmlColumnDefinition.AbstractValueProvider} implementations.
 */
public abstract class AbstractSafeHtmlColumnDefinition<T> extends ConfiguredColumnDefinition<T> {

    protected AbstractSafeHtmlColumnDefinition() {
        setRenderer(Renderer.class);
    }

    public void init() {
        if (HtmlRenderer.class.isAssignableFrom(getRenderer()) && !AbstractValueProvider.class.isAssignableFrom(getValueProvider())) {
            setRenderer(HtmlCleaningRenderer.class); //use sanitization of whole content
            throw new IllegalArgumentException("AbstractSafeHtmlColumnDefinition should use an instance of AbstractSafeHtmlColumnDefinition.AbstractValueProvider but has " + getValueProvider());
        }
    }

    /**
     * Developer is responsible for escaping HTML provided from user (content) or another developer (definitions, translation files) inputs with help of {@link #escapeHtml(String)}.
     */
    protected abstract static class AbstractValueProvider<T> implements ValueProvider<T, String> {

        protected String escapeHtml(String value) {
            return StringEscapeUtils.escapeHtml4(value);
        }
    }

    /**
     * {@link com.vaadin.ui.renderers.Renderer} for {@link com.tbscg.doosan.magnolia.bobcat.ui.contentapp.column.definition.AbstractSafeHtmlColumnDefinition}.
     * Unlike {@link com.vaadin.ui.renderers.HtmlRenderer} which is mapped to {@link info.magnolia.ui.contentapp.configuration.column.renderer.HtmlCleaningRenderer} at runtime,
     * this render doesn't sanitize content, this is responsibility of developer.
     */
    public static class Renderer extends HtmlRenderer {
    }

}