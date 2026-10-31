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

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.NodeVisitor;
import info.magnolia.link.LinkUtil;
import info.magnolia.objectfactory.Components;
import info.magnolia.translation.finder.PropertiesToTranslateFinder;
import info.magnolia.translation.finder.PropertyToTranslate;
import info.magnolia.translation.io.TranslationItem;
import info.magnolia.translation.io.writer.TranslationItemWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.*;

import static com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.isActivated;

/**
 * CUSTOMIZATION - Checks if node is activated.
 *
 * Export content visitor. Builds a TranslationBundle for each NodeData to be translated populating
 * it with the corresponding master value and locale-specific values. Passes each bundle to an
 * exporter.
 */
@Slf4j
public class ExportVisitor implements NodeVisitor {

    private final TranslationItemWriter translationBundleWriter;

    private final PropertiesToTranslateFinder finder;

    private final Locale[] targetLocales;

    private final Locale sourceLocale;

    private final Locale defaultLocale;

    private final String sourceLocaleStr;

    /**
     * @deprecated since 2.5.1. Use {@link #ExportVisitor(Locale sourceLocale, Locale defaultLocale, Locale[] targetLocales, TranslationItemWriter translationBundleWriter, PropertiesToTranslateFinder finder)} instead.
     */
    @Deprecated
    public ExportVisitor(Locale[] targetLocales, TranslationItemWriter translationBundleWriter, PropertiesToTranslateFinder finder) {
        this(Components.getComponent(I18nContentSupport.class).getDefaultLocale(),
                Components.getComponent(I18nContentSupport.class).getDefaultLocale(),
                targetLocales,
                translationBundleWriter,
                finder);
    }

    /**
     * Default constructor.
     *
     * Constructs an export visitor with the specified supported locales and exporter instance.
     */
    public ExportVisitor(Locale sourceLocale, Locale defaultLocale, Locale[] targetLocales, TranslationItemWriter translationBundleWriter, PropertiesToTranslateFinder finder) {
        this.targetLocales = targetLocales;
        this.translationBundleWriter = translationBundleWriter;
        this.finder = finder;
        this.sourceLocale = sourceLocale;
        this.defaultLocale = defaultLocale;
        this.sourceLocaleStr = getSuffixLocaleStr(defaultLocale, sourceLocale);

    }

    /**
     * Exports a translation bundle for {@link Property properties} returned from the finder.
     */
    @Override
    public void visit(Node node) throws RepositoryException {
        if (!isActivated(node)) {
            final List<PropertyToTranslate> propertiesToTranslate = finder.getPropertiesToTranslate(node);

            for (PropertyToTranslate propertyToTranslate : propertiesToTranslate) {
                final List<TranslationItem> bundles = buildTranslationBundle(node, propertyToTranslate);
                if (bundles != null && bundles.size() > 0) {

                    bundles.forEach(b -> {
                        try {
                            translationBundleWriter.writeRow(b);
                        } catch (IOException e) {
                            log.debug("Can not export bundle {}", b.getContext());
                        }
                    });
                }
            }
        }
    }

    /**
     * Builds a bundle for the node provided.
     */
    protected List<TranslationItem> buildTranslationBundle(final Node parentNode, final PropertyToTranslate propertyToTranslate) throws RepositoryException {

        List<TranslationItem> result = new ArrayList<>();

        Map<String, Property> defaultProperties = getPropertiesByPathAndLocale(parentNode, propertyToTranslate.getPropertyName(), sourceLocaleStr);

        for (String propKey : defaultProperties.keySet()) {

            Property defaultProperty = defaultProperties.get(propKey);

            // Page node is required for last modification date and link
            final Node pageNode = getParentNode(parentNode);

            log.debug("Creating TranslationBundle for node {} and nodeData {}", parentNode.getPath(), propertyToTranslate);

            Map<Locale, String> localizedValues = new HashMap<>();
            for (Locale locale : targetLocales) {
                String targetLocaleStr = getSuffixLocaleStr(defaultLocale, locale);
                Map<String, Property> localizedProperties = getPropertiesByPathAndLocale(parentNode, propKey, targetLocaleStr);
                if (localizedProperties.values().size() > 0) {
                    Property localizedProperty = localizedProperties.values().stream().findFirst().get();
                    localizedValues.put(locale, localizedProperty.isMultiple() ? MultiValueStringConverter.toString(localizedProperty.getValues()) : localizedProperty.getString());
                }
            }

            final String key = parentNode.getIdentifier() + ":" + propKey;
            String value = defaultProperty.isMultiple() ? MultiValueStringConverter.toString(defaultProperty.getValues()) : defaultProperty.getString();
            result.add(new TranslationItem.Builder(key, value, sourceLocale, propertyToTranslate.getContext())
                    .withLocalizedValues(localizedValues)
                    .withLastModificationTime(NodeTypes.LastModified.getLastModified(pageNode))
                    .withLink(LinkUtil.createExternalLink(pageNode))
                    .build());

        }

        return result;
    }

    private Map<String, Property> getPropertiesByPathAndLocale(final Node parentNode, final String path, final String locale) {

        String localeSuffix = StringUtils.isNotBlank(locale) ? "_" + locale : "";
        Map<String, Property> result = new HashMap<>();
        String key = "";
        List<String> pathParts = new ArrayList<>();
        for (String pathPart : path.split(PropertyToTranslate.PATH_SEPARATOR)) {
            if(StringUtils.isNotBlank(pathPart)) {
                pathParts.add(pathPart);
            }
        }
        if (pathParts.size() > 0) {
            String propertyName = pathParts.remove(pathParts.size() - 1);
            iteratePath(parentNode, pathParts, propertyName, localeSuffix, key, result);
        }

        return result;
    }

    private void iteratePath(final Node node, final List<String> path, final String propertyName, final String localeSuffix, String key, Map<String, Property> properties) {

        if (path.size() == 0) {
            key += PropertyToTranslate.PATH_SEPARATOR + propertyName;
            Property property = getPropertyByName(node, propertyName.replace(PropertyToTranslate.I18N_PLACEHOLDER, localeSuffix));
            if (property != null) {
                if (key.startsWith("/")) {
                    key = key.substring(1);
                }
                properties.put(key, property);
            }
        } else {

            String nodeName = path.remove(0);
            if (!nodeName.contains(PropertyToTranslate.INDEX_PLACEHOLDER)) {
                key += PropertyToTranslate.PATH_SEPARATOR + nodeName;
                Node childNode = getNodeByPath(node, nodeName.replace(PropertyToTranslate.I18N_PLACEHOLDER, localeSuffix));
                if (childNode != null) {
                    iteratePath(childNode, path, propertyName, localeSuffix, key, properties);
                }
            } else {
                long childNodeCount = getChildrenCount(node);
                String resetKey = key;
                ArrayList<String> resetPath = new ArrayList<>(path);
                for (long i = 0; i <= childNodeCount; i++) {
                    String indexedNodeName = nodeName.replace(PropertyToTranslate.INDEX_PLACEHOLDER, String.valueOf(i));
                    key += PropertyToTranslate.PATH_SEPARATOR + indexedNodeName;
                    Node childNode = getNodeByPath(node, indexedNodeName.replace(PropertyToTranslate.I18N_PLACEHOLDER, localeSuffix));
                    if (childNode != null) {
                        iteratePath(childNode, path, propertyName, localeSuffix, key, properties);
                    }

                    // reset parameters for next iteration/index
                    key = resetKey;
                    path.clear();
                    path.addAll(resetPath);
                }
            }
        }
    }

    private Node getNodeByPath(Node parent, String path) {
        String parentPath = "";
        try {
            parentPath = parent.getPath();
            if (parent.hasNode(path)) {
                return parent.getNode(path);
            }
        } catch (RepositoryException e) {
            log.debug("node not found: {}", parentPath + "/" + path);
        }
        return null;
    }


    private Property getPropertyByName(Node node, String propertyName) {
        String nodePath = "";
        try {
            nodePath = node.getPath();
            if (node.hasProperty(propertyName)) {
                return node.getProperty(propertyName);
            }
        } catch (RepositoryException e) {
            log.debug("property not found: {}", nodePath + ":" + propertyName);
        }

        return null;
    }

    private long getChildrenCount(Node parent) {
        String parentPath = "";
        try {
            parentPath = parent.getPath();
            return parent.getNodes().getSize();
        } catch (RepositoryException e) {
            log.debug("child nodes can't be retrieved for node: {}", parentPath);
        }
        return 0;
    }

    /**
     * This method was previously named differently (moved from AbstractTranslationBundleWriter of legacy module) but
     * would do the same thing: it tries to find the next parent node which is not of type
     * {@value NodeTypes.ContentNode#NAME}. This could either be a page {@value NodeTypes.Page#NAME} or a folder
     * ({@value NodeTypes.Folder#NAME} or {@value NodeTypes.Content#NAME}).
     */
    private Node getParentNode(final Node node) {
        Node potentialPage = node;
        try {
            while (NodeUtil.isNodeType(potentialPage, NodeTypes.ContentNode.NAME)) {
                potentialPage = potentialPage.getParent();
            }
        } catch (RepositoryException e) {
            throw new RuntimeException(String.format("Could not determine next page node of %s", NodeUtil.getPathIfPossible(node)), e);
        }
        return potentialPage;
    }

    private String getSuffixLocaleStr(Locale defaultLocale, Locale sourceLocale) {
        return (defaultLocale == null || sourceLocale == null || defaultLocale.equals(sourceLocale)) ?
                "" :
                sourceLocale.toString();
    }
}
