package com.tbscg.doosan.magnolia.toc.service;

import com.tbscg.doosan.magnolia.dxp.workflow.service.SqsServiceImpl;
import com.tbscg.doosan.magnolia.toc.TocModule;
import info.magnolia.objectfactory.annotation.LazySingleton;

import javax.inject.Inject;

/**
 *  SQS service implementation {@link com.tbscg.doosan.magnolia.dxp.workflow.service.SqsService} to inject
 *  ToC module configuration.
 *
 * @author apuigescobar
 */
@LazySingleton
public class TocSqsService extends SqsServiceImpl {
    @Inject
    public TocSqsService(TocModule tocConfig) {
        super(tocConfig);
    }
}
