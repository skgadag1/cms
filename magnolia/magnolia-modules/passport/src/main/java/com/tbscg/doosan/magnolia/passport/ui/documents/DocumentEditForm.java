
package com.tbscg.doosan.magnolia.passport.ui.documents;

import com.tbscg.doosan.magnolia.passport.data.model.PassportDocument;
import com.tbscg.doosan.magnolia.passport.data.model.PassportDocumentData;
import com.tbscg.doosan.magnolia.passport.data.model.PropertySimple;
import com.tbscg.doosan.magnolia.passport.data.provider.api.ApiService;
import com.tbscg.doosan.magnolia.passport.ui.MultilanguageContent;
import com.tbscg.doosan.magnolia.passport.ui.MultilanguageContent.TranslatedField;
import com.tbscg.doosan.magnolia.passport.util.DocumentConstants;
import com.vaadin.data.Binder;
import com.vaadin.data.ValidationResult;
import com.vaadin.ui.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.vaadin.addons.ComboBoxMultiselect;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.vaadin.server.Sizeable.Unit.PIXELS;
import static com.vaadin.shared.ui.datefield.DateTimeResolution.MINUTE;
import static java.util.Arrays.asList;

@Slf4j
@Getter
public class DocumentEditForm extends EditForm<PassportDocumentData> {

    private static final int FIELD_WIDTH = 250;
    private final VerticalLayout layout = new VerticalLayout();
    private final ApiService apiService;
    private final List<PropertySimple> brands;
    private final CheckBoxGroup<PropertySimple> brand = new CheckBoxGroup<>();
    private final List<PropertySimple> regions;
    private final CheckBoxGroup<PropertySimple> region = new CheckBoxGroup<>();
    private final List<PropertySimple> applications;
    private final ComboBoxMultiselect<PropertySimple> application = new ComboBoxMultiselect<>();
    private final ComboBox<DocumentConstants.Subject> subject = new ComboBox<>();
    private final DateTimeField startDatePicker = new DateTimeField("Start Date");
    private final DateTimeField endDatePicker = new DateTimeField("End Date");
    private final MultilanguageContent<PassportDocumentData> translatedNames;

    private DocumentEditForm(ApiService apiService, PassportDocumentData data) {
        super(data);
        this.apiService = apiService;

        this.brands = apiService.getBrands();
        this.brand.setCaption("Brand");
        this.brand.setItemCaptionGenerator(PropertySimple::getName);
        this.brand.setItems(this.brands);
        this.brand.setWidth(FIELD_WIDTH, PIXELS);

        this.regions = apiService.getRegions();
        this.region.setCaption("Region");
        this.region.setItemCaptionGenerator(PropertySimple::getName);
        this.region.setItems(this.regions);
        this.region.setWidth(FIELD_WIDTH, PIXELS);

        this.subject.setCaption("Subject");
        this.subject.setItems(asList(DocumentConstants.Subject.values()));
        this.subject.setEmptySelectionAllowed(false);
        this.subject.setWidth(FIELD_WIDTH, PIXELS);

        this.startDatePicker.setResolution(MINUTE);
        this.startDatePicker.setWidth(FIELD_WIDTH, PIXELS);
        this.startDatePicker.setValue(ZonedDateTime.now().toLocalDateTime());

        this.endDatePicker.setResolution(MINUTE);
        this.endDatePicker.setWidth(FIELD_WIDTH, PIXELS);
        this.endDatePicker.setValue(ZonedDateTime.now().toLocalDateTime());

        this.applications = apiService.getBaseApplications();
        this.application.setCaption("Applications");
        this.application.setItemCaptionGenerator(PropertySimple::getName);
        this.application.setItems(this.applications);
        this.application.setWidth(FIELD_WIDTH, PIXELS);

        this.layout.addComponent(new FormLayout(brand, region, subject, startDatePicker, endDatePicker, application));

        this.translatedNames = new MultilanguageContent<>(apiService.getLocales(),
                locale -> asList(
                        new TranslatedField<>(locale, new TextField("Title"), PassportDocumentData::getTitle),
                        new TranslatedField<>(locale, new TextField("Description"), PassportDocumentData::getDescription)
                )
        );
        this.layout.addComponent(this.translatedNames);
    }

    public static EditWindow<PassportDocumentData> newDocument(ApiService apiService, Consumer<PassportDocument> callback) {
        PassportDocumentData newPassportDocumentData = new PassportDocumentData();

        DocumentEditForm editForm = new DocumentEditForm(apiService, newPassportDocumentData);
        return new EditWindow<>("New document", editForm, data -> {
            int newDocumentId = apiService.addDocument(data).getDocument_id();

            // get document with details to display
            PassportDocument doc = PassportDocument.of(apiService.getDocument(newDocumentId));
            if (callback != null) {
                callback.accept(doc);
            }
        });
    }

    public static EditWindow<PassportDocumentData> editDocument(ApiService apiService, PassportDocument passportDocument, Consumer<PassportDocument> callback) {
        PassportDocumentData passportDocumentData = new PassportDocumentData(passportDocument);

        DocumentEditForm editForm = new DocumentEditForm(apiService, passportDocumentData);
        return new EditWindow<>("Edit document", editForm, data -> {
            long updatedDocumentId = apiService.updateDocument(passportDocument.getId(), data).getDocumentId();

            PassportDocument doc = PassportDocument.of(apiService.getDocument(updatedDocumentId));
            if (callback != null) {
                callback.accept(doc);
            }
        });
    }

    @Override
    public Component asComponent() {
        return this.layout;
    }

    @Override
    protected void bind(Binder<PassportDocumentData> binder) {
        binder.forField(this.brand)
                .asRequired("Brand is required")
                .bind(data -> this.apiService.getBrands()
                                .stream()
                                .filter(t -> data.getBrandIds().contains(t.getId()))
                                .collect(Collectors.toSet()),
                        (data, brands) -> data.setBrandIds(getIdsFromProperties(brands))
                );

        binder.forField(this.region)
                .asRequired("Region is required")
                .bind(data -> this.apiService.getRegions()
                                .stream()
                                .filter(t -> data.getRegionIds().contains(t.getId()))
                                .collect(Collectors.toSet()),
                        (data, regions) -> data.setRegionIds(getIdsFromProperties(regions))
                );

        binder.forField(this.subject)
                .asRequired("Subject is required")
                .bind(data -> Arrays.stream(DocumentConstants.Subject.values())
                                .filter(t -> t.toString().equals(data.getSubject()))
                                .findFirst()
                                .orElse(null),
                        (data, subject) -> data.setSubject(subject.toString()));

        binder.forField(this.startDatePicker)
                .withValidator(Objects::nonNull, "Start Date is required")
                .bind(data -> getDateTimeFromSeconds(data.getStartDateTime()), (data, time) -> data.setStartDateTime(getTimeInSecondsWithZone(time)));

        binder.forField(this.endDatePicker)
                .withValidator((s, context) -> s.isAfter(this.startDatePicker.getValue())
                        ? ValidationResult.ok()
                        : ValidationResult.error("End Date must be after Start Date"))
                .bind(data -> getDateTimeFromSeconds(data.getEndDateTime()), (data, time) -> data.setEndDateTime(getTimeInSecondsWithZone(time)));

        binder.forField(this.application)
                .withValidator(CollectionUtils::isNotEmpty, "At least one application is required")
                .bind(data -> {
                            Set<Long> dataApplications = data.getApplicationBaseIds();
                            return CollectionUtils.isEmpty(dataApplications) ?
                                    Collections.emptySet() :
                                    apiService.getBaseApplications()
                                            .stream()
                                            .filter(app -> dataApplications.contains(app.getId()))
                                            .collect(Collectors.toSet());
                        },
                        (data, applications) -> data.setApplicationBaseIds(applications
                                .stream()
                                .map(PropertySimple::getId)
                                .collect(Collectors.toSet())
                        )
                );
        translatedNames.bind(binder);
    }

    private LocalDateTime getDateTimeFromSeconds(long seconds) {
        if (seconds == 0) {
            return LocalDateTime.now(ZoneId.systemDefault());
        } else {
            return LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZoneId.systemDefault());
        }
    }

    private static Set<Long> getIdsFromProperties(Set<PropertySimple> properties) {
        return properties.stream()
                .map(PropertySimple::getId)
                .collect(Collectors.toSet());
    }

    private static long getTimeInSecondsWithZone(LocalDateTime time) {
        return time.atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli() / 1000;
    }
}
