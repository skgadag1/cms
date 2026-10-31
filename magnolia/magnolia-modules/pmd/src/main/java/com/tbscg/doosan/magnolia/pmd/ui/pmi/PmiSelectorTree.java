package com.tbscg.doosan.magnolia.pmd.ui.pmi;

import com.tbscg.doosan.magnolia.pmd.data.model.ConnectTarget;
import com.tbscg.doosan.magnolia.pmd.data.model.Model;
import com.tbscg.doosan.magnolia.pmd.data.model.Prefix;
import com.tbscg.doosan.magnolia.pmd.data.model.Range;
import com.tbscg.doosan.magnolia.pmd.data.provider.api.ApiService;
import com.tbscg.doosan.magnolia.pmd.data.provider.api.pmi.ProductHierarchyDataProvider;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.ui.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@StyleSheet("vaadin://themes/pmd/pmi-selector-tree.css")
public class PmiSelectorTree extends CustomField<ConnectTarget> {

    private final VerticalLayout layout = new VerticalLayout();
    private final Tree<ConnectTarget> tree = new Tree<>();
    private final Label selectedDisplay = new Label();
    private final Class<? extends ConnectTarget>[] selectableClasses;
    private final ProductHierarchyDataProvider dataProvider;
    private ConnectTarget selected;

    @SafeVarargs
    public PmiSelectorTree(ApiService apiService, Class<? extends ConnectTarget>... selectableClasses) {
        this.selectableClasses = selectableClasses;

        this.dataProvider = new ProductHierarchyDataProvider(apiService, selectableClasses);

        this.tree.setDataProvider(this.dataProvider);
        this.tree.setSelectionMode(Grid.SelectionMode.SINGLE);
        this.tree.setItemCaptionGenerator(ConnectTarget::getLabel);
        this.tree.addSelectionListener(e -> {
            ConnectTarget value = validateSelected(e.getFirstSelectedItem().orElse(null));
            if (isDifferentValue(value)) {
                ConnectTarget oldValue = this.getValue();
                pmiSelect(value);
                fireEvent(createValueChange(oldValue, true));
            }
        });

        Panel scrollPanel = new Panel("Product hierarchy", this.tree);
        scrollPanel.setSizeFull();
        this.layout.addComponentsAndExpand(scrollPanel);

        this.selectedDisplay.setCaption("selected:");
        this.layout.addComponent(this.selectedDisplay);
    }

    @Override
    protected Component initContent() {
        return this.layout;
    }

    @Override
    public ConnectTarget getValue() {
        return selected;
    }

    @Override
    protected void doSetValue(ConnectTarget value) {
        if (value != null) {
            this.tree.expand(this.dataProvider.getAncestors(value));
        }
        pmiSelect(value);
    }

    private ConnectTarget validateSelected(ConnectTarget selected) {
        if (selected != null) {
            if (this.selectableClasses == null || this.selectableClasses.length == 0) {
                return selected;
            } else {
                for (Class<? extends ConnectTarget> selectableClass : this.selectableClasses) {
                    if (selectableClass.isAssignableFrom(selected.getClass())) {
                        return selected;
                    }
                }
            }
        }
        return null;
    }

    private void pmiSelect(ConnectTarget target) {
        this.selected = validateSelected(target);
        if (this.selected == null) {
            this.tree.getSelectionModel().deselectAll();
            this.selectedDisplay.setValue("");
        } else {
            this.tree.getSelectionModel().select(this.selected);
            this.selectedDisplay.setValue(this.selected.getType() + ": " + this.selected.getLabel());
        }
    }
}
