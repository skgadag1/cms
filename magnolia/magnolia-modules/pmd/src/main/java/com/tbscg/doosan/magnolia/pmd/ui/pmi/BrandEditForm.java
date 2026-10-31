package com.tbscg.doosan.magnolia.pmd.ui.pmi;

import com.tbscg.doosan.magnolia.pmd.data.model.Brand;
import com.tbscg.doosan.magnolia.pmd.data.model.NameTranslation;
import com.tbscg.doosan.magnolia.pmd.data.model.PmiData;
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
import java.util.function.Consumer;

import static java.util.Collections.singletonList;

public class BrandEditForm extends EditForm<PmiData> {

    private final VerticalLayout layout = new VerticalLayout();
    private final TextField code = new TextField("Code");
    private final MultilanguageContent<PmiData> translatedNames;
    private final ApiService apiService;

    private BrandEditForm(ApiService apiService, PmiData data) {
        super(data);
        this.apiService = apiService;
        this.code.setWidthFull();
        this.layout.addComponent(new FormLayout(code));

        this.translatedNames = new MultilanguageContent<>(apiService.getLocales(),
            locale -> singletonList(
                new TranslatedField<>(locale, new TextField("Name"), PmiData::getName)
            )
        );
        this.layout.addComponent(this.translatedNames);
    }

    public static EditWindow<PmiData> newBrand(ApiService apiService, Consumer<Brand> callback) {
        BrandEditForm editForm = new BrandEditForm(apiService, new PmiData());
        return new EditWindow<>("New brand", editForm, data -> {
            Brand newBrand = apiService.addBrand(data);
            if (callback != null) {
                callback.accept(newBrand);
            }
        });
    }

    public static EditWindow<PmiData> editBrand(ApiService apiService, Brand brand, Consumer<Brand> callback) {
        List<NameTranslation> translations = apiService.getBrandTranslations(brand.getId());
        PmiData brandData = new PmiData(brand.getCode(), translations);
        BrandEditForm editForm = new BrandEditForm(apiService, brandData);
        return new EditWindow<>("Edit brand attributes", editForm, data -> {
            Brand updated = apiService.updateBrand(brand.getId(), data);
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
    protected void bind(Binder<PmiData> binder) {
        binder.forField(this.code)
                .withValidator(StringUtils::isNotBlank, "Code is required")
                .bind(PmiData::getCode, PmiData::setCode);

        this.translatedNames.bind(binder);
    }
}
