package com.tbscg.doosan.magnolia.dxp.workflow.util;

import com.machinezoo.noexception.Exceptions;
import com.tbscg.doosan.magnolia.dxp.workflow.model.ModelIndexedItem;
import com.tbscg.doosan.magnolia.dxp.workflow.model.NodeDetails;
import com.tbscg.doosan.magnolia.dxp.workflow.model.ProductIndexedItem;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.NodeTypes.Category;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.NodeTypes.Product;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.NodeTypes.ProductGroup;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.NodeTypes.SubCategory;
import info.magnolia.cms.util.QueryUtil;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import lombok.extern.slf4j.Slf4j;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.Properties.ID;
import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.Properties.LIFECYCLE;
import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.RelatedPage.NODE_NAME;
import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.RelatedPage.PATH;

@Slf4j
public class IndexedItemUtil {
    private static final String JCR_SQL_2 = "JCR-SQL2";
    private static final String SQL2_QUERY_STATEMENT = "SELECT * FROM [nt:base] AS model" +
            " WHERE ISDESCENDANTNODE(model , '/')" +
            " AND NAME() = '" + NODE_NAME + "'" +
            " AND CONTAINS([" + PATH + "], '%s')";

    public static Collection<Node> findAssociatedModelsForPage(final String pagePath, final String workspace, final String returnItemType) {
        final String statement = String.format(SQL2_QUERY_STATEMENT, pagePath);
        log.debug("Searching for associated models for page {} by query {}", pagePath, statement);
        try {
            final NodeIterator searchResult = QueryUtil.search(workspace, statement, JCR_SQL_2, returnItemType);
            log.debug("Searching for associated models size : {}", searchResult.getSize());
            return NodeUtil.getCollectionFromNodeIterator(searchResult);
        } catch (RepositoryException e) {
            log.warn("Cannot find associated models for page {} by query {}", pagePath, statement);
            return Collections.emptyList();
        }
    }

    public static ModelIndexedItem convertProductToIndexedItem(Node node, Boolean isInnerProduct, Function<Node, Map<String, String>> extractUrls) throws RepositoryException {
        String uuid = PropertyUtil.getString(node, "jcr:uuid");
        String path = NodeUtil.getNodePathIfPossible(node);
        String lifecycle = PropertyUtil.getString(node, LIFECYCLE);
        long lastModified = com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.getLastModifiedDate(node).getTimeInMillis();
        Node parent = node.getParent();
        Node grandParent = parent.getParent();
        Map<String, String> urls = null;
        if (!isInnerProduct && node.hasNode(NODE_NAME)) {
            urls = extractUrls.apply(node);
        }
        String group = null;
        if (isProductGroup(parent)) {
            group = parent.getName();
            parent = grandParent;
            grandParent = grandParent.getParent();
        }
        if (grandParent != null && isCategory(grandParent)) {
            NodeDetails subCategoryDetails = getNodeDetails(parent);
            NodeDetails categoryDetails = getNodeDetails(grandParent);
            return new ProductIndexedItem(uuid, path, lifecycle, lastModified, urls, categoryDetails, subCategoryDetails, group);
        }
        NodeDetails categoryDetails = getNodeDetails(parent);
        return new ProductIndexedItem(uuid, path, lifecycle, lastModified, urls, categoryDetails, null, group);
    }

    /**
     * Orders nodes by primary type so that the deepest items in the catalog hierarchy come first:
     * Product, ProductGroup, SubCategory, Category, then any other type. The previous
     * implementation was asymmetric (e.g. {@code compare(productA, productB) == compare(productB, productA) == -1})
     * which violates {@link Comparator}'s contract and produced order-dependent results under sort.
     */
    public static final Comparator<Node> nodesTypeComparator = Comparator.comparingInt(node ->
            typeRank(com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.getPrimaryNodeType(node)));

    private static int typeRank(final String type) {
        if (type == null) {
            return 4;
        }
        return switch (type) {
            case Product.NAME -> 0;
            case ProductGroup.NAME -> 1;
            case SubCategory.NAME -> 2;
            case Category.NAME -> 3;
            default -> 4;
        };
    }

    public static final BiFunction<List<Node>, Node, List<Node>> nodesAccumulator = (List<Node> acc, Node n) ->
    {
        if (acc.stream().noneMatch(accNode -> Exceptions.wrap().get(() -> com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.isAncestor(accNode, n)))) {
            acc.add(n);
        }
        return acc;
    };

    public static final BinaryOperator<List<Node>> nodeCombiner = (acc1, acc2) ->
    {
        acc1.addAll(acc2);
        return acc1;
    };

    private static boolean isCategory(Node node) {
        return com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.getPrimaryNodeType(node).equals(Category.NAME);
    }

    private static boolean isProductGroup(Node node) {
        return com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.getPrimaryNodeType(node).equals(ProductGroup.NAME);
    }

    public static NodeDetails getNodeDetails(Node node) throws RepositoryException {
        String uuid = PropertyUtil.getString(node, "jcr:uuid");
        String path = NodeUtil.getNodePathIfPossible(node);
        String id = PropertyUtil.getString(node, "id");
        Map<String, String> urls = null;
        if (node.hasNode(NODE_NAME)) {
            urls = ModelPageAssociationUtil.extractUrls(node);
        }
        return new NodeDetails(uuid, path, node.getName(), id, urls);
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}
