package com.tbscg.doosan.magnolia.dxp.core.app.action;


import info.magnolia.jcr.util.NodeNameHelper;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.pages.app.detail.PageEditorStatus;
import info.magnolia.pages.app.detail.action.clipboard.ClipboardSharedStorage;
import info.magnolia.pages.app.detail.action.clipboard.ComponentContentClipboard;
import info.magnolia.ui.contentapp.browser.ItemInteractionAvailability;
import info.magnolia.ui.datasource.jcr.JcrDatasource;
import info.magnolia.ui.framework.ContentClipboardException;
import info.magnolia.jcr.util.NodeTypes.Renderable;

import javax.inject.Inject;
import javax.jcr.*;
import java.util.Arrays;
import java.util.List;


public class ClipboardContents extends ComponentContentClipboard {

    private static final List<String> ALLOWED_PATHS = Arrays.asList("/irp", "/owner-portal", "/dp-software");
    private static final List<String> TEMPLATE_PREFIXES = Arrays.asList("irp", "dp-software", "owner-portal");
    private final PageEditorStatus pageEditorStatus;

    @Inject
    public ClipboardContents(ItemInteractionAvailability<Item> itemInteractionAvailability, NodeNameHelper nodeNameHelper, PageEditorStatus pageEditorStatus, JcrDatasource jcrDatasource, ClipboardSharedStorage<Item> sharedStorage) {
        super(itemInteractionAvailability, nodeNameHelper, pageEditorStatus, jcrDatasource, sharedStorage);
        this.pageEditorStatus = pageEditorStatus;
    }

    private boolean isPathAllowed(String path) {
        return ALLOWED_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean isTemplatePrefixValid(String prefix) {
        return TEMPLATE_PREFIXES.contains(prefix);
    }

    private String normalizeSource(String sourceTemplate, String sourcePrefix, String destinationPrefix) {
        return sourceTemplate.replaceFirst(sourcePrefix, destinationPrefix);
    }

    private void normalizeTemplates(Node source, Node destination) throws RepositoryException {
        String sourceTemplate = source.getProperty("mgnl:template").getValue().getString();
        String sourcePrefix = extractSourcePrefix(sourceTemplate);
        String destinationPrefix = extractDestinationPrefix(destination);
        if (!sourcePrefix.equals(destinationPrefix)
                && isTemplatePrefixValid(sourcePrefix)
                && isTemplatePrefixValid(destinationPrefix)) {
            String newTemplatePrefix = destinationPrefix + ":";
            source.getProperty("mgnl:template").setValue(normalizeSource(sourceTemplate, sourcePrefix + ":", newTemplatePrefix));
        }
        source.getSession().save();
    }

    @Override
    public List<Item> paste(Item destination) {
        final List<Item> pastedItems = super.paste(destination);
        pastedItems.forEach(item -> {
            if (item.isNode()) {
                try {
                    this.normalizeTemplates((Node) item, (Node) destination);
                } catch (RepositoryException e) {
                    throw new ContentClipboardException("Failed while pasting items under JCR item.", e);
                }
            }
        });
        return pastedItems;
    }

    @Override
    protected boolean canPasteInto(Item sourceItem, Item destinationItem) {
        try {
            Node target = (Node) destinationItem;
            return ((NodeUtil.isNodeType(target, "mgnl:area") || NodeUtil.isNodeType(target.getParent(), "mgnl:area"))
                    && (super.canPasteInto(sourceItem, destinationItem)
                    || (isPathAllowed(sourceItem.getPath()) && isPathAllowed(destinationItem.getPath())) && isSourceTemplateAvailable((Node)sourceItem, (Node)destinationItem)));
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isSourceTemplateAvailable(Node sourceNode, Node destinationNode) throws RepositoryException {
        String sourceTemplate = Renderable.getTemplate(sourceNode);
        String sourcePrefix = extractSourcePrefix(sourceTemplate);
        String destinationPrefix = extractDestinationPrefix(destinationNode);
        return this.pageEditorStatus.getAvailableTemplates().contains(normalizeSource(sourceTemplate, sourcePrefix, destinationPrefix));
    }

    private static String extractSourcePrefix(String sourceTemplate) {
        return sourceTemplate.split(":")[0];
    }

    private static String extractDestinationPrefix(Node destination) throws RepositoryException {
        return destination.getPath().split("/")[1];
    }
}
