package com.tbscg.doosan.magnolia.bobcat.rest.delivery;

import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.rest.reference.ReferenceResolver;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.jcr.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class CompatibleAttachmentsCountResolver implements ReferenceResolver<String, Integer> {

    private static final String ATTACHMENTS_WORKSPACE = "attachments";
    private static final String PRODUCTS_WORKSPACE = "products";
    private static final String PRODUCT_PROPERTY = "product";

    private final Provider<Context> contextProvider;

    @Inject
    public CompatibleAttachmentsCountResolver(Provider<Context> contextProvider) {
        this.contextProvider = contextProvider;
    }

    @Override
    public Optional<Integer> resolve(final String productOrGroupUuid) {
        try {
            return MgnlContext.doInSystemContext(() -> {
                try {
                    Context context = contextProvider.get();
                    Session productsSession = context.getJCRSession(PRODUCTS_WORKSPACE);
                    Session attachmentsSession = context.getJCRSession(ATTACHMENTS_WORKSPACE);

                    Node productNode;
                    try {
                        productNode = productsSession.getNodeByIdentifier(productOrGroupUuid);
                    } catch (ItemNotFoundException e) {
                        return Optional.of(0);
                    }

                    Set<String> productUuids = new HashSet<>();
                    collectProductUuids(productNode, productUuids);

                    if (productUuids.isEmpty()) {
                        return Optional.of(0);
                    }

                    int count = countCompatibleAttachments(attachmentsSession, productUuids);
                    return Optional.of(count);

                } catch (final RepositoryException e) {
                    return Optional.of(0);
                }
            });
        } catch (final Exception e) {
            return Optional.of(0);
        }
    }

    private void collectProductUuids(Node node, Set<String> productUuids) throws RepositoryException {
        String nodeType = node.getPrimaryNodeType().getName();

        if ("dxp:product".equals(nodeType)) {
            productUuids.add(node.getIdentifier());
        }

        NodeIterator children = node.getNodes();
        while (children.hasNext()) {
            Node child = children.nextNode();
            String childType = child.getPrimaryNodeType().getName();

            if ("dxp:productGroup".equals(childType) ||
                "dxp:subcategory".equals(childType) ||
                "dxp:product".equals(childType)) {
                collectProductUuids(child, productUuids);
            }
        }
    }

    private int countCompatibleAttachments(Session attachmentsSession, Set<String> productUuids)
            throws RepositoryException {
        Set<String> compatibleAttachmentUuids = new HashSet<>();

        for (String productUuid : productUuids) {
            String queryStatement = "SELECT * FROM [nt:base] WHERE [" + PRODUCT_PROPERTY + "] = '" + productUuid + "'";
            NodeIterator productRefNodes = info.magnolia.cms.util.QueryUtil.search(ATTACHMENTS_WORKSPACE, queryStatement, "JCR-SQL2");
            
            while (productRefNodes.hasNext()) {
                try {
                    Node productRefNode = productRefNodes.nextNode();
                    Node productsContainer = productRefNode.getParent();
                    if (productsContainer != null && "products".equals(productsContainer.getName())) {
                        Node attachment = productsContainer.getParent();
                        if (attachment != null && attachment.isNodeType("dxp:attachment")) {
                            compatibleAttachmentUuids.add(attachment.getIdentifier());
                        }
                    }
                } catch (ItemNotFoundException ignored) {
                    continue;
                }
            }
        }

        if (compatibleAttachmentUuids.isEmpty()) {
            return 0;
        }

        Set<String> attachmentGroupUuids = new HashSet<>();
        int ungroupedCount = 0;

        for (String attUuid : compatibleAttachmentUuids) {
            try {
                Node attachment = attachmentsSession.getNodeByIdentifier(attUuid);
                Node parent = attachment.getParent();
                
                if (parent != null && parent.isNodeType("dxp:attachmentGroup")) {
                    attachmentGroupUuids.add(parent.getIdentifier());
                } else {
                    ungroupedCount++;
                }
            } catch (ItemNotFoundException ignored) {
                continue;
            }
        }

        return attachmentGroupUuids.size() + ungroupedCount;
    }


}

