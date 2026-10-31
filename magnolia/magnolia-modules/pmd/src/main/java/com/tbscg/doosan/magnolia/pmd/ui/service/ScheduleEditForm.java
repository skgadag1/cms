
package com.tbscg.doosan.magnolia.pmd.ui.service;

import com.tbscg.doosan.magnolia.pmd.data.model.*;
import com.tbscg.doosan.magnolia.pmd.data.provider.api.ApiService;
import com.tbscg.doosan.magnolia.pmd.ui.EditForm;
import com.tbscg.doosan.magnolia.pmd.ui.EditWindow;
import com.tbscg.doosan.magnolia.pmd.ui.MultilanguageContent;
import com.tbscg.doosan.magnolia.pmd.ui.MultilanguageContent.TranslatedField;
import com.vaadin.data.Binder;
import com.vaadin.ui.*;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Getter
public class ScheduleEditForm extends EditForm<ScheduleData> {

    private final VerticalLayout layout = new VerticalLayout();
    private final ComboBox<BusinessUnit> bu = new ComboBox<>("Business unit");
    private final MultilanguageContent<ScheduleData> translations;
    private final ApiService apiService;

    private ScheduleEditForm(ApiService apiService, ScheduleData data) {
        super(data);
        this.apiService = apiService;

        this.bu.setItems(apiService.getBusinessUnits());
        this.bu.setItemCaptionGenerator(BusinessUnit::getCode);
        this.bu.setEmptySelectionAllowed(false);

        this.layout.addComponent(new FormLayout(this.bu));

        this.translations = new MultilanguageContent<>(apiService.getLocales(),
            locale -> Arrays.asList(
                new TranslatedField<>(locale, new TextField("Name"), ScheduleData::getName),
                new TranslatedField<>(locale, new TextField("Document URL"), ScheduleData::getInstructionUrl)
            )
        );
        this.layout.addComponent(this.translations);
    }

    public static EditWindow<ScheduleData> newSchedule(ApiService apiService, Consumer<Schedule> callback) {
        ScheduleEditForm editForm = new ScheduleEditForm(apiService, new ScheduleData());
        return new EditWindow<>("New schedule", editForm, data -> {
            Schedule newSchedule = apiService.addSchedule(data);
            if (callback != null) {
                callback.accept(newSchedule);
            }
        });
    }

    public static EditWindow<ScheduleData> editSchedule(ApiService apiService, Schedule schedule, Consumer<Schedule> callback) {
        List<ScheduleTranslation> translations = apiService.getScheduleTranslations(schedule.getId());
        ScheduleData scheduleData = new ScheduleData(schedule, translations);
        ScheduleEditForm editForm = new ScheduleEditForm(apiService, scheduleData);
        return new EditWindow<>("Edit schedule attributes", editForm, data -> {
            Schedule updated = apiService.updateSchedule(schedule.getId(), data);
            if (callback != null) {
                callback.accept(updated);
            }
        });
    }

    @Override
    public Component asComponent() {
        return this.layout;
    }

    @Override
    protected void bind(Binder<ScheduleData> binder) {
        binder.forField(this.bu)
                .withValidator(Objects::nonNull, "Business unit is required")
                .bind(data -> this.apiService.getBusinessUnits().stream()
                                .filter(bu -> bu.getCode().equals(data.getBusinessUnit()))
                                .findFirst().orElse(null),
                        (data, bu) -> data.setBusinessUnit(bu.getCode()));

        this.translations.bind(binder);
    }
}
