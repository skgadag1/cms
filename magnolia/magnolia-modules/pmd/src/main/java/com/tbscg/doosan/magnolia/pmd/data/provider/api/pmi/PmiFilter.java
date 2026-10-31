package com.tbscg.doosan.magnolia.pmd.data.provider.api.pmi;

import com.tbscg.doosan.magnolia.pmd.data.model.*;

import java.util.List;

public interface PmiFilter {

    List<Brand> filterBrands(List<Brand> brands);
    List<Family> filterFamilies(List<Family> families);
    List<Subfamily> filterSubfamilies(List<Subfamily> subfamilies);
    List<Model> filterModels(List<Model> models);
    List<Prefix> filterPrefixes(List<Prefix> prefixes);
    List<Range> filterRanges(List<Range> ranges);
}
