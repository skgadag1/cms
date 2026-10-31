package com.tbscg.doosan.magnolia.dxp.core.translation.finder;

import info.magnolia.context.Context;
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.translation.TranslationModule;
import info.magnolia.translation.finder.AbstractPropertiesToTranslateFinder;
import info.magnolia.translation.finder.PropertyToTranslate;
import info.magnolia.ui.api.app.AppDescriptor;
import info.magnolia.ui.api.app.registry.AppDescriptorRegistry;
import info.magnolia.ui.contentapp.detail.DetailDescriptor;
import info.magnolia.ui.contentapp.detail.DetailSubAppDescriptor;
import info.magnolia.ui.dialog.ConfiguredFormDialogDefinition;
import info.magnolia.ui.dialog.DialogDefinition;
import info.magnolia.ui.dialog.DialogDefinitionRegistry;
import info.magnolia.ui.editor.FormDefinition;
import info.magnolia.ui.form.definition.DefinitionConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import javax.inject.Inject;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.ArrayList;
import java.util.List;

/**
 * Implemented base on info.magnolia.translation.finder.DetailPresenterPropertiesToTranslateFinder
 */
@Slf4j
public class CustomDetailPresenterPropertiesToTranslateFinder extends AbstractPropertiesToTranslateFinder {

    private final DialogDefinitionRegistry dialogDefinitionRegistry;
    private final AppDescriptorRegistry appRegistry;
    private final Context context;
    private final String formPath;

    @Inject
    public CustomDetailPresenterPropertiesToTranslateFinder(TranslationModule module,
                                                            I18nizer i18nizer,
                                                            AppDescriptorRegistry appRegistry,
                                                            String formPath,
                                                            Context context,
                                                            DialogDefinitionRegistry dialogDefinitionRegistry) {

        super(module, i18nizer);
        this.dialogDefinitionRegistry = dialogDefinitionRegistry;
        this.context = context;
        this.formPath = formPath;
        this.appRegistry = appRegistry;

    }

    public List<PropertyToTranslate> getPropertiesToTranslate(Node node) {
        try {
            final Session configSession = context.getJCRSession(RepositoryConstants.CONFIG);
            if (configSession.nodeExists(formPath)) {
                Node detailSubAppNode = configSession.getNode(formPath);
                Item appNode = detailSubAppNode.getAncestor(4);
                String appName = appNode.getName();
                String subAppName = detailSubAppNode.getParent().getParent().getName();
                return findForm(appName, subAppName);
            } else if (formPath.contains("app")) {
                // This is a replica of what happens above, basically given the formPath we try to determine the subapp
                // app names and attempt to find the form with those information.
                final String[] pathSegments = StringUtils.split(formPath, "/");
                if (pathSegments.length > 5) {
                    return findForm(pathSegments[3], pathSegments[5]);
                }
            } else if (formPath.contains("dialogs")) {
                /**
                 * Additional condition (in comparison to original implementation) dedicated to Product Catalog App
                 * which doesn't have Detail sub app defined and where all content entities are managed through
                 * separate actions calling dialogs with corresponding form definitions. In this case in formPath
                 * we are passing paths not to Details sub apps but to corresponding dialog form definitions.
                 */
                final String[] pathSegments = StringUtils.split(formPath, "/");
                if (pathSegments.length > 3) {
                    return findFormInDialog(pathSegments[1], pathSegments[3]);
                }
            }
            else {
                    log.error("Form path '{}' doesn't exist", formPath);
            }
        } catch (RepositoryException e) {
            log.error("Could not find form definition for given path {}", formPath, e);
        }
        return new ArrayList<>();
    }

    private List<PropertyToTranslate> findFormInDialog(String appName, String dialogName) throws RepositoryException {
        String referenceId = appName + ":" + dialogName;
        DialogDefinition dialogDefinition = this.dialogDefinitionRegistry.getProvider(referenceId).get();
        FormDefinition form = ((ConfiguredFormDialogDefinition) dialogDefinition).getForm();
        return this.processForm(this.i18nizer.decorate(form), appName);
    }

    private List<PropertyToTranslate> findForm(String appName, String subAppName) throws RepositoryException {
        final AppDescriptor addDescriptor = this.appRegistry.getProvider(appName).get();
        Object subappDescriptor = addDescriptor.getSubApps().get(subAppName);
        FormDefinition decoratedForm = null;
        if(subappDescriptor != null && subappDescriptor instanceof DetailSubAppDescriptor) {
            final DetailSubAppDescriptor detailSubAppDescriptor = (DetailSubAppDescriptor) addDescriptor.getSubApps()
                    .get(subAppName);

            decoratedForm = DefinitionConverter.FORM.convert(detailSubAppDescriptor.getEditor().getForm());
        } else if(subappDescriptor != null && subappDescriptor instanceof DetailDescriptor) {
            decoratedForm = ((DetailDescriptor)subappDescriptor).getForm();
        }
        return this.processForm(this.i18nizer.decorate(decoratedForm), appName);
    }

}
