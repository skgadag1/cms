package com.tbscg.doosan.magnolia.pmd.ui.service;

import com.tbscg.doosan.magnolia.pmd.data.model.*;
import com.tbscg.doosan.magnolia.pmd.data.provider.api.ApiService;
import com.tbscg.doosan.magnolia.pmd.ui.EditForm;
import com.tbscg.doosan.magnolia.pmd.ui.pmi.PmiSelectorTree;
import com.tbscg.doosan.magnolia.pmd.ui.pmi.RangeEditForm;
import com.vaadin.data.Binder;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
public class ScheduleConnectionForm extends EditForm<ScheduleConnectionData> {

    private final FormLayout layout = new FormLayout();
    private final PmiSelectorTree pmiSelect;

    private final ApiService apiService;

    public ScheduleConnectionForm(ApiService apiService) {
        super(new ScheduleConnectionData());
        this.apiService = apiService;

        VerticalLayout rangeOptionsPart = new VerticalLayout();
        rangeOptionsPart.setCaption("Select PMI item");

        this.pmiSelect = new PmiSelectorTree(apiService, Model.class, Prefix.class, Range.class);
        this.pmiSelect.setCaption("Select range, prefix or model");
        this.pmiSelect.setWidth(400, Sizeable.Unit.PIXELS);
        this.pmiSelect.setHeight(600, Sizeable.Unit.PIXELS);

        layout.addComponent(this.pmiSelect);
    }

    @Override
    public Component asComponent() {
        return this.layout;
    }

    @Override
    protected void bind(Binder<ScheduleConnectionData> binder) {
        binder.forField(this.pmiSelect)
                .withValidator(Objects::nonNull, "range, prefix or model is required")
                .bind(data -> {
                    if ("model".equals(data.getTargetType())) return this.apiService.getModel(data.getTargetId());
                    if ("prefix".equals(data.getTargetType())) return this.apiService.getPrefix(data.getTargetId());
                    if ("range".equals(data.getTargetType())) return this.apiService.getRange(data.getTargetId());
                    return null;
                },
                (data, pmi) -> {
                    data.setTargetId(pmi.getId());
                    data.setTargetType(pmi.getType().toLowerCase());
                });
    }
}
