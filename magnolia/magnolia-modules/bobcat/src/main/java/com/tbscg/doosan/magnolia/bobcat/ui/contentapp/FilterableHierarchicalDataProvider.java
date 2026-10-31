package com.tbscg.doosan.magnolia.bobcat.ui.contentapp;

import com.machinezoo.noexception.Exceptions;
import com.tbscg.doosan.magnolia.bobcat.field.filter.NodeFilter;
import com.tbscg.doosan.magnolia.bobcat.ui.contentapp.datasource.jcr.FilterableJcrDatasourceDefinition;
import com.vaadin.data.provider.HierarchicalQuery;
import info.magnolia.cms.util.QueryUtil;
import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.iterator.FilteringNodeIterator;
import info.magnolia.jcr.iterator.FilteringPropertyIterator;
import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.jcr.predicate.JCRMgnlPropertyHidingPredicate;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.contentapp.HierarchicalJcrDataProvider;
import info.magnolia.ui.datasource.jcr.JcrDatasource;
import info.magnolia.ui.datasource.jcr.JcrDatasourceDefinition;
import info.magnolia.ui.filter.DataFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.commons.iterator.NodeIteratorAdapter;
import org.apache.jackrabbit.commons.iterator.RangeIteratorAdapter;

import javax.jcr.*;
import java.util.*;
import java.util.stream.Stream;

import static com.tbscg.doosan.magnolia.bobcat.ui.contentapp.JcrDataProviderUtils.streamJcrItems;


@Slf4j
public class FilterableHierarchicalDataProvider extends HierarchicalJcrDataProvider {

    private final JcrDatasourceDefinition datasourceDefinition;
    private final JcrDatasource datasource;
    private final NodeFilter nodeFilter;

    public FilterableHierarchicalDataProvider(final JcrDatasourceDefinition definition, final JcrDatasource datasource) {
        super(definition, datasource);

        this.datasourceDefinition = definition;
        this.datasource = datasource;

        if (datasourceDefinition instanceof FilterableJcrDatasourceDefinition) {
            this.nodeFilter = ((FilterableJcrDatasourceDefinition) this.datasourceDefinition).getNodeFilter();
        } else {
            this.nodeFilter = null;
        }
    }

    @Override
    public boolean hasChildren(final Item item) {
        return getChildren(item).findAny().isPresent();
    }

    @Override
    protected Stream<Item> fetchChildrenFromBackEnd(final HierarchicalQuery<Item, DataFilter> query) {
        return this.nodeFilter != null && this.nodeFilter.isPrefilterQuery() ? getChildrenWithPrefilter(getRoot(query)) : getChildren(getRoot(query))
                .skip(query.getOffset())
                .limit(query.getLimit());
    }

    private Item getRoot(final HierarchicalQuery<Item, DataFilter> query) {
        return Optional.ofNullable(query.getParent()).orElseGet(this::getRootNode);
    }

    private Item getRootNode() {
        try {
            return datasource.getJCRSession().getNode(datasourceDefinition.getRootPath());
        } catch (final RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    private Stream<Item> getChildrenWithPrefilter(final Item item) {
        Stream<? extends Item> stream = Stream.empty();
        try {
            if (item.isNode()) {
                final Node node = (Node) item;

                boolean skipSubqueryExecution = shouldRunPrefilterQuery(node.getPath());
                final String WORKSPACE = this.datasourceDefinition.getWorkspace();

                NodeIterator preselectedPaths = skipSubqueryExecution
                        ? new NodeIteratorAdapter(RangeIteratorAdapter.EMPTY)
                        : QueryUtil.search(WORKSPACE, buildPrefilterQueryStatement(this.nodeFilter.getPropertyValue(), item.getPath()));

                final NodeIterator filteredNodes = new FilteringNodeIterator(
                        new NodeIteratorAdapter(this.datasource.wrapNodeIterator(QueryUtil.search(WORKSPACE, buildBaseQueryStatement(item.getPath())))),
                        new JcrDataProviderUtils.NodeTypeFilteringPredicate(datasourceDefinition)
                                .and(new ModelFilteringPrefilteredPathsPredicate(nodeFilter, preselectedPaths, skipSubqueryExecution)));

                stream = streamJcrItems(filteredNodes);
                stream = Stream.concat(stream, getProperties(node));
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return (Stream<Item>) stream;
    }

    private Stream<Item> getChildren(final Item item) {
        Stream<? extends Item> stream = Stream.empty();
        if (item.isNode()) {
            final Node node = (Node) item;

            // Always filter out system nodes
            final Iterator<Node> nodeIterable = Exceptions.wrap().get(() -> NodeUtil.getNodes(node, new JcrDataProviderUtils.JcrMgnlNodeHidingPredicate(datasourceDefinition.isIncludeSystemNodes())).iterator());
            final NodeIterator filteredNodes = new FilteringNodeIterator(
                    new NodeIteratorAdapter(nodeIterable),
                    new JcrDataProviderUtils.NodeTypeFilteringPredicate(datasourceDefinition)
                            .and(nodeFilter != null ? new ModelFilteringPredicate(nodeFilter) : AbstractPredicate.alwaysTrue()));

            stream = streamJcrItems(filteredNodes);
            stream = Stream.concat(stream, getProperties(node));
        }
        return (Stream<Item>) stream;
    }

    private boolean shouldIncludeSystemProperties() {
        return datasourceDefinition.isIncludeSystemProperties();
    }

    private Stream<Property> getProperties(final Node node) {
        if (datasourceDefinition.isIncludeProperties()) {
            final Stream<Property> properties = streamJcrItems(Exceptions.wrap().get(() -> new FilteringPropertyIterator(node.getProperties(), shouldIncludeSystemProperties() ? new JcrDataProviderUtils.AlwaysTruePredicate() : new JCRMgnlPropertyHidingPredicate())));
            return properties.sorted(Comparator.comparing(Exceptions.wrap().function(Item::getName)));
        }

        return Stream.empty();
    }

    private String buildBaseQueryStatement(String path) {
        String resourceType = shouldRunPrefilterQuery(path) ? "nt:base" : "mgnl:folder";

        return StringUtils
                .replace("SELECT p.* from [{from}] as p WHERE ISCHILDNODE('{path}') ORDER BY p.[jcr:created]", "{path}", path)
                .replace("{from}", resourceType);
    }

    private String buildPrefilterQueryStatement(String filterName, String parentPath) {
        final String QUERY_STATEMENT = "SELECT child.* FROM [{from}] AS child WHERE child.lifecycle IS NOT NULL AND CONTAINS(child.*, '{filter}') AND ISDESCENDANTNODE('{parentPath}')";

        return StringUtils
                .replace(QUERY_STATEMENT, "{filter}", filterName)
                .replace("{parentPath}", parentPath)
                .replace("{from}", this.nodeFilter.getNodeType());
    }

    private boolean shouldRunPrefilterQuery(String path) {
        return path.split("/").length >= 3;
    }
}
