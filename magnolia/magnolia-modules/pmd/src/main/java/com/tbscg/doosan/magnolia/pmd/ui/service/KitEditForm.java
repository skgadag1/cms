package com.tbscg.doosan.magnolia.pmd.ui.service;

import com.tbscg.doosan.magnolia.pmd.data.model.Interval;
import com.tbscg.doosan.magnolia.pmd.data.model.ServiceKit;
import com.tbscg.doosan.magnolia.pmd.data.model.ServiceKitData;
import com.tbscg.doosan.magnolia.pmd.data.provider.api.ApiService;
import com.tbscg.doosan.magnolia.pmd.ui.EditForm;
import com.tbscg.doosan.magnolia.pmd.ui.EditWindow;
import com.vaadin.data.Binder;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Consumer;

@Getter
public class KitEditForm extends EditForm<ServiceKitData> {

    private final VerticalLayout layout = new VerticalLayout();

    private final TextField partNumber = new TextField("Part number");
    private final ApiService apiService;

    private KitEditForm(ApiService apiService, ServiceKitData data) {
        super(data);
        this.apiService = apiService;
        this.layout.addComponent(new FormLayout(this.partNumber));
    }

    public static EditWindow<ServiceKitData> newKit(
            ApiService apiService, Interval interval, Consumer<ServiceKit> callback) {
        KitEditForm editForm = new KitEditForm(apiService, new ServiceKitData());
        return new EditWindow<>("New service action", editForm, data -> {
            ServiceKit kit = apiService.addServiceKit(interval, data);
            if (callback != null) {
                callback.accept(kit);
            }
        });
    }

    @Override
    public Component asComponent() {
        return this.layout;
    }

    @Override
    protected void bind(Binder<ServiceKitData> binder) {
        binder.forField(this.partNumber)
                .withValidator(StringUtils::isNotBlank, "Part number is required")
                .bind(ServiceKitData::getPartNumber, ServiceKitData::setPartNumber);
    }
}
