package com.tbscg.doosan.magnolia.pmd.data.provider.api.pmi;

import com.tbscg.doosan.magnolia.pmd.data.model.*;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Setter
@NoArgsConstructor
public class PrefixFilter implements PmiFilter {

    private Prefix prefix;

    public List<Brand> filterBrands(List<Brand> brands) {
        if (prefix == null) {
            return brands;
        }
        return brands.stream().filter(b -> b.getId() == prefix.getBrandId()).collect(Collectors.toList());
    }

    @Override
    public List<Family> filterFamilies(List<Family> families) {
        if (prefix == null) {
            return families;
        }
        return families.stream().filter(f -> f.getId() == prefix.getFamilyId()).collect(Collectors.toList());
    }

    @Override
    public List<Subfamily> filterSubfamilies(List<Subfamily> subfamilies) {
        if (prefix == null) {
            return subfamilies;
        }
        return subfamilies.stream().filter(s -> s.getId() == prefix.getSubfamilyId()).collect(Collectors.toList());
    }

    @Override
    public List<Model> filterModels(List<Model> models) {
        if (prefix == null) {
            return models;
        }
        return models.stream().filter(m -> m.getId() == prefix.getModelId()).collect(Collectors.toList());
    }

    @Override
    public List<Prefix> filterPrefixes(List<Prefix> prefixes) {
        if (prefix == null) {
            return prefixes;
        }
        return prefixes.stream().filter(p -> p.getId() == prefix.getId()).collect(Collectors.toList());
    }

    @Override
    public List<Range> filterRanges(List<Range> ranges) {
        if (prefix == null) {
            return ranges;
        }
        return ranges.stream().filter(r -> r.getPrefixId() == prefix.getId()).collect(Collectors.toList());
    }
}
