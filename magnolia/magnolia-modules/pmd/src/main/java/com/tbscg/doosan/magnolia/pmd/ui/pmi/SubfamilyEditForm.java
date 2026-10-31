package com.tbscg.doosan.magnolia.pmd.ui.pmi;

import com.tbscg.doosan.magnolia.pmd.data.model.Family;
import com.tbscg.doosan.magnolia.pmd.data.model.NameTranslation;
import com.tbscg.doosan.magnolia.pmd.data.model.Subfamily;
import com.tbscg.doosan.magnolia.pmd.data.model.SubfamilyData;
import com.tbscg.doosan.magnolia.pmd.data.provider.api.ApiService;
import com.tbscg.doosan.magnolia.pmd.ui.EditForm;
import com.tbscg.doosan.magnolia.pmd.ui.EditWindow;
import com.tbscg.doosan.magnolia.pmd.ui.MultilanguageContent;
import com.tbscg.doosan.magnolia.pmd.ui.MultilanguageContent.TranslatedField;
import com.vaadin.data.Binder;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static java.util.Collections.singletonList;

public class SubfamilyEditForm extends EditForm<SubfamilyData> {

    private final VerticalLayout layout = new VerticalLayout();
    private final PmiSelectorTree family;
    private final TextField code = new TextField("Code");
    private final MultilanguageContent<SubfamilyData> translatedNames;
    private final ApiService apiService;

    private SubfamilyEditForm(ApiService apiService, SubfamilyData data) {
        super(data);
        this.apiService = apiService;

        this.family = new PmiSelectorTree(apiService, Family.class);
        this.family.setCaption("Select family");
        this.family.setWidth(300, Sizeable.Unit.PIXELS);
        this.family.setHeight(300, Sizeable.Unit.PIXELS);

        this.code.setWidthFull();
        this.layout.addComponent(new FormLayout(this.family, this.code));

        this.translatedNames = new MultilanguageContent<>(apiService.getLocales(),
            locale -> singletonList(
                new TranslatedField<>(locale, new TextField("Name"), SubfamilyData::getName)
            )
        );
        this.layout.addComponent(this.translatedNames);
    }

    public static EditWindow<SubfamilyData> newSubfamily(ApiService apiService, Family family, Consumer<Subfamily> callback) {
        SubfamilyData subfamilyData = new SubfamilyData().withFamilyId(family.getId());
        SubfamilyEditForm editForm = new SubfamilyEditForm(apiService, subfamilyData);
        return new EditWindow<>("New subfamily", editForm, data -> {
            Subfamily newSubfamily = apiService.addSubfamily(data);
            if (callback != null) {
                callback.accept(newSubfamily);
            }
        });
    }

    public static EditWindow<SubfamilyData> editSubfamily(ApiService apiService, Subfamily subfamily, Consumer<Subfamily> callback) {
        List<NameTranslation> translations = apiService.getSubfamilyTranslations(subfamily.getId());
        SubfamilyData subfamilyData = new SubfamilyData(subfamily, translations);
        SubfamilyEditForm editForm = new SubfamilyEditForm(apiService, subfamilyData);
        return new EditWindow<>("Edit subfamily attributes", editForm, data -> {
            Subfamily updated = apiService.updateSubfamily(subfamily.getId(), data);
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
    protected void bind(final Binder<SubfamilyData> binder) {
        binder.forField(this.family)
                .withValidator(Objects::nonNull, "Family is required")
                .bind(data -> this.apiService.getFamily(data.getFamilyId()),
                        (data, family) -> data.setFamilyId(family.getId()));

        binder.forField(this.code)
                .withValidator(StringUtils::isNotBlank, "Code is required")
                .bind(SubfamilyData::getCode, SubfamilyData::setCode);

        this.translatedNames.bind(binder);
    }
}
