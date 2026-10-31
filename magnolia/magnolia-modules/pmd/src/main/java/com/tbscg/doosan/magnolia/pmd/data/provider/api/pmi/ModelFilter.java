package com.tbscg.doosan.magnolia.pmd.data.provider.api.pmi;

import com.tbscg.doosan.magnolia.pmd.data.model.*;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Setter
@NoArgsConstructor
public class ModelFilter implements PmiFilter {

    private Model model;

    public List<Brand> filterBrands(List<Brand> brands) {
        if (model == null) {
            return brands;
        }
        return brands.stream().filter(b -> b.getId() == model.getBrandId()).collect(Collectors.toList());
    }

    @Override
    public List<Family> filterFamilies(List<Family> families) {
        if (model == null) {
            return families;
        }
        return families.stream().filter(f -> f.getId() == model.getFamilyId()).collect(Collectors.toList());
    }

    @Override
    public List<Subfamily> filterSubfamilies(List<Subfamily> subfamilies) {
        if (model == null) {
            return subfamilies;
        }
        return subfamilies.stream().filter(s -> s.getId() == model.getSubfamilyId()).collect(Collectors.toList());
    }

    @Override
    public List<Model> filterModels(List<Model> models) {
        if (model == null) {
            return models;
        }
        return models.stream().filter(m -> m.getId() == model.getId()).collect(Collectors.toList());
    }

    @Override
    public List<Prefix> filterPrefixes(List<Prefix> prefixes) {
        if (model == null) {
            return prefixes;
        }
        return prefixes.stream().filter(p -> p.getModelId() == model.getId()).collect(Collectors.toList());
    }

    @Override
    public List<Range> filterRanges(List<Range> ranges) {
        if (model == null) {
            return ranges;
        }
        return ranges.stream().filter(r -> r.getModelId() == model.getId()).collect(Collectors.toList());
    }
}
