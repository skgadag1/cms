package com.tbscg.doosan.magnolia.cloudinary.contentapp;

import com.tbscg.doosan.magnolia.dxp.core.util.ReflectionUtil;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.AppView;
import info.magnolia.ui.api.app.ChooseDialogCallback;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.contentapp.ContentApp;
import info.magnolia.ui.contentapp.ContentAppDescriptor;
import info.magnolia.ui.contentapp.choosedialog.ChooseDialogComponentProviderUtil;
import info.magnolia.ui.dialog.choosedialog.ChooseDialogPresenter;
import info.magnolia.ui.dialog.definition.ChooseDialogDefinition;
import info.magnolia.ui.dialog.definition.ConfiguredChooseDialogDefinition;
import info.magnolia.ui.framework.ioc.SessionStore;
import info.magnolia.ui.framework.ioc.UiContextBoundComponentProvider;
import info.magnolia.ui.framework.ioc.UiContextReference;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.reflect.Method;

/**
 * Customization of Magnolia UI v5 content app class which leverages
 * the {@link CustomChooseDialogCallback} class to change the asset chooser behavior.
 */
public class CustomContentApp extends ContentApp {

    private static final Logger log = LoggerFactory.getLogger(CustomContentApp.class);

    @Inject
    public CustomContentApp(AppContext appContext, AppView view) {
        super(appContext, view);
    }

    @Override
    public void openChooseDialog(UiContext overlayLayer, String targetTreeRootPath, String selectedId, final ChooseDialogCallback callback) {

        // fetch or compute chooseDialogDefinition from default subApp
        ChooseDialogDefinition chooseDialogDefinition;
        if (appContext.getAppDescriptor() instanceof ContentAppDescriptor) {
            chooseDialogDefinition = ((ContentAppDescriptor) appContext.getAppDescriptor()).getChooseDialog();
        } else {
            chooseDialogDefinition = new ConfiguredChooseDialogDefinition();
        }
        chooseDialogDefinition = ensureChooseDialogField(chooseDialogDefinition, targetTreeRootPath);
        chooseDialogDefinition = addAvailabilityActionRenderer(chooseDialogDefinition);

        if (callback instanceof CustomChooseDialogCallback) {
            CustomChooseDialogCallback customCallback = (CustomChooseDialogCallback) callback;
            customCallback.customizeChooseDialog(chooseDialogDefinition.getContentConnector());
        }

        // create chooseDialogComponentProvider and get new instance of presenter from there
        final UiContextReference stubSubAppContextReference = createStubSubAppContextReference(appContext.getDefaultSubAppDescriptor().getName());
        final UiContextBoundComponentProvider parentSubAppComponentProvider = new UiContextBoundComponentProvider(stubSubAppContextReference);

        final UiContextBoundComponentProvider chooseDialogComponentProvider =
                ChooseDialogComponentProviderUtil.createChooseDialogComponentProvider(overlayLayer, chooseDialogDefinition, parentSubAppComponentProvider);
        final ChooseDialogPresenter presenter = chooseDialogComponentProvider.newInstance(chooseDialogDefinition.getPresenterClass(), chooseDialogComponentProvider);

        if (!StringUtils.isBlank(targetTreeRootPath) && !StringUtils.isBlank(selectedId) && !"/".equals(targetTreeRootPath) && selectedId.startsWith(targetTreeRootPath)) {
            selectedId = StringUtils.removeStart(selectedId, targetTreeRootPath);
        }

        // Wrap the dialog callback with a bean store destruction trigger
        final UiContextReference uiContextReference = chooseDialogComponentProvider.getUiContextReference();
        final ChooseDialogCallback composedCallback = ChooseDialogCallback.composeWith(() -> SessionStore.access().releaseBeanStore(uiContextReference), callback);
        presenter.start(composedCallback, chooseDialogDefinition, overlayLayer, selectedId);
        presenter.getView().getActionAreaView().getViewForAction("cancel").asVaadinComponent().removeStyleName("cancel");
    }

    /***************************************************************/
    /** superclass private method calls to avoid code duplication: */
    private ChooseDialogDefinition ensureChooseDialogField(ChooseDialogDefinition definition, String targetTreeRootPath) {
        Method method = ReflectionUtil.getMethod(ContentApp.class, "ensureChooseDialogField", ChooseDialogDefinition.class, String.class);
        return ReflectionUtil.invoke(method, this, definition, targetTreeRootPath);
    }

    private ChooseDialogDefinition addAvailabilityActionRenderer(ChooseDialogDefinition definition) {
        Method method = ReflectionUtil.getMethod(ContentApp.class, "addAvailabilityActionRenderer", ChooseDialogDefinition.class);
        return ReflectionUtil.invoke(method, this, definition);
    }

    private UiContextReference createStubSubAppContextReference(String subAppName) {
        Method method = ReflectionUtil.getMethod(ContentApp.class, "createStubSubAppContextReference", String.class);
        return ReflectionUtil.invoke(method, this, subAppName);
    }
    /***************************************************************/
}
