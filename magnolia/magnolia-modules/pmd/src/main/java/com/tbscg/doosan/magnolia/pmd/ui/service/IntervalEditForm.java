
package com.tbscg.doosan.magnolia.pmd.ui.service;

import com.tbscg.doosan.magnolia.pmd.data.model.IntervalData;
import com.tbscg.doosan.magnolia.pmd.data.model.Interval;
import com.tbscg.doosan.magnolia.pmd.data.model.NameTranslation;
import com.tbscg.doosan.magnolia.pmd.data.model.Schedule;
import com.tbscg.doosan.magnolia.pmd.data.provider.api.ApiService;
import com.tbscg.doosan.magnolia.pmd.ui.EditForm;
import com.tbscg.doosan.magnolia.pmd.ui.EditWindow;
import com.tbscg.doosan.magnolia.pmd.ui.MultilanguageContent;
import com.tbscg.doosan.magnolia.pmd.ui.MultilanguageContent.TranslatedField;
import com.tbscg.doosan.magnolia.pmd.util.Validation;
import com.vaadin.data.Binder;
import com.vaadin.ui.*;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@Getter
public class IntervalEditForm extends EditForm<IntervalData> {

    private final VerticalLayout layout = new VerticalLayout();

    private final TextField labourHours = new TextField("Labour hours");
    private final MultilanguageContent<IntervalData> translations;
    private final ApiService apiService;

    private IntervalEditForm(ApiService apiService, IntervalData data) {
        super(data);
        this.apiService = apiService;

        this.layout.addComponent(new FormLayout(this.labourHours));

        this.translations = new MultilanguageContent<>(apiService.getLocales(),
            locale -> Collections.singletonList(
                new TranslatedField<>(locale, new TextField("Name"), IntervalData::getName)
            )
        );
        this.layout.addComponent(this.translations);
    }

    public static EditWindow<IntervalData> newInterval(ApiService apiService, Schedule schedule, Consumer<Interval> callback) {
        IntervalEditForm editForm = new IntervalEditForm(apiService, new IntervalData());
        return new EditWindow<>("New interval", editForm, data -> {
            Interval newInterval = apiService.addInterval(schedule, data);
            if (callback != null) {
                callback.accept(newInterval);
            }
        });
    }

    public static EditWindow<IntervalData> editInterval(ApiService apiService, Interval interval, Consumer<Interval> callback) {
        List<NameTranslation> translations = apiService.getIntervalTranslations(interval.getId());
        IntervalData intervalData = new IntervalData(interval, translations);
        IntervalEditForm editForm = new IntervalEditForm(apiService, intervalData);
        return new EditWindow<>("Edit interval attributes", editForm, data -> {
            Interval updated = apiService.updateInterval(interval.getId(), data);
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
    protected void bind(Binder<IntervalData> binder) {
        binder.forField(this.labourHours)
                .withValidator(StringUtils::isNotBlank, "Labour hours field is required")
                .withValidator(Validation::isNumberFormat, "Labour hours should be a number")
                .bind(IntervalData::getLabourHours, IntervalData::setLabourHours);

        this.translations.bind(binder);
    }
}
