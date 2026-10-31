/**
 * This file Copyright (c) 2017-2018 Magnolia International
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
package com.tbscg.doosan.magnolia.toc.io;

import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;
import info.magnolia.objectfactory.Components;
import info.magnolia.translation.finder.PropertyToTranslate;
import info.magnolia.translation.io.TranslationItem;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * CUSTOMIZATION - Checks if node is activated.
 *
 * Applies translation item updates to the repository.
 */
@Slf4j
public class TranslationItemImporter {

    private final Session session;

    /**
     * When true node updates with empty values will be written. When false only node updates with
     * non-empty values will be written.
     */
    private final boolean importEmptyValues;

    /**
     * When true existing nodes values will be overwritten. When false node values will only be
     * written when the node does not already exist or if it exists with an empty value.
     */
    private final boolean overwriteExistingValues;

    public TranslationItemImporter(Session session, boolean overwriteExistingValues, boolean importEmptyValues) {
        this.session = session;
        this.overwriteExistingValues = overwriteExistingValues;
        this.importEmptyValues = importEmptyValues;
    }

    /**
     * Applies a node bundle update to the repository.
     */
    public void applyUpdate(TranslationItem translationItem) throws RepositoryException {
        final String[] identifierAndPropertyName = translationItem.getKey().split(":");
        if (identifierAndPropertyName.length != 2) {
            throw new IllegalArgumentException(String.format("Expected valid item key but got '%s'", translationItem.getKey()));
        }

        final String identifierOfParentNode = identifierAndPropertyName[0];
        final String identifierOfProperty =
                // if i18n placeholder is not contained in the property name,
                // we are dealing with old export key format
                // (add i18n placeholder at the end in this case)
                identifierAndPropertyName[1].contains(PropertyToTranslate.I18N_PLACEHOLDER) ?
                identifierAndPropertyName[1] :
                identifierAndPropertyName[1] + PropertyToTranslate.I18N_PLACEHOLDER;

        // Find the parent node, this must already exist
        Node parentNode = null;
        try {
            parentNode = getParentNode(identifierOfParentNode);
        } catch (RepositoryException e) {
            log.warn("Parent node [{}] does not exist. It has probably been deleted.", identifierOfParentNode, e);
        }

        if (parentNode != null && !NodeUtil.isActivated(parentNode)) {

            final String masterPropertyPath = identifierOfProperty.replace(PropertyToTranslate.I18N_PLACEHOLDER, getLocaleStringPropertySuffix(translationItem.getMasterLocale(), parentNode));
            if (parentNode.hasProperty(masterPropertyPath)) {
                Property masterProperty = parentNode.getProperty(masterPropertyPath);

                if (masterProperty.isMultiple()) {
                    // Update the master property value, creating the property if appropriate
                    this.updateMultiValuePropertyValue(parentNode, masterPropertyPath, MultiValueStringConverter.toArray(translationItem.getMasterValue()));

                    // Update each localized property, creating each property if appropriate
                    for (Locale updateLocale : translationItem.getLocales()) {
                        final String localizedValue = translationItem.getLocalizedValue(updateLocale);
                        this.updateMultiValuePropertyValue(parentNode, identifierOfProperty.replace(PropertyToTranslate.I18N_PLACEHOLDER, getLocaleStringPropertySuffix(updateLocale, parentNode)),
                                MultiValueStringConverter.toArray(localizedValue));
                    }
                } else {
                    // Update the master property value, creating the property if appropriate
                    this.updatePropertyValue(parentNode, masterPropertyPath, translationItem.getMasterValue());

                    // Update each localized property, creating each property if appropriate
                    for (Locale updateLocale : translationItem.getLocales()) {
                        final String localizedValue = translationItem.getLocalizedValue(updateLocale);
                        this.updatePropertyValue(parentNode, identifierOfProperty.replace(PropertyToTranslate.I18N_PLACEHOLDER, getLocaleStringPropertySuffix(updateLocale, parentNode)),
                                localizedValue);
                    }
                }
            }
        }
    }

    protected Property getProperty(Node parent, String propertyName, boolean create) throws RepositoryException {
        Property property = parent.hasProperty(propertyName) ? parent.getProperty(propertyName) : null;
        if (create && (property == null || property.getValue() == null)) {
            if (propertyName.contains(PropertyToTranslate.PATH_SEPARATOR)) {
                final int lastSeparatorIndex = propertyName.lastIndexOf(PropertyToTranslate.PATH_SEPARATOR);
                final String childNodePath = propertyName.substring(0, lastSeparatorIndex);
                final String propertyNameClean = propertyName.substring(lastSeparatorIndex + 1);
                final Node childNode = getOrCreateNode(parent, childNodePath);
                property = childNode.setProperty(propertyNameClean, "");
            } else {
                property = parent.setProperty(propertyName, "");
            }
        }

        return property != null && property.getValue() != null ? property : null;
    }

    private Node getOrCreateNode(Node parent, String path) throws RepositoryException {
        Node result = parent;
        final List<String> pathParts = Arrays.asList(path.split(PropertyToTranslate.PATH_SEPARATOR));
        for (String pathPart : pathParts) {
            if (result.hasNode(pathPart)) {
                result = result.getNode(pathPart);
            } else {
                result = result.addNode(pathPart, "mgnl:contentNode");
            }
        }
        return result;
    }

    protected Property getMultiValueProperty(Node parent, String propertyName, boolean create) throws RepositoryException {
        Property property = parent.hasProperty(propertyName) ? parent.getProperty(propertyName) : null;
        if (create && (property == null || property.getValues() == null)) {
            property = parent.setProperty(propertyName, new String[0]);
        }
        return property != null && property.getValues() != null ? property : null;
    }

    private Node getParentNode(String uuid) throws RepositoryException {
        return session.getNodeByIdentifier(uuid);
    }

    private boolean isEmpty(Property property) throws RepositoryException {
        if (property.isMultiple()) {
            return property == null || property.getValues() == null
                    || property.getValues().length == 0;
        } else {
            return property == null || property.getValue() == null
                    || StringUtils.isEmpty(property.getValue().getString());
        }
    }

    private void updatePropertyValue(Node parent, String propertyName, String value) throws RepositoryException {
        if (importEmptyValues || !StringUtils.isEmpty(value)) {
            final Property property = getProperty(parent, propertyName, true);

            if (overwriteExistingValues || isEmpty(property)) {
                if (log.isDebugEnabled()) {
                    log.debug("Updating property [{}] with value [{}]", property.getPath(), value);
                }

                property.setValue(value != null ? value : "");
            }
        }
    }

    private void updateMultiValuePropertyValue(Node parent, String propertyPath, String[] value) throws RepositoryException {
        if (this.importEmptyValues || value != null) {
            final Property property = this.getMultiValueProperty(parent, propertyPath, true);

            if (this.overwriteExistingValues || this.isEmpty(property)) {
                this.setMultiValuePropertyValue(property, value);
            }
        }
    }

    private void setMultiValuePropertyValue(Property property, String[] value) throws RepositoryException {
        if (log.isDebugEnabled()) {
            log.debug("Updating property [{}] with value [{}]", (property != null) ? property.getPath() : null, value);
        }
        if (property != null) {
            property.setValue(value != null ? value : new String[0]);
        }
    }

    private String getLocaleStringPropertySuffix(Locale locale, Node node) {
        final SiteManager siteManager = Components.getComponent(SiteManager.class);
        final Site siteToExport = siteManager.getAssignedSite(node);
        final I18nContentSupport i18nContentSupport = siteToExport.getI18n();
        Locale passedOrDefaultLocale = locale;
        if (passedOrDefaultLocale == null) {
            // in case of null, fallback to default locale
            passedOrDefaultLocale =  i18nContentSupport.getDefaultLocale();
        }
        return i18nContentSupport.getDefaultLocale().equals(passedOrDefaultLocale) ? "" : "_" + passedOrDefaultLocale.toString();
    }
}
