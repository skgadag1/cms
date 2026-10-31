package com.tbscg.doosan.magnolia.pmd.ui.service;

import com.tbscg.doosan.magnolia.pmd.data.model.*;
import com.tbscg.doosan.magnolia.pmd.data.provider.api.ApiService;
import com.tbscg.doosan.magnolia.pmd.ui.EditForm;
import com.tbscg.doosan.magnolia.pmd.ui.EditWindow;
import com.tbscg.doosan.magnolia.pmd.util.Validation;
import com.vaadin.data.Binder;
import com.vaadin.data.converter.StringToDoubleConverter;
import com.vaadin.ui.*;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.function.Consumer;

@Getter
public class ActionEditForm extends EditForm<ServiceActionData> {

    private final VerticalLayout layout = new VerticalLayout();

    private final ComboBox<ServiceActionType> type = new ComboBox<>("Action type");
    private final TextField quantity = new TextField("Quantity");
    private final TextField partNumber = new TextField("Part number");
    private final ApiService apiService;

    private ActionEditForm(ApiService apiService, ServiceActionData data) {
        super(data);
        this.apiService = apiService;

        this.type.setEmptySelectionAllowed(false);
        this.type.setItems(apiService.getServiceActionTypes());
        this.type.setItemCaptionGenerator(ServiceActionType::getCode);

        this.layout.addComponent(new FormLayout(this.type, this.quantity, this.partNumber));
    }

    public static EditWindow<ServiceActionData> newAction(
            ApiService apiService, Interval interval, Consumer<ServiceAction> callback) {
        ActionEditForm editForm = new ActionEditForm(apiService, new ServiceActionData());
        return new EditWindow<>("New service action", editForm, data -> {
            ServiceAction action = apiService.addServiceAction(interval, data);
            if (callback != null) {
                callback.accept(action);
            }
        });
    }

    @Override
    public Component asComponent() {
        return this.layout;
    }

    @Override
    protected void bind(Binder<ServiceActionData> binder) {
        binder.forField(this.type)
                .withValidator(Objects::nonNull, "Type is required")
                .bind(data -> this.apiService.getServiceActionTypes().stream()
                                .filter(t -> t.getId() == data.getTypeId())
                                .findFirst().orElse(null),
                        (data, type) -> data.setTypeId(type.getId()));

        binder.forField(this.quantity)
                .withValidator(StringUtils::isNotBlank, "Quantity is required")
                .withValidator(Validation::isNumberFormat, "Quantity should be a number")
                .withConverter(new StringToDoubleConverter("Invalid number value"))
                .bind(ServiceActionData::getQuantity, ServiceActionData::setQuantity);

        binder.forField(this.partNumber)
                .withValidator(StringUtils::isNotBlank, "Part number is required")
                .bind(ServiceActionData::getPartNumber, ServiceActionData::setPartNumber);
    }
}
