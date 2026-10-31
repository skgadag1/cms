package com.tbscg.doosan.magnolia.concepts.action.ui;

import com.tbscg.doosan.magnolia.concepts.action.ui.validators.ChipSimpleTextValidator;
import com.tbscg.doosan.magnolia.concepts.action.ui.validators.CompositeValidator;
import com.tbscg.doosan.magnolia.concepts.action.ui.validators.ProhibitToChangeNameValidator;
import com.tbscg.doosan.magnolia.concepts.action.ui.validators.UniqueConceptNameValidator;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.availability.AvailabilityChecker;
import info.magnolia.ui.contentapp.definition.EditorDefinition;
import info.magnolia.ui.contentapp.detail.DetailPresenter;
import info.magnolia.ui.contentapp.detail.DetailView;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.form.FormPresenter;
import info.magnolia.ui.framework.app.SubAppActionExecutor;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

public class ConceptsDetailPresenter extends DetailPresenter {
    private CompositeValidator validator;

    public ConceptsDetailPresenter(SubAppContext subAppContext, EventBus eventBus, DetailView view, ComponentProvider componentProvider, SubAppActionExecutor executor, I18nizer i18nizer, SimpleTranslator i18n, AvailabilityChecker checker, ContentConnector contentConnector, FormPresenter formPresenter) {
        super(subAppContext, eventBus, view, componentProvider, executor, i18nizer, i18n, checker, contentConnector, formPresenter);
    }

    @Override
    public DetailView start(EditorDefinition editorDefinition, DetailView.ViewType viewType, Object itemId) {
        final DetailView view = super.start(editorDefinition, viewType, itemId);

        if (editorDefinition instanceof ConceptsEditorDefinition) {
            // enable validation if concept form
            final JcrNodeAdapter item = (JcrNodeAdapter) getItem();
            this.validator = new CompositeValidator(new EditorValidator[]{
                    new ProhibitToChangeNameValidator(item, this),
                    new ChipSimpleTextValidator(item, this),
                    new UniqueConceptNameValidator(item, this)}
            );
        }

        return view;
    }

    @Override
    public boolean isValid() {
        if (this.validator == null) {
            return super.isValid();
        }

        return super.isValid() && this.validator.isValid();
    }
}
