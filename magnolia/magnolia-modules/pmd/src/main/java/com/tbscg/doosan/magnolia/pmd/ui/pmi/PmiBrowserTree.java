
package com.tbscg.doosan.magnolia.pmd.ui.pmi;

import com.tbscg.doosan.magnolia.pmd.data.model.*;
import com.tbscg.doosan.magnolia.pmd.data.provider.api.ApiService;
import com.tbscg.doosan.magnolia.pmd.data.provider.api.pmi.ProductHierarchyDataProvider;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.*;
import lombok.extern.slf4j.Slf4j;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.function.BooleanSupplier;

@Slf4j
@StyleSheet("vaadin://themes/pmd/pmi-v-menubar.css")
public class PmiBrowserTree extends VerticalLayout {

    private final ApiService apiService;
    private final TreeGrid<ConnectTarget> grid = new TreeGrid<>();
    private final ProductHierarchyDataProvider productProvider;
    private final ModelSelector modelSelect;
    private final PrefixSelector prefixSelect;

    public PmiBrowserTree(ApiService apiService) {
        this.apiService = apiService;
        setMargin(false);
        this.productProvider = new ProductHierarchyDataProvider(apiService, Range.class);

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidth(100, Unit.PERCENTAGE);
        toolbar.setDefaultComponentAlignment(Alignment.BOTTOM_LEFT);

        this.modelSelect = new ModelSelector(apiService, "Filter by model:");
        this.modelSelect.addValueChangeListener(e -> selectModel(e.getValue()));
        toolbar.addComponent(this.modelSelect);

        this.prefixSelect = new PrefixSelector(apiService, "Filter by prefix:");
        this.prefixSelect.addValueChangeListener(e -> selectPrefix(e.getValue()));
        toolbar.addComponent(this.prefixSelect);

        Button clearButton = new Button("Clear filters", VaadinIcons.ERASER);
        clearButton.addClickListener(e -> {
            this.productProvider.resetFilters();
            this.modelSelect.setValue(null);
            this.prefixSelect.setValue(null);
        });
        toolbar.addComponent(clearButton);

        toolbar.addComponentsAndExpand(new Panel());

        Button addBrandButton = new Button("Add new brand", VaadinIcons.PLUS);
        addBrandButton.addClickListener(e -> getUI().addWindow(BrandEditForm.newBrand(apiService, s -> refreshGrid())));
        toolbar.addComponent(addBrandButton);

        addComponent(toolbar);

        this.grid.setDataProvider(productProvider);
        this.grid.setSelectionMode(Grid.SelectionMode.NONE);
        this.grid.addColumn(ConnectTarget::getLabel).setCaption("Name");
        this.grid.addColumn(ConnectTarget::getType).setCaption("Type");
        this.grid.addComponentColumn(this::getItemActions).setCaption("Options");
        this.grid.setSizeFull();

        Panel scrollPanel = new Panel(this.grid);
        scrollPanel.setSizeFull();
        addComponentsAndExpand(scrollPanel);
    }

    private void selectPrefix(Prefix selected) {
        if (selected != null) {
            this.grid.expand(this.productProvider.getAncestors(selected));
            this.modelSelect.setValue(null);
        }
        this.productProvider.setPrefixFilter(selected);
    }

    private void selectModel(Model selected) {
        if (selected != null) {
            this.grid.expand(this.productProvider.getAncestors(selected));
            this.prefixSelect.setValue(null);
        }
        this.productProvider.setModelFilter(selected);
    }

    private Component getItemActions(ConnectTarget item) {
        if (item != null) {
            if (item instanceof Brand) {
                return getBrandActions((Brand) item);
            } else if (item instanceof Family) {
                return getFamilyActions((Family) item);
            } else if (item instanceof Subfamily) {
                return getSubfamilyActions((Subfamily) item);
            } else if (item instanceof Model) {
                return getModelActions((Model) item);
            } else if (item instanceof Prefix) {
                return getPrefixActions((Prefix) item);
            } else if (item instanceof Range) {
                return getRangeActions((Range) item);
            }
        }
        return new Label();
    }

    private void refreshGrid() {
        this.grid.getDataProvider().refreshAll();
    }

    private void executeAction(String errorMsg, Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            log.error(errorMsg, e);
            Notification.show(errorMsg, Notification.Type.ERROR_MESSAGE);
        }
    }

    private void executeConditionalAction(String errorMsg, BooleanSupplier condition, Runnable action) {
        if (condition.getAsBoolean()) {
            executeAction("Unexpected error occured", action);
        } else {
            Notification.show(errorMsg, Notification.Type.ERROR_MESSAGE);
        }
    }

    private Component getBrandActions(Brand brand) {
        MenuBar menu = new MenuBar();
        menu.addItem("edit", VaadinIcons.EDIT, item -> {
            getUI().addWindow(BrandEditForm.editBrand(apiService, brand, s -> refreshGrid()));
        });
        menu.addItem("delete", VaadinIcons.CLOSE, item -> {
            executeConditionalAction("Can't remove brand with families assigned",
                    () -> this.productProvider.getChildren(brand).isEmpty(),
                    () -> ConfirmDialog.show(getUI(), "Remove brand " + brand.getName() + "?", dialog -> {
                        if (dialog.isConfirmed()) {
                            executeAction("Error trying to remove brand", () -> {
                                apiService.deleteBrand(brand.getId());
                                refreshGrid();
                            });
                        }
                    })
            );
        });
        menu.addItem("add family", VaadinIcons.PLUS, item -> {
            getUI().addWindow(FamilyEditForm.newFamily(apiService, brand, s -> refreshGrid()));
        });
        return menu;
    }

    private Component getFamilyActions(Family family) {
        MenuBar menu = new MenuBar();
        menu.addItem("edit", VaadinIcons.EDIT, item -> {
            getUI().addWindow(FamilyEditForm.editFamily(apiService, family, s -> refreshGrid()));
        });
        menu.addItem("delete", VaadinIcons.CLOSE, item -> {
            executeConditionalAction("Can't remove family with subfamilies assigned",
                    () -> this.productProvider.getChildren(family).isEmpty(),
                    () -> ConfirmDialog.show(getUI(), "Remove family " + family.getName() + "?", dialog -> {
                        if (dialog.isConfirmed()) {
                            executeAction("Error trying to remove family", () -> {
                                apiService.deleteFamily(family.getId());
                                refreshGrid();
                            });
                        }
                    })
            );
        });
        menu.addItem("add subfamily", VaadinIcons.PLUS, item -> {
            getUI().addWindow(SubfamilyEditForm.newSubfamily(apiService, family, s -> refreshGrid()));
        });
        return menu;
    }

    private Component getSubfamilyActions(Subfamily subfamily) {
        MenuBar menu = new MenuBar();
        menu.addItem("edit", VaadinIcons.EDIT, item -> {
            getUI().addWindow(SubfamilyEditForm.editSubfamily(apiService, subfamily, s -> refreshGrid()));
        });
        menu.addItem("delete", VaadinIcons.CLOSE, item -> {
            executeConditionalAction("Can't remove subfamily with models assigned",
                    () -> this.productProvider.getChildren(subfamily).isEmpty(),
                    () -> ConfirmDialog.show(getUI(), "Remove subfamily " + subfamily.getName() + "?", dialog -> {
                        if (dialog.isConfirmed()) {
                            executeAction("Error trying to remove subfamily", () -> {
                                apiService.deleteSubfamily(subfamily.getId());
                                refreshGrid();
                            });
                        }
                    })
            );
        });
        menu.addItem("add model", VaadinIcons.PLUS, item -> {
            getUI().addWindow(ModelEditForm.newModel(apiService, subfamily, s -> refreshGrid()));
        });
        return menu;
    }

    private Component getModelActions(Model model) {
        MenuBar menu = new MenuBar();
        menu.addItem("edit", VaadinIcons.EDIT, item -> {
            getUI().addWindow(ModelEditForm.editModel(apiService, model, s -> refreshGrid()));
        });
        menu.addItem("delete", VaadinIcons.CLOSE, item -> {
            executeConditionalAction("Can't remove model with prefixes assigned",
                    () -> this.productProvider.getChildren(model).isEmpty(),
                    () -> ConfirmDialog.show(getUI(), "Remove model " + model.getCode() + "?", dialog -> {
                        if (dialog.isConfirmed()) {
                            executeAction("Error trying to remove model", () -> {
                                apiService.deleteModel(model.getId());
                                refreshGrid();
                            });
                        }
                    })
            );
        });
        menu.addItem("add prefix", VaadinIcons.PLUS, item -> {
            getUI().addWindow(PrefixEditForm.newPrefix(apiService, model, s -> refreshGrid()));
        });
        return menu;
    }

    private Component getPrefixActions(Prefix prefix) {
        MenuBar menu = new MenuBar();
        menu.addItem("edit", VaadinIcons.EDIT, item -> {
            getUI().addWindow(PrefixEditForm.editPrefix(apiService, prefix, s -> refreshGrid()));
        });
        menu.addItem("delete", VaadinIcons.CLOSE, item -> {
            executeConditionalAction("Can't remove prefix with ranges assigned",
                    () -> this.productProvider.getChildren(prefix).isEmpty(),
                    () -> ConfirmDialog.show(getUI(), "Remove prefix " + prefix.getPrefix() + "?", dialog -> {
                        if (dialog.isConfirmed()) {
                            executeAction("Error trying to remove prefix", () -> {
                                apiService.deletePrefix(prefix.getId());
                                refreshGrid();
                            });
                        }
                    })
            );
        });
        menu.addItem("add range", VaadinIcons.PLUS, item -> {
            getUI().addWindow(RangeEditForm.newRange(apiService, prefix, s -> refreshGrid()));
        });
        return menu;
    }

    private Component getRangeActions(Range range) {
        MenuBar menu = new MenuBar();
        menu.addItem("edit", VaadinIcons.EDIT, item -> {
            getUI().addWindow(RangeEditForm.editRange(apiService, range, s -> refreshGrid()));
        });
        menu.addItem("delete", VaadinIcons.CLOSE, item -> {
            ConfirmDialog.show(getUI(), "Remove range " + range.getLabel() + "?", dialog -> {
                if (dialog.isConfirmed()) {
                    executeAction("Error trying to remove range", () -> {
                        apiService.deleteRange(range.getId());
                        refreshGrid();
                    });
                }
            });
        });
        return menu;
    }
}
