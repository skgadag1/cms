
package com.tbscg.doosan.magnolia.pmd.ui.service;

import com.tbscg.doosan.magnolia.pmd.data.model.*;
import com.tbscg.doosan.magnolia.pmd.data.provider.api.ApiService;
import com.tbscg.doosan.magnolia.pmd.ui.EditForm;
import com.tbscg.doosan.magnolia.pmd.ui.EditWindow;
import com.tbscg.doosan.magnolia.pmd.util.Validation;
import com.vaadin.data.Binder;
import com.vaadin.data.converter.StringToDoubleConverter;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.ui.*;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.function.Consumer;

@Getter
public class TriggerEditForm extends EditForm<IntervalTriggerData> {

    private final VerticalLayout layout = new VerticalLayout();

    private final ComboBox<IntervalTriggerType> type = new ComboBox<>("Trigger type");
    private final TextField intervalValue = new TextField("Interval value");
    private final ApiService apiService;

    private TriggerEditForm(ApiService apiService, IntervalTriggerData data) {
        super(data);
        this.apiService = apiService;

        this.type.setEmptySelectionAllowed(false);
        this.type.setWidthFull();
        this.type.setItems(apiService.getIntervalTriggerTypes());
        this.type.setItemCaptionGenerator(IntervalTriggerType::getCode);

        this.layout.addComponent(new FormLayout(this.type, this.intervalValue));
    }

    public static EditWindow<IntervalTriggerData> newTrigger(
            ApiService apiService, Interval interval, Consumer<IntervalTrigger> callback) {
        TriggerEditForm editForm = new TriggerEditForm(apiService, new IntervalTriggerData());
        return new EditWindow<>("New interval trigger", editForm, data -> {
            IntervalTrigger newTrigger = apiService.addIntervalTrigger(interval, data);
            if (callback != null) {
                callback.accept(newTrigger);
            }
        });
    }

    @Override
    public Component asComponent() {
        return this.layout;
    }

    @Override
    protected void bind(Binder<IntervalTriggerData> binder) {
        binder.forField(this.type)
                .withValidator(Objects::nonNull, "Type is required")
                .bind(data -> this.apiService.getIntervalTriggerTypes().stream()
                                .filter(t -> t.getId() == data.getTypeId())
                                .findFirst().orElse(null),
                        (data, type) -> data.setTypeId(type.getId()));

        binder.forField(this.intervalValue)
                .withValidator(StringUtils::isNotBlank, "Interval value is required")
                .withValidator(Validation::isNumberFormat, "Interval value should be a number")
                .withConverter(new StringToDoubleConverter("Invalid number value"))
                .bind(IntervalTriggerData::getIntervalValue, IntervalTriggerData::setIntervalValue);
    }
}
