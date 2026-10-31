package com.tbscg.doosan.magnolia.product.catalog.extimport;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.tbscg.doosan.magnolia.product.catalog.extimport.buildandquote.BuildAndQuoteCategory;
import com.tbscg.doosan.magnolia.product.catalog.extimport.buildandquote.BuildAndQuoteService;
import com.tbscg.doosan.magnolia.product.catalog.extimport.gpim.GpimProduct;
import com.tbscg.doosan.magnolia.product.catalog.extimport.pimpp.PimppProduct;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.predicate.NodeTypePredicate;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.FailableCallable;
import org.apache.jackrabbit.JcrConstants;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.groupingBy;

/**
 * Default implementation for {@link ProductImportService}.
 */
@Slf4j
@AllArgsConstructor
public class DefaultProductImportService implements ProductImportService {

    private static final String DXP_CATEGORY_TYPE_NAME = "dxp:category";
    private static final String DXP_PRODUCT_TYPE_NAME = "dxp:product";
    private static final String DXP_PRODUCT_GROUP_TYPE_NAME = "dxp:productGroup";
    private static final String DXP_ATTACHMENT_CATEGORY_TYPE_NAME = "dxp:attachmentCategory";
    private static final String DXP_ATTACHMENT_GROUP_TYPE_NAME = "dxp:attachmentGroup";
    private static final String DXP_ATTACHMENT_TYPE_NAME = "dxp:attachment";
    private static final String PRODUCTS_WORKSPACE = "products";
    private static final String MAGNOLIA_CONTENT_NODE = "mgnl:contentNode";
    private static final String MAGNOLIA_FOLDER = "mgnl:folder";
    private static final String GPIM_PRODUCT = "GpimProduct";
    private static final String EMPTY_STRING = "";
    private static final String GPIM_UNNAMED = "no_gpim_name";

    private final BuildAndQuoteService buildAndQuoteService;
    private final RegionsService regionsService;

    private final ImmutableMap<String, String> categoryNameMapping = ImmutableMap.of(
            "Compact Excavators", "Excavators",
            "Mini Track Loaders", "Mini Loaders",
            "Toolcat&#153; Work Machine", "Utility Work Machine",
            "Telehandler", "VersaHandler TTC"
    );

    private static void mapSubCategory(final PimCategory subCategory, final Node subcategoryNode, final String buildAndQuoteId) throws RepositoryException {
        subcategoryNode.setProperty("title", subCategory.getName());
        subcategoryNode.setProperty("code", subCategory.getCode());
        subcategoryNode.setProperty("id", buildAndQuoteId);
    }

    private static String sanitizeNodeName(final String nodeName) {
        return nodeName.trim().replaceAll("[^\\w_]+", "-");
    }

    private static String sanitizeNodeName(final String nodeName, final String gpimCode) {
        if (nodeName != null) {
            return sanitizeNodeName(nodeName);
        }
        return GPIM_UNNAMED + "#" + gpimCode;
    }

    private void importModelsWithSameName(final Node parentNode,
                                          final String modelName,
                                          final List<? extends PimModel> prodList,
                                          final String buildAndQuoteID,
                                          final String dxpName,
                                          final Multimap<String, String> productsIdMap,
                                          final Boolean allowCategoryCreation
    ) {
        final boolean applySuffix = prodList.size() > 1;
        final Node regionNode = getRegionNode(parentNode);
        if (Objects.isNull(regionNode)) {
            return;
        }

        int i = 1;
        for (final PimModel pimModel : prodList) {
            try {
                final String effectiveName = applySuffix ? modelName + i++ : modelName;
                log.debug("Importing model: '{}' as '{}'", modelName, effectiveName);

                final Node modelNode = getOrCreateModelNode(parentNode, regionNode, pimModel, effectiveName, dxpName);
                mapModel(pimModel, modelNode, buildAndQuoteID, productsIdMap, allowCategoryCreation);
            } catch (final Exception e) {
                log.error("Failed to add product node for {}", modelName, e);
            }
        }
    }

    private Node getRegionNode(Node parentNode) {
        try {
            if (MAGNOLIA_FOLDER.equals(parentNode.getPrimaryNodeType().getName()) && this.regionsService.isRegion(parentNode.getPath())) {
                return parentNode;
            }
            Node currentParent = NodeUtil.getNearestAncestorOfType(parentNode, MAGNOLIA_FOLDER);

            if (this.regionsService.isRegion(currentParent.getPath())) {
                return currentParent;
            }
        } catch (Exception repositoryException) {
            log.error("Failed to get Region Root for {}", parentNode);
        }
        return null;
    }

    private static Node getOrCreateNode(final Node rootNode, final String nodeName, final String typeName) throws ProductImportException {
        try {
            if (rootNode.hasNode(nodeName)) {
                log.debug("Node '{}' already exists.", nodeName);
                return rootNode.getNode(nodeName);
            }

            log.debug("Creating node '{}'.", nodeName);
            Node newNode = rootNode.addNode(nodeName, typeName);
            return newNode;
        } catch (final RepositoryException e) {
            throw new ProductImportException("Failed to get or create node with name: '" + nodeName + "' and type: '" + typeName + "'", e);
        }
    }

    private static Node getOrCreateModelNode(final Node rootNode, final Node regionRoot, PimModel pimModel, final String nodeName, final String typeName) throws ProductImportException {
        try {
            Iterable<Node> nodes = NodeUtil.collectAllChildren(regionRoot, new NodeTypePredicate(typeName));

            return StreamSupport.stream(nodes.spliterator(), false)
                    .filter((node) -> PropertyUtil.getString(node, "id", "").equals(pimModel.getCode()))
                    .findFirst()
                    .orElseGet(() -> com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.addNode(rootNode, nodeName, typeName));
        } catch (final RepositoryException e) {
            throw new ProductImportException("Failed to get or create node with id: '" + pimModel.getCode() + "' and type: '" + typeName + "'", e);
        }
    }

    private static Node getOrCreateGroupNode(final Node rootNode, final String nodeName, final String typeName) throws ProductImportException {
        try {
            final Node node = getOrCreateNode(rootNode, nodeName, typeName);

            if (node.isNodeType(typeName)) {
                return node;
            }

            final String tempPath = node.getPath() + "-temp";
            final Session session = node.getSession();
            session.move(node.getPath(), tempPath);
            final Node group = rootNode.addNode(nodeName, typeName);
            session.move(tempPath, group.getPath() + "/" + nodeName);

            return group;
        } catch (final RepositoryException e) {
            throw new ProductImportException("Failed to get or create group node with name: '" + nodeName + "' and type: '" + typeName + "'", e);
        }
    }

    private static Multimap<String, String> getProducts(final Node rootNode, final PimCatalog pimCatalog) throws RepositoryException {
        final Multimap<String, String> productsIdMap = ArrayListMultimap.create();
        for (final PimCategory category : pimCatalog.getCategories()) {
            log.debug("Category Name : {} ", category.getName());
            final NodeIterator subCategoryIterator = rootNode.getNodes();

            while (subCategoryIterator.hasNext()) {
                final Node subCategory = subCategoryIterator.nextNode();
                if (subCategory.hasProperty("title") && StringUtils.equalsIgnoreCase(subCategory.getProperty("title").getString(), category.getName())) {
                    continue;
                }
                unwrapProducts(productsIdMap, subCategory);
            }
        }
        return productsIdMap;
    }

    private static void unwrapProducts( Multimap<String, String> productsIdMap, Node node) throws RepositoryException {
        final NodeIterator nodeIterator = node.getNodes();
        if(!nodeIterator.hasNext()) {
            return;
        }

        while (nodeIterator.hasNext()) {
            final Node subNode = nodeIterator.nextNode();
            getNodeProperties(productsIdMap, subNode);
            unwrapProducts(productsIdMap, subNode);
        }
    }

    private static void getNodeProperties(final Multimap<String, String> productsIdMap, final Node node) throws RepositoryException {
        final String contentType = node.getProperty(JcrConstants.JCR_PRIMARYTYPE).getString();
        log.debug("primary Type : {}", contentType);
        if (!(StringUtils.equalsIgnoreCase(contentType, DXP_PRODUCT_TYPE_NAME) && node.hasProperty("id") && node.hasProperty(JcrConstants.JCR_UUID))) {
            return;
        }
        if (node.hasProperty("title")) {
            log.debug("Product {} Added", node.getProperty("title").getString());
        }
        final String id = node.getProperty("id").getString();
        final String uuid = node.getProperty(JcrConstants.JCR_UUID).getString();
        productsIdMap.put(id, uuid);
    }

    private static void mapModel(final PimModel pimModel, final Node modelNode,
                                 final String buildAndQuoteID, final Multimap<String, String> productsIdMap,
                                 final Boolean allowCategoryCreation ) throws RepositoryException {
        modelNode.setProperty("id", pimModel.getCode());
        modelNode.setProperty("category", buildAndQuoteID);
        modelNode.setProperty("lifecycle", pimModel.getLifecycle());
        setValueIfIsEmpty(modelNode, pimModel.getName(), "title");

        if(!(pimModel instanceof PimppProduct)) {
            modelNode.setProperty("yearBuilt", pimModel.getYearOfProduction());
            modelNode.setProperty("yearEnd", pimModel.getYearOfProductionEnd());
        }

        final String pimType = pimModel.getClass().getSimpleName();
        final Boolean isGpim = pimType.equals(GPIM_PRODUCT);
        log.debug("PIM Type : {}", pimType);

        if (!isGpim || allowCategoryCreation) {
            return;
        }
        final List<List<String>> compatibleProductDetails = pimModel.getCompatibleCarriers().stream()
                .map(GpimProduct::getCode)
                .map(productsIdMap::get).map(ArrayList::new)
                .filter(e -> !e.isEmpty())
                .collect(Collectors.toList());

        final int productCount = compatibleProductDetails.size();
        log.debug("Compatible products count : {}", productCount);
        if (productCount > 0) {

            int counter = 0;
            final Node attachment = getOrCreateNode(modelNode, PRODUCTS_WORKSPACE, MAGNOLIA_CONTENT_NODE);
            for (List<String> productIds : compatibleProductDetails) {

                for (String productId : productIds) {
                    Node product = getOrCreateNode(attachment, PRODUCTS_WORKSPACE + (counter), MAGNOLIA_CONTENT_NODE);
                    product.setProperty("product", productId);
                    counter++;
                }
            }
        }
    }

    private static void setValueIfIsEmpty(Node modelNode, String value, String prop) throws RepositoryException {
        if (!modelNode.hasProperty(prop)) {
            modelNode.setProperty(prop, value);
        }
    }

    private static void mapCategory(final PimCategory pimCategory, final Node categoryNode) throws RepositoryException {
        categoryNode.setProperty("title", pimCategory.getName());
        categoryNode.setProperty("description", pimCategory.getDescription());
    }

    @Override
    public void importExternalCatalog(final FailableCallable<PimCatalog, Exception> catalogSupplier,
                                      final FailableCallable<PimCatalog, Exception> relationsSupplier,
                                      final String importPath,
                                      final String workspace) throws Exception {
        final Session session = MgnlContext.getJCRSession(workspace);
        final Node rootNode = session.getNode(importPath);

        final PimCatalog pimCatalog = catalogSupplier.call();

        final boolean isImportingProducts = workspace.equals(PRODUCTS_WORKSPACE);
        final String categoryNodeTypeName = isImportingProducts ? DXP_CATEGORY_TYPE_NAME : DXP_ATTACHMENT_CATEGORY_TYPE_NAME;
        final String groupNodeTypeName = isImportingProducts ? DXP_PRODUCT_GROUP_TYPE_NAME : DXP_ATTACHMENT_GROUP_TYPE_NAME;
        final String modelNodeTypeName = isImportingProducts ? DXP_PRODUCT_TYPE_NAME : DXP_ATTACHMENT_TYPE_NAME;
        
        Multimap<String, String> productsIdMap = ArrayListMultimap.create();

        if (!isImportingProducts) {
            final Session relatedProductsSession = MgnlContext.getJCRSession(PRODUCTS_WORKSPACE);
            final Node prodRootNode = relatedProductsSession.getNode(importPath);
            productsIdMap = getProducts(prodRootNode, pimCatalog);
        }

        for (final PimCategory category : pimCatalog.getCategories()) {
            importCategory(rootNode, category, categoryNodeTypeName, groupNodeTypeName, modelNodeTypeName,
                    productsIdMap);
        }

        session.save();
    }

    private void importCategory(final Node rootNode, final PimCategory pimCategory,
                                final String categoryNodeTypeName, final String groupNodeTypeName, 
                                final String modelNodeTypeName,
                                final Multimap<String, String> productsIdMap
    ) throws RepositoryException {
        log.debug("Importing category: {}", pimCategory.getName());

        final String categoryName = sanitizeNodeName(pimCategory.getName());
        log.debug("Sanitized category name: {}", categoryName);

        final Node categoryNode = getOrCreateNode(rootNode, categoryName, categoryNodeTypeName);
        mapCategory(pimCategory, categoryNode);

        for (final PimCategory subCategory : pimCategory.getSubCategories()) {
            importSubCategory(categoryNode, subCategory, groupNodeTypeName, modelNodeTypeName,
                    productsIdMap);
        }
    }

    private void importSubCategory(final Node importCategoryNode,
                                   final PimCategory subCategory,
                                   final String groupNodeTypeName,
                                   final String modelNodeTypeName,
                                   final Multimap<String, String> productsIdMap) throws RepositoryException {
        if (CollectionUtils.isEmpty(subCategory.getProducts()) &&
                CollectionUtils.isEmpty(subCategory.getProductGroups()) &&
                CollectionUtils.isEmpty(subCategory.getSubCategories())) {
            log.debug("Skipping empty subcategory: {}", subCategory.getName());
            return;
        }

        log.debug("Importing sub-category/group: {}", subCategory.getName());

        final String subCategoryName = sanitizeNodeName(subCategory.getName());
        log.debug("Sanitized sub-category/group name: {}", subCategoryName);

        // For products workspace: creates dxp:subcategory
        // For attachments workspace: creates dxp:attachmentGroup
        final Node subCategoryNode = getOrCreateNode(importCategoryNode, subCategoryName, groupNodeTypeName);

        final String buildAndQuoteID = getBuildAndQuoteID(subCategory);
        mapSubCategory(subCategory, subCategoryNode, buildAndQuoteID);

        subCategory.getProducts().stream()
                .collect(groupingBy(pimProduct -> sanitizeNodeName(pimProduct.getName(), pimProduct.getCode())))
                .forEach((name, prodList) -> importModelsWithSameName(subCategoryNode, name, prodList, buildAndQuoteID, modelNodeTypeName,
                            productsIdMap, true));

        final Stream<DefaultProductGroup> productsFromSubcategoriesStream = subCategory.getSubCategories().stream()
                .map(this::categoryToProductGroup)
                .filter(this::groupHasProducts);

        final Stream<? extends PimGroup> productGroupStream = subCategory.getProductGroups().stream();

        Stream.concat(productGroupStream, productsFromSubcategoriesStream)
                .forEach(group -> importGroup(subCategoryNode, group, groupNodeTypeName, modelNodeTypeName,
                            productsIdMap, true, buildAndQuoteID));
    }

    private boolean groupHasProducts(PimGroup productGroup) {
        return !productGroup.getProducts().isEmpty();
    }

    private DefaultProductGroup categoryToProductGroup(PimCategory pimCategory) {
        final Stream<? extends PimModel> productsStream = pimCategory.getProducts().stream();
        final Stream<? extends PimModel> productsFromSubGroups = pimCategory.getProductGroups().stream()
                .flatMap(pimGroup -> pimGroup.getProducts().stream());

        return DefaultProductGroup.builder()
                .groupName(pimCategory.getName())
                .products(Stream.concat(productsStream, productsFromSubGroups).collect(Collectors.toList()))
                .build();
    }

    private void importGroup(final Node importSubCategoryNode,
                             final PimGroup pimGroup,
                             final String groupNodeTypeName,
                             final String modelNodeTypeName,
                             final Multimap<String, String> productsIdMap,
                             final Boolean allowCategoryCreation,
                             final String buildAndQuoteID) {
        if (pimGroup.getProducts() == null) {
            return;
        }

        try {
            log.debug("Importing group: {}", pimGroup.getGroupName());

            final String pimGroupName = sanitizeNodeName(EMPTY_STRING.equals(pimGroup.getGroupName()) ? "-" : pimGroup.getGroupName());
            log.debug("Sanitized sub-category name: {}", pimGroupName);

            final Node groupNode = getOrCreateGroupNode(importSubCategoryNode, pimGroupName, groupNodeTypeName);
            setValueIfIsEmpty(groupNode, pimGroup.getGroupName(), "title");

            pimGroup.getProducts().stream()
                    .collect(groupingBy(pimProduct -> sanitizeNodeName(pimProduct.getName(), pimProduct.getCode())))
                    .forEach((name, prodList) -> importModelsWithSameName(groupNode, name,
                            prodList, buildAndQuoteID, modelNodeTypeName,
                            productsIdMap, allowCategoryCreation));
            if (!groupNode.hasNodes()) {
                groupNode.remove();
            }
        } catch (final RepositoryException e) {
            throw new ProductImportException("Failed to import group: '" + pimGroup.getGroupName() + "'", e);
        }
    }

    private String getBuildAndQuoteID(final PimCategory subCategory) {
        return buildAndQuoteService.getCategoryByName(getBuildAndQuoteName(subCategory))
                .map(BuildAndQuoteCategory::getProdCategoryId)
                .orElse("");
    }

    private String getBuildAndQuoteName(final PimCategory subCategory) {
        return Optional.ofNullable(categoryNameMapping.get(subCategory.getName())).orElse(subCategory.getName());
    }
}
