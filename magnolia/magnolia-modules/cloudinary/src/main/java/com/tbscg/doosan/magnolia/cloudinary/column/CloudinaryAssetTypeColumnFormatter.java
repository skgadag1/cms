/**
 * This file Copyright (c) 2014-2018 Magnolia International
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
package com.tbscg.doosan.magnolia.cloudinary.column;

import com.vaadin.v7.data.util.BeanItem;
import com.vaadin.v7.ui.Table;
import info.magnolia.dam.api.Asset;
import info.magnolia.dam.api.Item;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.workbench.column.AbstractColumnFormatter;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;

/**
 * Mime-type column formatter for Cloudinary assets.
 *
 * @author Alejandro Puig Escobar
 */
public class CloudinaryAssetTypeColumnFormatter extends AbstractColumnFormatter<CloudinaryAssetTypeColumnDefinition> {

    private final ContentConnector contentConnector;

    @Inject
    public CloudinaryAssetTypeColumnFormatter(CloudinaryAssetTypeColumnDefinition definition, ContentConnector contentConnector) {
        super(definition);
        this.contentConnector = contentConnector;
    }

    @Override
    public Object generateCell(Table source, Object itemId, Object columnId) {
        com.vaadin.v7.data.Item uiItem = contentConnector.getItem(itemId);
        if (uiItem instanceof BeanItem) {
            Item item = ((BeanItem<Item>) uiItem).getBean();
            if (item != null && item.isAsset()) {
                return ((Asset) item).getMimeType();
            }
        }
        return StringUtils.EMPTY;
    }
}
