package com.tbscg.doosan.magnolia.translation.setup;

import com.tbscg.doosan.magnolia.dxp.core.setup.AutomaticDefaultDxpVersionHandler;
import com.tbscg.doosan.magnolia.dxp.core.setup.task.EnvironmentAwareTask;
import com.tbscg.doosan.magnolia.dxp.core.util.EnvironmentConstants;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.repository.RepositoryConstants;

/**
 * Version handler for translation-ext-config module.
 * This handler forces bootstrapping of all JCR files during module installation
 * and updates. It ensures that the Google Translation provider configuration
 * is always up-to-date in the Magnolia config workspace.
 */
public class TranslationExtConfigVersionHandler extends AutomaticDefaultDxpVersionHandler {
    public TranslationExtConfigVersionHandler() {
        super();
            register(DeltaBuilder.update("1.0.2",
                            "Bootstrap translation-ext-config module for public instance")
                    .addTask(
                            new EnvironmentAwareTask(
                                    "Remove translation events listener configuration for public instance",
                                    "Removes the translation events listener configuration as it is not needed for public instance",
                                    EnvironmentConstants.PUBLIC_INSTANCE,
                                    EnvironmentConstants.SHARED_ENVIRONMENT,
                                    new RemoveNodeTask(
                                            "Remove translation events listener configuration",
                                            "Removes the translation events listener configuration",
                                            RepositoryConstants.CONFIG,
                                            "/modules/observation/config/listenerConfigurations/translationEvents"))
                    )
            );
    }
}
