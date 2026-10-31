package com.tbscg.doosan.magnolia.pmd.ui;

import com.tbscg.doosan.magnolia.pmd.data.model.Document;
import com.tbscg.doosan.magnolia.pmd.data.model.DocumentData;
import com.tbscg.doosan.magnolia.pmd.data.model.DocumentTranslation;
import com.tbscg.doosan.magnolia.pmd.data.model.DocumentType;
import com.tbscg.doosan.magnolia.pmd.data.provider.api.ApiService;
import com.tbscg.doosan.magnolia.pmd.ui.MultilanguageContent.TranslatedField;
import com.vaadin.data.Binder;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.*;
import lombok.Getter;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static java.util.Arrays.asList;

@Getter
public class DocumentEditForm extends EditForm<DocumentData> {

    private final VerticalLayout layout = new VerticalLayout();
    private final ComboBox<DocumentType> type;
    private final MultilanguageContent<DocumentData> translatedContent;
    private final ApiService apiService;
    private final List<DocumentType> documentTypes;

    private DocumentEditForm(ApiService apiService, DocumentData data) {
        super(data);
        this.apiService = apiService;
        this.documentTypes = apiService.getDocumentTypes();
        this.type = new ComboBox<>("Type");
        this.type.setItemCaptionGenerator(DocumentType::getName);
        this.type.setItems(this.documentTypes);
        this.type.setEmptySelectionAllowed(false);
        this.type.setWidth(200, Sizeable.Unit.PIXELS);
        this.layout.addComponent(new FormLayout(type));

        this.translatedContent = new MultilanguageContent<>(apiService.getLocales(),
            locale -> asList(
                new TranslatedField<>(locale, new TextField("Title"), DocumentData::getTitle),
                new TranslatedField<>(locale, new TextField("Description"), DocumentData::getDescription)
            )
        );
        this.layout.addComponent(this.translatedContent);
    }

    public static EditWindow<DocumentData> newDocument(ApiService apiService, Consumer<Document> callback) {
        DocumentEditForm form = new DocumentEditForm(apiService, new DocumentData());
        return new EditWindow<>("Add new document", form, data -> {
            Document newDoc = apiService.addDocument(data);
            if (callback != null) {
                callback.accept(newDoc);
            }
        });
    }

    public static EditWindow<DocumentData> editDocument(ApiService apiService, Document document, Consumer<Document> callback) {
        List<DocumentTranslation> translations = apiService.getDocumentTranslations(document.getId());
        DocumentData documentData = new DocumentData(document, translations);
        DocumentEditForm form = new DocumentEditForm(apiService, documentData);
        return new EditWindow<>("Edit document", form, data -> {
            Document newDoc = apiService.updateDocument(document.getId(), data);
            if (callback != null) {
                callback.accept(newDoc);
            }
        });
    }

    @Override
    public Component asComponent() {
        return this.layout;
    }

    @Override
    protected void bind(Binder<DocumentData> binder) {
        binder.forField(this.type)
                .withValidator(Objects::nonNull, "Type is required")
                .bind(data -> this.documentTypes.stream()
                                .filter(t -> t.getId() == data.getTypeId())
                                .findFirst().orElse(null),
                        (data, type) -> data.setTypeId(type.getId()));

        this.translatedContent.bind(binder);
    }
}
