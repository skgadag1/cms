package com.tbscg.doosan.magnolia.localizations.service;

import com.tbscg.doosan.magnolia.dxp.workflow.service.SqsServiceImpl;
import com.tbscg.doosan.magnolia.localizations.LocalizationsModule;
import info.magnolia.objectfactory.annotation.LazySingleton;

import javax.inject.Inject;

/**
 * SQS service implementation {@link com.tbscg.doosan.magnolia.dxp.workflow.service.SqsService} to inject
 * localizations module configuration.
 *
 * @author apuigescobar
 */
@LazySingleton
public class LocalizationsSqsService extends SqsServiceImpl {
    @Inject
    public LocalizationsSqsService(LocalizationsModule localizationsConfig) {
        super(localizationsConfig);
    }
}
