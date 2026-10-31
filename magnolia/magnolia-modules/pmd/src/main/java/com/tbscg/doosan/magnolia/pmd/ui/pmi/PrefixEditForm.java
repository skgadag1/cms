package com.tbscg.doosan.magnolia.pmd.ui.pmi;

import com.tbscg.doosan.magnolia.pmd.data.model.Model;
import com.tbscg.doosan.magnolia.pmd.data.model.Prefix;
import com.tbscg.doosan.magnolia.pmd.data.model.PrefixData;
import com.tbscg.doosan.magnolia.pmd.data.provider.api.ApiService;
import com.tbscg.doosan.magnolia.pmd.ui.EditForm;
import com.tbscg.doosan.magnolia.pmd.ui.EditWindow;
import com.vaadin.data.Binder;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class PrefixEditForm extends EditForm<PrefixData> {

    private final VerticalLayout layout = new VerticalLayout();
    private final PmiSelectorTree model;
    private final TextField prefix = new TextField("Prefix");
    private final ApiService apiService;

    private PrefixEditForm(ApiService apiService, PrefixData data) {
        super(data);
        this.apiService = apiService;

        this.model = new PmiSelectorTree(apiService, Model.class);
        this.model.setCaption("Select model");
        this.model.setWidth(300, Sizeable.Unit.PIXELS);
        this.model.setHeight(300, Sizeable.Unit.PIXELS);

        this.prefix.setWidthFull();
        this.layout.addComponent(new FormLayout(this.model, this.prefix));
    }

    private static final Pattern PREFIX_PATTERN = Pattern.compile(
            "^[a-z0-9]{4}|[a-z0-9]{7}$",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    protected void bind(Binder<PrefixData> binder) {
        SerializablePredicate<String> validatePrefix = s -> StringUtils.isNotBlank(s)
                && PREFIX_PATTERN.matcher(s).matches();

        binder.forField(this.model)
                .withValidator(Objects::nonNull, "Model is required")
                .bind(data -> this.apiService.getModel(data.getModelId()),
                        (data, model) -> data.setModelId(model.getId()));

        binder.forField(this.prefix)
                .withValidator(validatePrefix, "Invalid format")
                .bind(PrefixData::getPrefix,
                        (data, prefix) -> data.setPrefix(prefix.toUpperCase()));
    }

    public static EditWindow<PrefixData> newPrefix(ApiService apiService, Model model, Consumer<Prefix> callback) {
        PrefixEditForm editForm = new PrefixEditForm(apiService, new PrefixData().withModelId(model.getId()));
        return new EditWindow<>("New prefix", editForm, data -> {
            Prefix newPrefix = apiService.addPrefix(data);
            if (callback != null) {
                callback.accept(newPrefix);
            }
        });
    }

    public static EditWindow<PrefixData> editPrefix(ApiService apiService, Prefix prefix, Consumer<Prefix> callback) {
        PrefixData prefixData = new PrefixData(prefix);
        PrefixEditForm editForm = new PrefixEditForm(apiService, prefixData);
        return new EditWindow<>("Edit prefix attributes", editForm, data -> {
            Prefix updated = apiService.updatePrefix(prefix.getId(), data);
            if (callback != null) {
                callback.accept(updated);
            }
        });
    }

    @Override
    public Component asComponent() {
        return this.layout;
    }
}
