package com.tbscg.doosan.magnolia.pmd.data.provider.api.pmi;

import com.tbscg.doosan.magnolia.pmd.data.model.*;
import com.tbscg.doosan.magnolia.pmd.data.provider.api.ApiService;
import com.vaadin.data.provider.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.data.provider.HierarchicalQuery;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class ProductHierarchyDataProvider extends AbstractBackEndHierarchicalDataProvider<ConnectTarget, Void> {

    private final ApiService apiService;
    private final int maxLevel;
    private final PrefixFilter prefixFilter = new PrefixFilter();
    private final ModelFilter modelFilter = new ModelFilter();

    @SafeVarargs
    public ProductHierarchyDataProvider(ApiService apiService, Class<? extends ConnectTarget>... selectableClasses) {
        this.apiService = apiService;
        this.maxLevel = Arrays.stream(selectableClasses)
                .map(this::getHierarchyLevel)
                .max(Integer::compareTo)
                .orElse(getHierarchyLevel(Range.class));
    }

    private Collection<PmiFilter> getFilters() {
        return Arrays.asList(this.prefixFilter, this.modelFilter);
    }

    public List<? extends ConnectTarget> getChildren(ConnectTarget item) {
        List<? extends ConnectTarget> result;
        if (item == null) {
            List<Brand> brands = this.apiService.getBrands();
            for (PmiFilter filter : getFilters()) {
                brands = filter.filterBrands(brands);
            }
            result = brands;
        } else if (item instanceof Brand) {
            List<Family> families = this.apiService.getFamilies((Brand) item);
            for (PmiFilter filter : getFilters()) {
                families = filter.filterFamilies(families);
            }
            result = families;
        } else if (item instanceof Family) {
            List<Subfamily> subfamilies = this.apiService.getSubfamilies((Family) item);
            for (PmiFilter filter : getFilters()) {
                subfamilies = filter.filterSubfamilies(subfamilies);
            }
            result = subfamilies;
        } else if (item instanceof Subfamily) {
            List<Model> models = this.apiService.getModels((Subfamily) item);
            for (PmiFilter filter : getFilters()) {
                models = filter.filterModels(models);
            }
            result = models;
        } else if (item instanceof Model) {
            List<Prefix> prefixes = this.apiService.getPrefixes((Model) item);
            for (PmiFilter filter : getFilters()) {
                prefixes = filter.filterPrefixes(prefixes);
            }
            result = prefixes;
        } else if (item instanceof Prefix) {
            List<Range> ranges = this.apiService.getRanges((Prefix) item);
            for (PmiFilter filter : getFilters()) {
                ranges = filter.filterRanges(ranges);
            }
            result = ranges;
        } else {
            log.error("issue getting backend data for " + item);
            throw new IllegalArgumentException("invalid parent item: " + item);
        }
        return result;
    }

    private List<? extends ConnectTarget> getChildrenNonEmpty(ConnectTarget item) {
        List<? extends ConnectTarget> result = getChildren(item);
        if (result.isEmpty() && hasChildren(item)) {
            return Collections.singletonList(new NoChildren(item));
        }
        return result;
    }

    @Override
    protected Stream<ConnectTarget> fetchChildrenFromBackEnd(HierarchicalQuery<ConnectTarget, Void> query) {
        Stream<ConnectTarget> stream = getChildrenNonEmpty(query.getParent()).stream()
                .skip(query.getOffset())
                .limit(query.getLimit())
                .map(Function.identity());
        List<ConnectTarget> list = stream.collect(Collectors.toList());
        String itemsStr = list.stream().map(i -> String.valueOf(i.getId())).collect(Collectors.joining(","));
        log.debug(printQuery("fetchChildrenFromBackEnd", query) + " | RESULTS: " + itemsStr);
        return list.stream();
    }

    @Override
    public int getChildCount(HierarchicalQuery<ConnectTarget, Void> query) {
        int size = getChildrenNonEmpty(query.getParent()).size();
        log.debug(printQuery("getChildCount", query) + " | RESULT: " + size);
        return size;
    }

    private int getHierarchyLevel(Class<? extends ConnectTarget> classType) {
        if (classType == Brand.class) return 1;
        else if (classType == Family.class) return 2;
        else if (classType == Subfamily.class) return 3;
        else if (classType == Model.class) return 4;
        else if (classType == Prefix.class) return 5;
        else if (classType == Range.class) return 6;
        else return 0;
    }

    @Override
    public boolean hasChildren(ConnectTarget item) {
        boolean itemLevelCheck = item != null && getHierarchyLevel(item.getClass()) < this.maxLevel;
        boolean result = itemLevelCheck && item.hasChildren();
        log.debug("hasChildren | " + item + " | RESULT: " + result);
        return result;
    }

    private String printQuery(String prefix, HierarchicalQuery<ConnectTarget, Void> query) {
        return prefix + " | offset:" + query.getOffset()
                + ", limit:" + query.getLimit()
                + ", parent: " + query.getParentOptional().map(Object::toString).orElse(null);
    }

    public ConnectTarget getParent(ConnectTarget item) {
        if (item instanceof Family) {
            return this.apiService.getBrand(((Family) item).getBrandId());
        } else if (item instanceof Subfamily) {
            return this.apiService.getFamily(((Subfamily) item).getFamilyId());
        } else if (item instanceof Model) {
            return this.apiService.getSubfamily(((Model) item).getSubfamilyId());
        } else if (item instanceof Prefix) {
            return this.apiService.getModel(((Prefix) item).getModelId());
        } else if (item instanceof Range) {
            return this.apiService.getPrefix(((Range) item).getPrefixId());
        }
        return null;
    }

    public List<ConnectTarget> getAncestors(ConnectTarget item) {
        ConnectTarget parent = getParent(item);
        if (parent != null) {
            List<ConnectTarget> list = new ArrayList<>(getAncestors(parent));
            list.add(parent);
            return list;
        }
        return Collections.emptyList();
    }

    public void resetFilters() {
        this.modelFilter.setModel(null);
        this.prefixFilter.setPrefix(null);
        this.refreshAll();
    }

    public void setPrefixFilter(Prefix prefix) {
        if (prefix != null) {
            this.modelFilter.setModel(null);
        }
        this.prefixFilter.setPrefix(prefix);
        this.refreshAll();
    }

    public void setModelFilter(Model model) {
        if (model != null) {
            this.prefixFilter.setPrefix(null);
        }
        this.modelFilter.setModel(model);;
        this.refreshAll();
    }
}
