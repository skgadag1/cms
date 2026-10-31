package com.tbscg.doosan.magnolia.bobcat.ui.contentapp.datasource.jcr;

import com.vaadin.data.provider.HierarchicalQuery;
import com.vaadin.data.provider.Query;
import info.magnolia.ui.filter.DataFilter;

import javax.jcr.Node;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class SetMaxLimitQueryTransformer {

    private final Query<Node, DataFilter> query;

    public SetMaxLimitQueryTransformer(final Query<Node, DataFilter> query) {
        DataFilter filter = query.getFilter().map(this::createNewFilter).orElseGet(DataFilter::new);
        this.query = buildQuery(query, filter);
    }

    private Query<Node, DataFilter> buildQuery(Query<Node, DataFilter> query, DataFilter filter) {
        if (query instanceof HierarchicalQuery) {
            return new HierarchicalQuery<>(query.getOffset(), Integer.MAX_VALUE, query.getSortOrders(), query.getInMemorySorting(), filter, ((HierarchicalQuery<Node, DataFilter>) query).getParent());
        } else {
            return new Query<>(query.getOffset(), Integer.MAX_VALUE, query.getSortOrders(), query.getInMemorySorting(), filter);
        }
    }

    public Query<Node, DataFilter> getQuery() {
        return this.query;
    }

    public void cropKeyFromQuery(final String key) {
        this.query.getFilter().ifPresent(filter -> filter.getPropertyFilters().remove(key));
    }

    public boolean hasProperty(final String key) {
        final Object expectedKey = getMappedQuery().get(key);
        return Objects.nonNull(expectedKey) && !Objects.equals(expectedKey, "");
    }

    public Object getProperty(final String key) {
        return getMappedQuery().get(key);
    }

    private DataFilter createNewFilter(final DataFilter oldFilter) {
        DataFilter newFilter = new DataFilter();
        newFilter.setFullTextSearchStatement(oldFilter.getFullTextSearchStatement());
        newFilter.setFilteringMode(oldFilter.getFilteringMode());
        newFilter.setValue(oldFilter.getValue());
        Map<String, Object> propertyFilters = oldFilter.getPropertyFilters();
        for (Map.Entry<String, Object> entry : propertyFilters.entrySet()) {
            newFilter.filter(entry.getKey(), entry.getValue());
        }
        return newFilter;
    }

    private Map<String, Object> getMappedQuery() {
        return this.query.getFilter().map(DataFilter::getPropertyFilters).orElseGet(Collections::emptyMap);
    }
}
