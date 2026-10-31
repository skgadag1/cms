package com.tbscg.doosan.magnolia.bobcat.rest.delivery;

import com.tbscg.doosan.magnolia.bobcat.rest.model.CompatibleMachines;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.rest.reference.ReferenceResolver;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.jcr.*;
import java.util.*;

public class CompatibleMachinesResolver implements ReferenceResolver<String, CompatibleMachines> {

    private static final String PRODUCTS_NODE = ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCTS;
    private static final String PRODUCT_PROPERTY = ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCT;
    private static final String TITLE_PROPERTY = ProductCatalogConstants.Properties.TITLE;
    private static final String TYPE_PRODUCT = ProductCatalogConstants.NodeTypes.Product.NAME;
    private static final String TYPE_PRODUCT_GROUP = ProductCatalogConstants.NodeTypes.ProductGroup.NAME;
    private static final String TYPE_SUBCATEGORY = ProductCatalogConstants.NodeTypes.SubCategory.NAME;
    private static final String TYPE_CATEGORY = ProductCatalogConstants.NodeTypes.Category.NAME;

    private final Provider<Context> contextProvider;
    private final RequestLocaleResolver requestLocaleResolver;
    private final I18nLabelResolver i18nLabelResolver;

    @Inject
    public CompatibleMachinesResolver(Provider<Context> contextProvider,
                                      RequestLocaleResolver requestLocaleResolver,
                                      I18nLabelResolver i18nLabelResolver) {
        this.contextProvider = contextProvider;
        this.requestLocaleResolver = requestLocaleResolver;
        this.i18nLabelResolver = i18nLabelResolver;
    }

    @Override
    public Optional<CompatibleMachines> resolve(final String attachmentUuid) {
        return resolveInSystemContext(attachmentUuid);
    }

    private Optional<CompatibleMachines> resolveInSystemContext(final String attachmentUuid) {
        final Locale requestLocale = requestLocaleResolver.resolve();
        final I18nLabelResolver.LabelLookup titleLookup = i18nLabelResolver.forProperty(TITLE_PROPERTY, requestLocale);
        try {
            return MgnlContext.doInSystemContext(() -> resolveFromAttachmentNode(attachmentUuid, titleLookup));
        } catch (Exception e) {
            return Optional.of(emptyCompatibleMachines());
        }
    }

    private Optional<CompatibleMachines> resolveFromAttachmentNode(final String attachmentUuid,
                                                                   final I18nLabelResolver.LabelLookup titleLookup) {
        try {
            final Context context = contextProvider.get();
            final Session attachmentsSession = context.getJCRSession(ProductCatalogConstants.ATTACHMENTS_WORKSPACE);
            final Session productsSession = context.getJCRSession(ProductCatalogConstants.PRODUCTS_WORKSPACE);
            final Optional<Node> attachmentNode = getNodeByIdentifier(attachmentsSession, attachmentUuid);
            if (attachmentNode.isEmpty()) {
                return Optional.of(emptyCompatibleMachines());
            }
            final List<String> productIds = collectProductIds(attachmentNode.get());
            final Set<String> machineNames = collectMachineNames(productsSession, productIds, titleLookup);
            return Optional.of(createCompatibleMachinesPayload(machineNames));
        } catch (RepositoryException e) {
            return Optional.of(emptyCompatibleMachines());
        }
    }

    private Set<String> collectMachineNames(final Session productsSession,
                                                      final List<String> productIds,
                                                      final I18nLabelResolver.LabelLookup titleLookup) throws RepositoryException {
        final Set<String> machineNames = new LinkedHashSet<>();
        for (String productId : productIds) {
            final Optional<Node> referencedNode = getNodeByIdentifier(productsSession, productId);
            if (referencedNode.isEmpty()) {
                continue;
            }
            collectMachineNamesFromReferenceNode(referencedNode.get(), machineNames, titleLookup);
        }
        return machineNames;
    }

    private List<String> collectProductIds(final Node attachmentNode) throws RepositoryException {
        final Optional<Node> productsNode = getProductsNode(attachmentNode);
        if (productsNode.isEmpty()) {
            return Collections.emptyList();
        }
        return extractProductIds(productsNode.get());
    }

    private Optional<Node> getProductsNode(final Node attachmentNode) throws RepositoryException {
        try {
            return Optional.of(attachmentNode.getNode(PRODUCTS_NODE));
        } catch (PathNotFoundException e) {
            return Optional.empty();
        }
    }

    private List<String> extractProductIds(final Node productsNode) throws RepositoryException {
        final List<String> productIds = new ArrayList<>();
        final NodeIterator productItems = productsNode.getNodes();
        while (productItems.hasNext()) {
            final String productId = PropertyUtil.getString(productItems.nextNode(), PRODUCT_PROPERTY);
            if (!StringUtils.isBlank(productId)) {
                productIds.add(productId);
            }
        }
        return productIds;
    }

    private Optional<Node> getNodeByIdentifier(final Session session, final String nodeId) throws RepositoryException {
        try {
            return Optional.of(session.getNodeByIdentifier(nodeId));
        } catch (ItemNotFoundException ignored) {
            return Optional.empty();
        }
    }

    private void collectMachineNamesFromReferenceNode(final Node referencedNode,
                                                      final Set<String> machineNames,
                                                      final I18nLabelResolver.LabelLookup titleLookup) throws RepositoryException {
        final String nodeType = referencedNode.getPrimaryNodeType().getName();
        if (TYPE_PRODUCT.equals(nodeType)) {
            resolveMachineNameFromProduct(referencedNode, titleLookup).ifPresent(machineNames::add);
            return;
        }

        if (TYPE_PRODUCT_GROUP.equals(nodeType)) {
            resolveMachineNameFromProductGroup(referencedNode, titleLookup).ifPresent(machineNames::add);
            return;
        }

        if (TYPE_SUBCATEGORY.equals(nodeType)) {
            resolveMachineLabel(referencedNode, titleLookup).ifPresent(machineNames::add);
            return;
        }

        if (TYPE_CATEGORY.equals(nodeType)) {
            collectSubcategoryMachineNamesOrCategory(referencedNode, machineNames, titleLookup);
            return;
        }

        resolveMachineNameFromAncestors(referencedNode, titleLookup).ifPresent(machineNames::add);
    }

    private void collectSubcategoryMachineNamesOrCategory(final Node categoryNode,
                                                          final Set<String> machineNames,
                                                          final I18nLabelResolver.LabelLookup titleLookup) throws RepositoryException {
        boolean hasSubcategoryChild = false;
        final NodeIterator children = categoryNode.getNodes();
        while (children.hasNext()) {
            final Node child = children.nextNode();
            if (!TYPE_SUBCATEGORY.equals(child.getPrimaryNodeType().getName())) {
                continue;
            }
            hasSubcategoryChild = true;
            resolveMachineLabel(child, titleLookup).ifPresent(machineNames::add);
        }

        if (!hasSubcategoryChild) {
            resolveMachineLabel(categoryNode, titleLookup).ifPresent(machineNames::add);
        }
    }

    private Optional<String> resolveMachineNameFromProduct(final Node productNode,
                                                           final I18nLabelResolver.LabelLookup titleLookup) throws RepositoryException {
        Node parent = getParent(productNode);
        if (parent == null) {
            return Optional.empty();
        }
        // Mirror indexed product hierarchy logic: skip productGroup level for machine grouping.
        if (TYPE_PRODUCT_GROUP.equals(parent.getPrimaryNodeType().getName())) {
            parent = getParent(parent);
        }
        if (parent == null) {
            return Optional.empty();
        }
        return resolveMachineLabel(parent, titleLookup);
    }

    private Optional<String> resolveMachineNameFromProductGroup(final Node productGroupNode,
                                                                final I18nLabelResolver.LabelLookup titleLookup) {
        final Node parent = getParent(productGroupNode);
        if (parent == null) {
            return Optional.empty();
        }
        return resolveMachineLabel(parent, titleLookup);
    }

    private Optional<String> resolveMachineNameFromAncestors(final Node node,
                                                             final I18nLabelResolver.LabelLookup titleLookup) {
        Node current = node;
        while (current != null) {
            final String nodeType = getNodeType(current);
            if (TYPE_SUBCATEGORY.equals(nodeType) || TYPE_CATEGORY.equals(nodeType)) {
                return resolveMachineLabel(current, titleLookup);
            }
            current = getParent(current);
        }
        return Optional.empty();
    }

    private String getNodeType(final Node node) {
        try {
            return node.getPrimaryNodeType().getName();
        } catch (RepositoryException e) {
            return null;
        }
    }

    private Optional<String> resolveMachineLabel(final Node node, final I18nLabelResolver.LabelLookup titleLookup) {
        final Optional<String> localizedTitle = titleLookup.resolve(node);
        if (localizedTitle.isPresent()) {
            return localizedTitle;
        }
        try {
            return Optional.ofNullable(node.getName());
        } catch (RepositoryException e) {
            return Optional.empty();
        }
    }

    private Node getParent(final Node node) {
        try {
            return node.getParent();
        } catch (RepositoryException e) {
            return null;
        }
    }

    private CompatibleMachines emptyCompatibleMachines() {
        return createCompatibleMachinesPayload(Collections.emptyList());
    }

    private CompatibleMachines createCompatibleMachinesPayload(final Collection<String> machineNames) {
        return new CompatibleMachines(machineNames.stream().map(CompatibleMachines.Machine::new).toList());
    }
}
