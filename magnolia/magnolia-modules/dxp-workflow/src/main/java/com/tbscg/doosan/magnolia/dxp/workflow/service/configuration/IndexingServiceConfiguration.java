package com.tbscg.doosan.magnolia.dxp.workflow.service.configuration;

import com.google.inject.Inject;
import com.tbscg.doosan.magnolia.dxp.core.SitePagePath;
import com.tbscg.doosan.magnolia.dxp.workflow.DxpWorkflow;
import com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant;
import com.tbscg.doosan.magnolia.dxp.workflow.model.PageNode;
import com.tbscg.doosan.magnolia.dxp.workflow.util.PageCustomLinks;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.predicate.NodeTypePredicate;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.ATTACHMENTS_WORKSPACE;
import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.PRODUCTS_WORKSPACE;
import static com.tbscg.doosan.magnolia.dxp.workflow.util.WorkflowUtil.filterOutLockedNodes;

/**
 * Configuration class for the @{@link com.tbscg.doosan.magnolia.dxp.workflow.service.IndexingService}.
 */
public class IndexingServiceConfiguration {

    public static final String TABLE_PARTITION_KEY_NAME = "pk";
    public static final String TABLE_SORT_KEY_NAME = "sk";
    public static final String TABLE_REGION_KEY_NAME = "region";
    public static final String TABLE_LANG_KEY_NAME = "lang";
    public static final String TABLE_PAYLOAD_KEY_NAME = "payload";
    public static final String TABLE_DATE_KEY_NAME = "dateModified";
    public static final String TABLE_URLS_KEY_NAME = "urls";
    public static final String TABLE_PK_PAGE_TYPE = "page";
    public static final String INDEX_ACTION = "INDEXING";
    public static final String DEINDEX_ACTION = "DEINDEXING";
    public static final String INDEXER_SERVICE_CONFIG_PATH = "/modules/dxp-workflow/indexing";
    public static final List<String> INDEXABLE_WORKSPACE = List.of(
            WorkflowConstant.WEBSITE_WORKSPACE,
            PRODUCTS_WORKSPACE,
            ATTACHMENTS_WORKSPACE);

    private final DxpWorkflow module;

    @Inject
    public IndexingServiceConfiguration(DxpWorkflow module) {
        this.module = module;
    }

    public static boolean isIndexableWorkspace(String workspace) {
        return INDEXABLE_WORKSPACE.contains(workspace);
    }

    /**
     * Returns DynamoDB CmsIndexerTable name from SSM parameter store.
     *
     * @return DynamoDB table name used by the indexer
     */
    public String getIndexerTableName() {
        return this.module.getCmsIndexerTableName();
    }

    /**
     * Returns, from JCR, with list of sites that are covered be the indexer.
     *
     * @return list of sites that are covered be the indexer
     * @throws RepositoryException thrown when problem with reading configuration from JCR
     */
    public List<String> getIndexableSites() throws RepositoryException {
        final Session session = MgnlContext.getJCRSession(WorkflowConstant.CONFIG_WORKSPACE);
        final Property property = session.getProperty(INDEXER_SERVICE_CONFIG_PATH + "/indexableSites");
        return Arrays.stream(property.getString().split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .toList();
    }

    /**
     * Returns, from JCR, flat list of all subpages (recursively) for given root page.
     *
     * @param rootPagePath page root path to get subpages from
     * @return lat list of all subpages for given root page.
     * @throws RepositoryException thrown when problem with getting subpages from JCR
     */
    public List<PageNode> getSubpages(final String rootPagePath) throws RepositoryException {
        final List<PageNode> subpages = new ArrayList<>();
        final Session session = MgnlContext.getJCRSession(WorkflowConstant.WEBSITE_WORKSPACE);
        final Node rootNode = session.getNode(rootPagePath);
        final Iterable<Node> childNodeIterator = NodeUtil.collectAllChildren(rootNode, new NodeTypePredicate(NodeTypes.Page.NAME));
        final List<Node> nodeList = NodeUtil.asList(childNodeIterator);
        for (Node node : filterOutLockedNodes(nodeList)) {
            Instant lastModifiedTime = toInstant(com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.getLastModifiedDate(node));
            String nodeType = com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.getPrimaryNodeType(node);
            String parentId = com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.getParentNodeId(node);
            subpages.add(
                    new PageNode(node.getIdentifier(), parentId,
                            new SitePagePath(node.getCorrespondingNodePath(WorkflowConstant.WEBSITE_WORKSPACE)),
                            lastModifiedTime,
                            nodeType,
                            PageCustomLinks.byLanguage(node)));
        }
        return subpages;
    }

    private static Instant toInstant(final Calendar calendar) {
        return calendar == null ? null : calendar.toInstant();
    }
}
