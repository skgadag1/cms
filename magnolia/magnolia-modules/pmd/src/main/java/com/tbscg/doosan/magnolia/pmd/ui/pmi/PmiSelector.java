
package com.tbscg.doosan.magnolia.pmd.ui.pmi;

import com.tbscg.doosan.magnolia.pmd.data.model.*;
import com.tbscg.doosan.magnolia.pmd.data.provider.api.ApiService;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;

import java.util.List;
import java.util.function.Consumer;

public class PmiSelector extends FormLayout {

    private final ComboBox<Brand> brands = getCombo("Brand");
    private final ComboBox<Family> families = getCombo("Family");
    private final ComboBox<Subfamily> subfamilies = getCombo("Subfamily");
    private final ComboBox<Model> models = getCombo("Model");
    private final ComboBox<Prefix> prefixes = getCombo("Prefix");
    private final ComboBox<Range> ranges = getCombo("Range");

    private final Label selectedDisplay = new Label();

    private final ApiService apiService;

    private ConnectTarget selected;
    private Consumer<ConnectTarget> selectionHandler;

    private <T extends ConnectTarget> ComboBox<T> getCombo(String caption) {
        ComboBox<T> combo = new ComboBox<>(caption);
        combo.setEmptySelectionAllowed(true);
        combo.setItemCaptionGenerator(ConnectTarget::getLabel);
        combo.setEnabled(false);
        combo.setValue(null);
        combo.setWidth(220, Sizeable.Unit.PIXELS);
        return combo;
    }

    public PmiSelector(ApiService apiService) {
        this.apiService = apiService;
        selectedDisplay.setCaption("selected:");

        brands.addValueChangeListener(e -> selectBrand(e.getValue()));
        families.addValueChangeListener(e -> selectFamily(e.getValue()));
        subfamilies.addValueChangeListener(e -> selectSubfamily(e.getValue()));
        models.addValueChangeListener(e -> selectModel(e.getValue()));
        prefixes.addValueChangeListener(e -> selectPrefix(e.getValue()));
        ranges.addValueChangeListener(e -> selectRange(e.getValue()));

        addComponent(brands);
        addComponent(families);
        addComponent(subfamilies);
        addComponent(models);
        addComponent(prefixes);
        addComponent(ranges);

        addComponent(selectedDisplay);

        List<Brand> brandList = apiService.getBrands();
        this.brands.setItems(brandList);
        this.brands.setEnabled(true);
        if (!brandList.isEmpty()) {
            this.brands.setValue(brandList.get(0));
        }
    }

    public void setSelectionHandler(Consumer<ConnectTarget> selectionHandler) {
        this.selectionHandler = selectionHandler;
    }

    public ConnectTarget getSelected() {
        return selected;
    }

    private void pmiSelect(ConnectTarget target) {
        this.selected = target;
        if (target != null) {
            selectedDisplay.setValue(target.getType() + ": " + target.getLabel());
        } else {
            selectedDisplay.setValue("");
        }
        if (this.selectionHandler != null) {
            this.selectionHandler.accept(target);
        }
    }

    private void disable(ComboBox<? extends ConnectTarget> combo) {
        combo.setValue(null);
        combo.setItems();
        combo.setEnabled(false);
    }

    private void selectBrand(Brand brand) {
        disable(ranges);
        disable(prefixes);
        disable(models);
        disable(subfamilies);
        disable(families);

        pmiSelect(brand);
        if (brand != null) {
            families.setItems(apiService.getFamilies(brand));
            families.setEnabled(true);
        }
    }

    private void selectFamily(Family family) {
        disable(ranges);
        disable(prefixes);
        disable(models);
        disable(subfamilies);

        pmiSelect(family);
        if (family == null) {
            selectBrand(brands.getValue());
        } else {
            subfamilies.setItems(apiService.getSubfamilies(family));
            subfamilies.setEnabled(true);
        }
    }

    private void selectSubfamily(Subfamily subfamily) {
        disable(ranges);
        disable(prefixes);
        disable(models);

        if (subfamily == null) {
            selectFamily(families.getValue());
        } else {
            pmiSelect(subfamily);
            models.setItems(apiService.getModels(subfamily));
            models.setEnabled(true);
        }
    }

    private void selectModel(Model model) {
        disable(ranges);
        disable(prefixes);

        if (model == null) {
            selectSubfamily(subfamilies.getValue());
        } else {
            pmiSelect(model);
            prefixes.setItems(apiService.getPrefixes(model));
            prefixes.setEnabled(true);
        }
    }

    private void selectPrefix(Prefix prefix) {
        disable(ranges);

        if (prefix == null) {
            selectModel(models.getValue());
        } else {
            pmiSelect(prefix);
            ranges.setItems(apiService.getRanges(prefix));
            ranges.setEnabled(true);
        }
    }

    private void selectRange(Range range) {
        if (range == null) {
            selectPrefix(prefixes.getValue());
        } else {
            pmiSelect(range);
        }
    }
}
