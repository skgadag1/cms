package com.tbscg.doosan.magnolia.pmd.ui.pmi;

import com.tbscg.doosan.magnolia.pmd.data.model.Model;
import com.tbscg.doosan.magnolia.pmd.data.model.ModelData;
import com.tbscg.doosan.magnolia.pmd.data.model.Subfamily;
import com.tbscg.doosan.magnolia.pmd.data.provider.api.ApiService;
import com.tbscg.doosan.magnolia.pmd.ui.EditForm;
import com.tbscg.doosan.magnolia.pmd.ui.EditWindow;
import com.vaadin.data.Binder;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.function.Consumer;

public class ModelEditForm extends EditForm<ModelData> {

    private final VerticalLayout layout = new VerticalLayout();
    private final PmiSelectorTree subfamily;
    private final TextField code = new TextField("Code");
    private final ApiService apiService;

    private ModelEditForm(ApiService apiService, ModelData data) {
        super(data);
        this.apiService = apiService;

        this.subfamily = new PmiSelectorTree(apiService, Subfamily.class);
        this.subfamily.setCaption("Select subfamily");
        this.subfamily.setWidth(300, Sizeable.Unit.PIXELS);
        this.subfamily.setHeight(300, Sizeable.Unit.PIXELS);

        this.code.setWidthFull();
        this.layout.addComponent(new FormLayout(this.subfamily, this.code));
    }

    @Override
    public Component asComponent() {
        return this.layout;
    }

    public static EditWindow<ModelData> newModel(ApiService apiService, Subfamily subfamily, Consumer<Model> callback) {
        ModelEditForm editForm = new ModelEditForm(apiService, new ModelData().withSubfamilyId(subfamily.getId()));
        return new EditWindow<>("New model", editForm, data -> {
            Model newModel = apiService.addModel(data);
            if (callback != null) {
                callback.accept(newModel);
            }
        });
    }

    public static EditWindow<ModelData> editModel(ApiService apiService, Model model, Consumer<Model> callback) {
        ModelEditForm editForm = new ModelEditForm(apiService, new ModelData(model));
        return new EditWindow<>("Edit model attributes", editForm, data -> {
            Model updated = apiService.updateModel(model.getId(), data);
            if (callback != null) {
                callback.accept(updated);
            }
        });
    }

    @Override
    protected void bind(Binder<ModelData> binder) {
        binder.forField(this.subfamily)
                .withValidator(Objects::nonNull, "Subfamily is required")
                .bind(data -> this.apiService.getSubfamily(data.getSubfamilyId()),
                        (data, subfamily) -> data.setSubfamilyId(subfamily.getId()));

        binder.forField(this.code)
                .withValidator(StringUtils::isNotBlank, "Code is required")
                .bind(ModelData::getCode, ModelData::setCode);
    }
}
