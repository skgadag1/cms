package com.tbscg.doosan.magnolia.bobcat.ui.contentapp.datasource.jcr;

import com.tbscg.doosan.magnolia.bobcat.field.filter.NodeFilter;
import com.tbscg.doosan.magnolia.bobcat.ui.contentapp.ModelFilteringPredicate;
import com.tbscg.doosan.magnolia.filtering.ValidNodeTypesProvider;
import com.vaadin.data.provider.Query;
import info.magnolia.ui.contentapp.JcrDataProvider;
import info.magnolia.ui.datasource.jcr.JcrDatasource;
import info.magnolia.ui.datasource.jcr.JcrDatasourceDefinition;
import info.magnolia.ui.filter.DataFilter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.jcr.Node;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
public class JcrPrimaryTypeProvider extends JcrDataProvider {
    private static final String VALID_NODE_NAME = "jcr:primaryType";

    private final JcrDatasourceDefinition definition;
    private final ValidNodeTypesProvider validNodeTypesProvider;

    @Inject
    public JcrPrimaryTypeProvider(final JcrDatasourceDefinition definition,
                                  final JcrDatasource datasource,
                                  final ValidNodeTypesProvider validNodeTypesProvider) {
        super(definition, datasource);

        this.definition = definition;
        this.validNodeTypesProvider = validNodeTypesProvider;
    }

    @Override
    public Stream<Node> fetchFromBackEnd(final Query<Node, DataFilter> query) {
        log.debug("fetchFromBackEnd : query is {}", new QueryToStringFormatter(query));

        final boolean isFilterableDatasource = this.definition instanceof FilterableJcrDatasourceDefinition;
        log.debug("isFilterableDatasource = {}", isFilterableDatasource);

        final SetMaxLimitQueryTransformer queryTransformer = new SetMaxLimitQueryTransformer(query);

        final Set<String> validNodeTypes = findTypesByQuery(queryTransformer);
        log.debug("validNodeTypes = {}", validNodeTypes);

        final boolean shouldTransformQuery = validNodeTypes != null || isFilterableDatasource;
        log.debug("shouldTransformQuery = {}", shouldTransformQuery);

        final Query<Node, DataFilter> effectiveQuery = shouldTransformQuery ? queryTransformer.getQuery() : query;
        log.debug("effectiveQuery = {}", new QueryToStringFormatter(effectiveQuery));

        Stream<Node> nodeStream = super.fetchFromBackEnd(effectiveQuery);
        if (validNodeTypes != null) {
            nodeStream = filterNodesByTypes(nodeStream, validNodeTypes);
        }
        if (log.isDebugEnabled()) {
            log.debug("Nodes returned by query:");
            nodeStream = debugNodesInStream(nodeStream);
        }

        if (isFilterableDatasource) {
            final NodeFilter nodeFilter = ((FilterableJcrDatasourceDefinition) definition).getNodeFilter();
            log.debug("nodeFilter = {}", nodeFilter);

            final ModelFilteringPredicate modelFilteringPredicate = new ModelFilteringPredicate(nodeFilter);
            nodeStream = nodeStream.filter(modelFilteringPredicate::evaluateTyped);
            if (log.isDebugEnabled()) {
                log.debug("Nodes after filtering:");
                nodeStream = debugNodesInStream(nodeStream);
            }
        }

        return nodeStream;
    }

    private Stream<Node> debugNodesInStream(final Stream<Node> nodeStream) {
        return nodeStream
                .peek(node -> log.debug("{}", node))
                .collect(Collectors.toList())
                .stream();
    }

    private Stream<Node> filterNodesByTypes(final Stream<Node> fetchedNodes, final Set<String> validNodeTypes) {
        final List<Node> outputNodes = new LinkedList<>();
        for (final Node node : fetchedNodes.collect(Collectors.toList())) {
            try {
                final String nodeName = node.getPrimaryNodeType().getName();
                if (validNodeTypes.contains(nodeName)) {
                    outputNodes.add(node);
                }
            } catch (final Exception e) {
                log.debug("Primary node for {} not exist", node);
            }
        }
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(outputNodes.iterator(), Spliterator.ORDERED),
                false);
    }

    private Set<String> findTypesByQuery(final SetMaxLimitQueryTransformer queryTransformer) {
        if (!queryTransformer.hasProperty(VALID_NODE_NAME)) {
            return null;
        }
        final Object searchedProperty = queryTransformer.getProperty(VALID_NODE_NAME);
        queryTransformer.cropKeyFromQuery(VALID_NODE_NAME);

        final String searchingText = searchedProperty.toString();
        return this.validNodeTypesProvider.getMatchedNodesByText(searchingText);
    }

    private static final class QueryToStringFormatter {
        private final Query<Node, DataFilter> query;

        public QueryToStringFormatter(final Query<Node, DataFilter> query) {
            this.query = query;
        }

        @Override
        public String toString() {
            return "[offset=" + this.query.getOffset() + "; limit=" + this.query.getLimit() + "; filter=" + this.query.getFilter() + "]";
        }
    }
}
