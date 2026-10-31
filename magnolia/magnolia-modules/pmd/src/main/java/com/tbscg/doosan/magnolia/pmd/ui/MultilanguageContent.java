package com.tbscg.doosan.magnolia.pmd.ui;

import com.tbscg.doosan.magnolia.pmd.data.model.Locale;
import com.vaadin.data.Binder;
import com.vaadin.data.ValueProvider;
import com.vaadin.ui.*;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MultilanguageContent<T> extends VerticalLayout {

    private final ComboBox<Locale> localeSelect = new ComboBox<>();
    private final Label title = new Label("Translated content:");
    private final List<TranslatedField<T>> translatedFields = new ArrayList<>();

    public MultilanguageContent(List<Locale> locales,
                                ValueProvider<Locale, List<TranslatedField<T>>> fieldProvider) {

        this.localeSelect.setWidth(200, Unit.PIXELS);
        this.localeSelect.setItems(locales);
        this.localeSelect.setEmptySelectionCaption("- show all -");
        this.localeSelect.setEmptySelectionAllowed(true);
        this.localeSelect.setItemCaptionGenerator(lang -> lang.getName() + (lang.isFallback() ? "(default)" : ""));
        this.localeSelect.addValueChangeListener(e -> selectPanel(e.getValue()));

        HorizontalLayout titlebar = new HorizontalLayout();
        titlebar.setWidthFull();
        titlebar.addComponentsAndExpand(this.title);
        titlebar.addComponent(new Button("show all", e -> selectPanel(null)));
        titlebar.addComponent(this.localeSelect);
        addComponent(titlebar);

        FormLayout translatedContent = new FormLayout();
        locales.forEach(locale -> {
            List<TranslatedField<T>> fields = fieldProvider.apply(locale);
            fields.stream().map(TranslatedField::getComponent).forEach(translatedContent::addComponent);
            this.translatedFields.addAll(fields);
        });
        addComponentsAndExpand(translatedContent);

        locales.stream().findFirst().ifPresent(this::selectPanel);
    }

    private void selectPanel(Locale locale) {
        this.localeSelect.setValue(locale);
        this.translatedFields.forEach(field -> {
            field.getComponent().setVisible(locale == null || field.getLocale() == locale);
        });
    }

    public void setTitle(String title) {
        this.title.setValue(title);
    }

    public void bind(Binder<T> binder) {
        this.translatedFields.forEach(field -> {
            binder.forField(field.getComponent())
                    .withValidator(value -> !field.getLocale().isFallback() || StringUtils.isNotBlank(value),
                            "Default language content is required")
                    .bind(data -> field.getMapProvider().apply(data).get(field.getLocale().getCode()),
                            (data, name) -> {
                                if (StringUtils.isNotBlank(name)) {
                                    field.getMapProvider().apply(data).put(field.getLocale().getCode(), name);
                                }
                            });
        });
    }

    @Getter
    public static class TranslatedField<T> {

        private final Locale locale;
        private final AbstractField<String> component;
        private final ValueProvider<T, Map<String, String>> mapProvider;

        public TranslatedField(
                Locale locale,
                AbstractField<String> fieldComponent,
                ValueProvider<T, Map<String, String>> translationMapProvider) {
            this.locale = locale;
            this.mapProvider = translationMapProvider;
            this.component = fieldComponent;
            this.component.setCaption(this.component.getCaption() + " (" + this.locale.getCode() + ")");
        }
    }
}
