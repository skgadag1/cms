package com.tbscg.doosan.magnolia.dxp.workflow.service.indexing;

import info.magnolia.context.MgnlContext;
import info.magnolia.objectfactory.Components;
import info.magnolia.test.mock.MockComponentProvider;
import org.junit.After;
import org.junit.Before;

public abstract class BaseIndexingServiceTest {

    @Before
    public void setUpComponentProvider() {
        Components.setComponentProvider(new MockComponentProvider());
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
        Components.setComponentProvider(null);
    }
}
