
package com.tbscg.doosan.magnolia.pmd.ui.pmi;

import com.tbscg.doosan.magnolia.pmd.data.model.Brand;
import com.tbscg.doosan.magnolia.pmd.data.model.Family;
import com.tbscg.doosan.magnolia.pmd.data.model.FamilyData;
import com.tbscg.doosan.magnolia.pmd.data.model.NameTranslation;
import com.tbscg.doosan.magnolia.pmd.data.provider.api.ApiService;
import com.tbscg.doosan.magnolia.pmd.ui.EditForm;
import com.tbscg.doosan.magnolia.pmd.ui.EditWindow;
import com.tbscg.doosan.magnolia.pmd.ui.MultilanguageContent;
import com.tbscg.doosan.magnolia.pmd.ui.MultilanguageContent.TranslatedField;
import com.vaadin.data.Binder;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static java.util.Collections.singletonList;

public class FamilyEditForm extends EditForm<FamilyData> {

    private final VerticalLayout layout = new VerticalLayout();
    private List<Brand> availableBrands = Collections.emptyList();
    private final ComboBox<Brand> brand = new ComboBox<>("Brand");
    private final TextField code = new TextField("Code");
    private final MultilanguageContent<FamilyData> translatedNames;
    private final ApiService apiService;

    private FamilyEditForm(ApiService apiService, FamilyData data) {
        super(data);
        this.apiService = apiService;
        this.availableBrands = apiService.getBrands();
        this.brand.setItems(this.availableBrands);
        this.brand.setItemCaptionGenerator(Brand::getName);
        this.brand.setEmptySelectionAllowed(false);

        this.code.setWidthFull();
        this.layout.addComponent(new FormLayout(this.brand, this.code));

        this.translatedNames = new MultilanguageContent<>(apiService.getLocales(),
            locale -> singletonList(
                new TranslatedField<>(locale, new TextField("Name"), FamilyData::getName)
            )
        );
        this.layout.addComponent(this.translatedNames);
    }

    public static EditWindow<FamilyData> newFamily(ApiService apiService, Brand brand, Consumer<Family> callback) {
        FamilyEditForm editForm = new FamilyEditForm(apiService, new FamilyData().withBrandId(brand.getId()));
        return new EditWindow<>("New family", editForm, data -> {
            Family newFamily = apiService.addFamily(data);
            if (callback != null) {
                callback.accept(newFamily);
            }
        });
    }

    public static EditWindow<FamilyData> editFamily(ApiService apiService, Family family, Consumer<Family> callback) {
        List<NameTranslation> translations = apiService.getFamilyTranslations(family.getId());
        FamilyData familyData = new FamilyData(family, translations);
        FamilyEditForm editForm = new FamilyEditForm(apiService, familyData);
        return new EditWindow<>("Edit family attributes", editForm, data -> {
            Family updated = apiService.updateFamily(family.getId(), data);
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
    protected void bind(Binder<FamilyData> binder) {
        binder.forField(this.brand)
                .withValidator(Objects::nonNull, "Brand is required")
                .bind(data -> this.apiService.getBrand(data.getBrandId()),
                        (data, brand) -> data.setBrandId(brand.getId()));

        binder.forField(this.code)
                .withValidator(StringUtils::isNotBlank, "Code is required")
                .bind(FamilyData::getCode, FamilyData::setCode);

        this.translatedNames.bind(binder);
    }
}
