package com.tbscg.doosan.magnolia.bobcat.rest.delivery;

import com.tbscg.doosan.magnolia.bobcat.BobcatModuleConfiguration;
import com.tbscg.doosan.magnolia.bobcat.custompath.CustomPathUtil;
import com.tbscg.doosan.magnolia.bobcat.custompath.RootPathAndNodeName;
import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.security.JCRSessionOp;
import info.magnolia.cms.util.PathUtil;
import info.magnolia.cms.util.QueryUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.rest.AbstractEndpoint;
import info.magnolia.rest.DynamicPath;
import info.magnolia.rest.delivery.jcr.NodesResult;
import info.magnolia.rest.delivery.jcr.filter.FilteringContentDecoratorBuilder;
import info.magnolia.rest.delivery.jcr.filter.NodeTypesPredicate;
import info.magnolia.rest.delivery.jcr.i18n.I18n;
import info.magnolia.rest.delivery.jcr.v2.ConfiguredFiltering;
import info.magnolia.rest.delivery.jcr.v2.DeliveryContext;
import info.magnolia.rest.delivery.jcr.v2.JcrDeliveryEndpointDefinition;
import info.magnolia.rest.reference.ReferenceDefinition;
import info.magnolia.rest.reference.ReferenceResolver;
import info.magnolia.rest.reference.ReferenceResolverDefinition;
import info.magnolia.rest.reference.jcr.JcrReferenceResolver;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.commons.iterator.FilteringNodeIterator;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import java.util.*;
import java.util.function.Predicate;

import static com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.tryGetParentNodeWithType;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.logging.log4j.util.Strings.isBlank;

/**
 * {@link BobcatJcrDeliveryEndpoint} overrides default Magnolia
 * {@link info.magnolia.rest.delivery.jcr.v2.JcrDeliveryEndpoint} implementation. Custom implementation supports
 * friendly URLs handling, which allows content editors to define set of aliases
 * for URLs stored in Magnolia.
 */
@DynamicPath
@Path("/")
@ConfiguredFiltering
@I18n
@Slf4j
public class BobcatJcrDeliveryEndpoint extends AbstractEndpoint<JcrDeliveryEndpointDefinition> {

    private static final String PATH_PARAM = "path";
    private static final String NODE_TYPES_PARAM = "nodeTypes";
    private static final String KEYWORD_PARAM = "q";
    private static final String ORDER_BY_PARAM = "orderBy";
    private static final String OFFSET_PARAM = "offset";
    private static final String LIMIT_PARAM = "limit";
    private static final String LANGUAGE_PARAM = "lang";
    private static final String ALL_LANGUAGE_PARAM = "all";
    private static final String VERSION = "version";
    private static final String CUSTOM_PATH = "customPath";
    private static final String SQL2_QUERY_STATEMENT = "SELECT * FROM [nt:base] WHERE customLink = '%s'";
    private static final String JCR_PAGE_TYPE = "mgnl:page";
    private static final List<String> ENDPOINT_PARAMETERS = Collections.unmodifiableList(
            Arrays.asList(NODE_TYPES_PARAM, LANGUAGE_PARAM, KEYWORD_PARAM, ORDER_BY_PARAM, OFFSET_PARAM, LIMIT_PARAM, CUSTOM_PATH));

    private final ComponentProvider componentProvider;
    private final I18nContentSupport i18nContentSupport;
    private final VersionManager versionManager;
    private final Map<Predicate, ReferenceResolver<?, ?>> resolvers;
    private final Provider<WebContext> webContextProvider;
    private final BobcatModuleConfiguration moduleConfiguration;

    @javax.ws.rs.core.Context
    private UriInfo uriInfo;

    @javax.ws.rs.core.Context
    private Providers providers;

    @Inject
    public BobcatJcrDeliveryEndpoint(
            final JcrDeliveryEndpointDefinition endpointDefinition,
            final ComponentProvider componentProvider,
            final I18nContentSupport i18nContentSupport,
            final VersionManager versionManager,
            final Provider<WebContext> webContextProvider,
            final BobcatModuleConfiguration moduleConfiguration) {
        super(endpointDefinition);
        this.componentProvider = componentProvider;
        this.i18nContentSupport = i18nContentSupport;
        this.versionManager = versionManager;
        resolvers = endpointDefinition.getReferences().stream()
                .filter(definition -> definition.getReferenceResolver() != null
                        && definition.getReferenceResolver().getImplementationClass() != null)
                .collect(toMap(this::instantiatePredicate, this::instantiateResolver));
        this.webContextProvider = webContextProvider;
        this.moduleConfiguration = moduleConfiguration;
    }

    /**
     * Returns a node including its properties and child nodes down to a certain
     * depth. Provides additional parameter
     */
    @GET
    @Path("/{path:.*(?<!@nodes)}") // Do not allow path that end with @nodes
    @Produces({MediaType.APPLICATION_JSON})
    public Node readNode(@PathParam(PATH_PARAM) @DefaultValue("/") final String path,
                         @QueryParam(VERSION) final String version,
                         @QueryParam(CUSTOM_PATH) final String customPath) throws RepositoryException {

        final JcrDeliveryEndpointDefinition definition = getEndpointDefinition();
        requireNodeTypes(definition);
        initDeliveryContext();
        final WebContext webContext = webContextProvider.get();

        final Optional<String> optJcrPath = this.getJcrPathByCustomPath(this.getEndpointDefinition().getWorkspace(), customPath);

        return doSessionOperation(definition.isBypassWorkspaceAcls(),
                new JCRSessionOp<Node>(definition.getWorkspace()) {
                    @Override
                    public Node exec(final Session session) throws RepositoryException {
                        final String rootPath = definition.getRootPath();
                        log.debug("rootPath = {}", rootPath);

                        final String nodePath = PathUtil.createPath(rootPath, optJcrPath.orElse(path));

                        Node node = session.getNode(nodePath);
                        if (StringUtils.isNotEmpty(version) && isPreviewEndpoint()) {
                            node = versionManager.getVersion(node, version);
                        }
                        final NodeTypesPredicate nodeTypesPredicate = new NodeTypesPredicate(definition.getNodeTypes(),
                                !definition.isStrict());
                        final boolean nodeTypeMatched = nodeTypesPredicate.evaluateTyped(node);
                        if (!nodeTypeMatched) {
                            log.error("Node '{}' does not match any configured node type", node);
                            throw new NotFoundException(
                                    String.format("Node '%s' does not match any configured node type", node));
                        }
                        webContext.getAggregationState().setMainContentNode(node);
                        return node;
                    }
                });
    }

    /**
     * Returns an array of nodes under a given parent path.
     */
    @GET
    @Path("/{path:(.*)}@nodes")
    @Produces({MediaType.APPLICATION_JSON})
    public NodeIterator getChildren(
            @PathParam(PATH_PARAM) @DefaultValue("") final String path,
            @QueryParam(CUSTOM_PATH) final String customPath) throws RepositoryException {

        final JcrDeliveryEndpointDefinition definition = getEndpointDefinition();
        requireNodeTypes(definition);
        initDeliveryContext();

        final WebContext webContext = webContextProvider.get();
        final Optional<String> optJcrPath = this.getJcrPathByCustomPath(this.getEndpointDefinition().getWorkspace(), customPath);

        return doSessionOperation(definition.isBypassWorkspaceAcls(),
                new JCRSessionOp<NodeIterator>(definition.getWorkspace()) {
                    @Override
                    public NodeIterator exec(final Session session) throws RepositoryException {
                        final String rootPath = definition.getRootPath();
                        log.debug("rootPath = {}", rootPath);

                        final String nodePath = PathUtil.createPath(rootPath, optJcrPath.orElse(path));

                        final Node parent = session.getNode(nodePath);

                        webContext.getAggregationState().setMainContentNode(parent);
                        return new FilteringNodeIterator(parent.getNodes(),
                                new NodeTypesPredicate(definition.getNodeTypes(), !definition.isStrict()));
                    }
                });
    }

    /**
     * Searches for nodes matching specific filters and returns a list of results.
     *
     * @param customPath - search by custom name stored in dynamodb cache.
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public NodesResult queryNodes(@QueryParam(KEYWORD_PARAM) final String keyword,
                                  @QueryParam(ORDER_BY_PARAM) final String orderByParam,
                                  @QueryParam(OFFSET_PARAM) final Long offsetParam,
                                  @QueryParam(LIMIT_PARAM) final Long limitParam,
                                  @QueryParam(CUSTOM_PATH) final String customPath) throws RepositoryException {

        final JcrDeliveryEndpointDefinition definition = getEndpointDefinition();
        requireNodeTypes(definition);
        initDeliveryContext();

        final long offset = offsetParam == null ? 0 : offsetParam;
        final long limit = limitParam == null ? definition.getLimit() : limitParam;
        final List<String> propertiesToOrderBy = StringUtils.isEmpty(orderByParam) ? Collections.emptyList()
                : Arrays.stream(StringUtils.split(orderByParam, ",")).map(String::trim).collect(toList());
        final Map<String, List<String>> filteringConditions = uriInfo.getQueryParameters().entrySet().stream()
                .filter(entry -> !ENDPOINT_PARAMETERS.contains(entry.getKey()) && !entry.getValue().isEmpty())
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        final Optional<String> jcrPath = this.getJcrPathByCustomPath(this.getEndpointDefinition().getWorkspace(), customPath);

        final Optional<RootPathAndNodeName> optRootPathAndNodeName = jcrPath
                .map(CustomPathUtil::splitToRootPathAndNodeName);
        log.debug("optRootPathAndNodeName = {}", optRootPathAndNodeName);

        final NodeIterator results = isBlank(customPath) || jcrPath.isPresent() ?
                doSessionOperation(definition.isBypassWorkspaceAcls(),
                        new JCRSessionOp<NodeIterator>(definition.getWorkspace()) {
                            @Override
                            public NodeIterator exec(final Session session) throws RepositoryException {
                                final Workspace workspaceObj = session.getWorkspace();
                                final Query query = BobcatQueryBuilder.inWorkspace(workspaceObj)
                                        .rootPath(optRootPathAndNodeName.map(RootPathAndNodeName::getRootPath).orElse(null))
                                        .name(optRootPathAndNodeName.map(RootPathAndNodeName::getNodeName).orElse(null))
                                        .strict(definition.isStrict())
                                        .nodeTypes(definition.getNodeTypes())
                                        .keyword(keyword)
                                        .conditions(filteringConditions)
                                        .orderBy(propertiesToOrderBy)
                                        .offset(offset)
                                        .limit(limit).build();

                                log.debug("query = {}", query);

                                final QueryResult result = query.execute();
                                return result.getNodes();
                            }
                        })
                : new EmptyNodeIterator();
        // TODO total number of entries in result will be set later.
        return new NodesResult(results, new Long(0),offset,limit);
    }

    private Optional<String> getJcrPathByCustomPath(final String workspace, final String customPath) {
        if (moduleConfiguration.isCustomPathResolutionEnabled()) {
            final String statement = String.format(SQL2_QUERY_STATEMENT, customPath);
            try {
                NodeIterator queryResult = QueryUtil.search(workspace, statement, "JCR-SQL2");
                if (queryResult != null) {
                    return NodeUtil.getCollectionFromNodeIterator(queryResult).stream()
                            .findFirst()
                            .map(node -> tryGetParentNodeWithType(node, JCR_PAGE_TYPE)
                                    .map(NodeUtil::getPathIfPossible)
                                    .filter(StringUtils::isNotEmpty))
                            .orElse(Optional.empty());
                }
            } catch (RepositoryException e) {
                log.error("Error while querying jcr by customPath, e");
            }
        }
        return Optional.empty();
    }

    private void requireNodeTypes(final JcrDeliveryEndpointDefinition definition) {
        if (definition.getNodeTypes() == null) {
            throw new NotFoundException(
                    String.format("%s are configured as null, not serving any content", NODE_TYPES_PARAM));
        } else if (definition.getNodeTypes().isEmpty()) {
            throw new NotFoundException(
                    String.format("%s are configured as empty, not serving any content", NODE_TYPES_PARAM));
        }
    }

    private <R> R doSessionOperation(final boolean bypassWorkspaceAcls, final MgnlContext.Op<R, RepositoryException> operation)
            throws RepositoryException {
        if (bypassWorkspaceAcls) {
            return MgnlContext.doInSystemContext(operation);
        }

        return operation.exec();
    }

    private void initDeliveryContext() {
        final ContextResolver<DeliveryContext> contextResolver = providers.getContextResolver(DeliveryContext.class,
                MediaType.WILDCARD_TYPE);
        final DeliveryContext context = contextResolver.getContext(DeliveryContext.class);
        if (context != null) {
            context.setEndpointDefinition(getEndpointDefinition());
            context.setResolvers(resolvers);
        }
    }

    private Predicate<Item> instantiatePredicate(final ReferenceDefinition definition) {
        return item -> {
            try {
                final Node node = item.isNode() ? (Node) item : item.getParent();
                final boolean matchNodeType = definition.getNodeType() == null || node.isNodeType(definition.getNodeType());
                final boolean matchItemName = definition.getPropertyName() != null
                        && item.getName().matches(definition.getPropertyName());
                return matchNodeType && matchItemName;
            } catch (final RepositoryException e) {
                log.warn("Cannot get information from item {}:", item, e);
            }

            return false;
        };
    }

    private ReferenceResolver<?, ?> instantiateResolver(final ReferenceDefinition definition) {
        final ReferenceResolverDefinition resolverDefinition = definition.getReferenceResolver();
        final ReferenceResolver<?, ?> resolver = componentProvider.newInstance(resolverDefinition.getImplementationClass(),
                resolverDefinition);
        if (JcrReferenceResolver.class.isAssignableFrom(resolver.getClass())) {
            return new BobcatJcrDeliveryEndpoint.JcrReferenceResolverWrapper((JcrReferenceResolver) resolver,
                    getEndpointDefinition().isIncludeSystemProperties(), getEndpointDefinition().getSystemProperties(),
                    getEndpointDefinition().isBypassWorkspaceAcls(), i18nContentSupport);
        }
        return resolver;
    }

    private boolean shouldSupportI18n() {
        return !ALL_LANGUAGE_PARAM.equalsIgnoreCase(uriInfo.getQueryParameters().getFirst(LANGUAGE_PARAM));
    }

    private boolean isPreviewEndpoint() {
        return uriInfo.getPath().contains("/preview/");
    }

    private static class EmptyNodeIterator implements NodeIterator {
        @Override
        public Node nextNode() {
            return null;
        }

        @Override
        public void skip(final long skipNum) {
        }

        @Override
        public long getSize() {
            return 0;
        }

        @Override
        public long getPosition() {
            return 0;
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            return null;
        }
    }

    /**
     * A simple wrapper around the ReferenceResolver, to forward the endpoint
     * settings for returned JCR Nodes (e.g. inclusion of system properties, bypass
     * of workspace ACLs).
     */
    private class JcrReferenceResolverWrapper implements ReferenceResolver<String, Object> {
        private final JcrReferenceResolver resolver;
        private final boolean includeSystemProperties;
        private final List<String> systemProperties;
        private final boolean useSystemContext;
        private final I18nContentSupport i18nContentSupport;

        private JcrReferenceResolverWrapper(final JcrReferenceResolver resolver, final boolean includeSystemProperties,
                                            final List<String> systemProperties, final boolean useSystemContext, final I18nContentSupport i18nContentSupport) {
            this.resolver = resolver;
            this.includeSystemProperties = includeSystemProperties;
            this.systemProperties = systemProperties;
            this.useSystemContext = useSystemContext;
            this.i18nContentSupport = i18nContentSupport;
        }

        @Override
        public Optional<Object> resolve(final String value) {
            Optional<Object> resolvedItem;
            try {
                resolvedItem = doSessionOperation(useSystemContext, () -> resolver.resolve(value));
            } catch (final RepositoryException e) {
                BobcatJcrDeliveryEndpoint.log.warn("Cannot resolve referenced item {}:", value, e);
                resolvedItem = empty();
            }

            if (resolvedItem.isPresent() && resolvedItem.get() instanceof Node) {
                final FilteringContentDecoratorBuilder filteringBuilder = new FilteringContentDecoratorBuilder().depth(-1)
                        .includeSystemProperties(includeSystemProperties).systemProperties(systemProperties)
                        .supportI18n(shouldSupportI18n() ? i18nContentSupport : null);

                return resolvedItem.map(Node.class::cast).map(filteringBuilder::wrapNode);
            }

            return resolvedItem;
        }
    }
}
