package com.tbscg.doosan.magnolia.bobcat.rest.delivery;

import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.rest.reference.ReferenceResolver;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Resolves parent information for attachment nodes.
 * - For dxp:attachment -> returns parent attachmentGroup/attachmentCategory info
 * - For dxp:attachmentGroup -> returns parent attachmentCategory info
 * - For dxp:attachmentCategory -> returns parent info (or nulls if parent is folder)
 * 
 * Always returns a Map with parentName, parentTitle, parentSfId
 */
public class AttachmentParentResolver implements ReferenceResolver<String, Map<String, String>> {

    private static final String ATTACHMENTS_WORKSPACE = "attachments";
    private static final String FOLDER_TYPE = "mgnl:folder";

    private final Provider<Context> contextProvider;

    @Inject
    public AttachmentParentResolver(Provider<Context> contextProvider) {
        this.contextProvider = contextProvider;
    }

    @Override
    public Optional<Map<String, String>> resolve(final String nodeUuid) {
        try {
            return MgnlContext.doInSystemContext(() -> {
                try {
                    Context context = contextProvider.get();
                    Session session = context.getJCRSession(ATTACHMENTS_WORKSPACE);

                    Node node;
                    try {
                        node = session.getNodeByIdentifier(nodeUuid);
                    } catch (Exception e) {
                        return Optional.of(createEmptyParentInfo());
                    }

                    Node parent = getParent(node);
                    return Optional.of(createParentInfo(parent));

                } catch (final RepositoryException e) {
                    return Optional.of(createEmptyParentInfo());
                }
            });
        } catch (final Exception e) {
            return Optional.of(createEmptyParentInfo());
        }
    }

    private Node getParent(Node node) throws RepositoryException {
        Node parent = node.getParent();
        String parentType = parent.getPrimaryNodeType().getName();

        if (FOLDER_TYPE.equals(parentType)) {
            return null;
        }

        return parent;
    }

    private Map<String, String> createParentInfo(Node parent) {
        Map<String, String> parentInfo = new HashMap<>();
        
        if (parent == null) {
            parentInfo.put("parentName", "");
            parentInfo.put("parentTitle", "");
            parentInfo.put("parentSfId", "");
        } else {
            try {
                parentInfo.put("parentName", parent.getName());
            } catch (RepositoryException e) {
                parentInfo.put("parentName", "");
            }
            parentInfo.put("parentTitle", getPropertyValue(parent, "title"));
            parentInfo.put("parentSfId", getPropertyValue(parent, "sfId"));
        }
        
        return parentInfo;
    }

    private Map<String, String> createEmptyParentInfo() {
        Map<String, String> parentInfo = new HashMap<>();
        parentInfo.put("parentName", "");
        parentInfo.put("parentTitle", "");
        parentInfo.put("parentSfId", "");
        return parentInfo;
    }

    private String getPropertyValue(Node node, String propertyName) {
        try {
            if (node != null && node.hasProperty(propertyName)) {
                return node.getProperty(propertyName).getString();
            }
        } catch (RepositoryException e) {
        }
        return "";
    }
}
