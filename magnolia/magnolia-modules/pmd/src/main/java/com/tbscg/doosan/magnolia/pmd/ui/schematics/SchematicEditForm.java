
package com.tbscg.doosan.magnolia.pmd.ui.schematics;

import com.tbscg.doosan.magnolia.pmd.data.model.Schematic;
import com.tbscg.doosan.magnolia.pmd.data.model.SchematicData;
import com.tbscg.doosan.magnolia.pmd.data.model.SchematicTranslation;
import com.tbscg.doosan.magnolia.pmd.data.model.SchematicType;
import com.tbscg.doosan.magnolia.pmd.data.provider.api.ApiService;
import com.tbscg.doosan.magnolia.pmd.ui.EditForm;
import com.tbscg.doosan.magnolia.pmd.ui.EditWindow;
import com.tbscg.doosan.magnolia.pmd.ui.MultilanguageContent;
import com.tbscg.doosan.magnolia.pmd.ui.MultilanguageContent.TranslatedField;
import com.vaadin.data.Binder;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.*;
import lombok.Getter;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static java.util.Collections.singletonList;

@Getter
public class SchematicEditForm extends EditForm<SchematicData> {

    private final VerticalLayout layout = new VerticalLayout();
    private final ComboBox<SchematicType> type = new ComboBox<>("Type");
    private final TextArea partNumbers = new TextArea("Part numbers");
    private final MultilanguageContent<SchematicData> translatedNames;
    private final ApiService apiService;
    private final List<SchematicType> schematicTypes;

    private SchematicEditForm(ApiService apiService, SchematicData data) {
        super(data);
        this.apiService = apiService;
        this.schematicTypes = apiService.getSchematicTypes();

        this.type.setItemCaptionGenerator(SchematicType::getName);
        this.type.setItems(this.schematicTypes);
        this.type.setEmptySelectionAllowed(false);
        this.type.setWidth(200, Sizeable.Unit.PIXELS);

        this.partNumbers.setWidthFull();
        this.partNumbers.setRows(3);

        this.layout.addComponent(new FormLayout(type, partNumbers));

        this.translatedNames = new MultilanguageContent<>(apiService.getLocales(),
            locale -> singletonList(
                new TranslatedField<>(locale, new TextField("Name"), SchematicData::getName)
            )
        );
        this.layout.addComponent(this.translatedNames);
    }

    public static EditWindow<SchematicData> newSchematic(ApiService apiService, Consumer<Schematic> callback) {
        SchematicEditForm editForm = new SchematicEditForm(apiService, new SchematicData());
        return new EditWindow<>("New schematic", editForm, data -> {
            Schematic newSchematic = apiService.addSchematic(data);
            if (callback != null) {
                callback.accept(newSchematic);
            }
        });
    }

    public static EditWindow<SchematicData> editSchematic(ApiService apiService, Schematic schematic, Consumer<Schematic> callback) {
        List<SchematicTranslation> translations = apiService.getSchematicTranslations(schematic.getId());
        SchematicData schematicData = new SchematicData(schematic, translations);
        SchematicEditForm editForm = new SchematicEditForm(apiService, schematicData);
        return new EditWindow<>("Edit schematic attributes", editForm, data -> {
            Schematic updated = apiService.updateSchematic(schematic.getId(), data);
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
    protected void bind(Binder<SchematicData> binder) {
        binder.forField(this.type)
                .withValidator(Objects::nonNull, "Type is required")
                .bind(data -> this.apiService.getSchematicTypes().stream()
                                .filter(t -> t.getId() == data.getTypeId())
                                .findFirst().orElse(null),
                     (data, type) -> data.setTypeId(type.getId()));

        binder.forField(this.partNumbers)
                .bind(SchematicData::getPartNumbers, SchematicData::setPartNumbers);

        this.translatedNames.bind(binder);
    }
}
