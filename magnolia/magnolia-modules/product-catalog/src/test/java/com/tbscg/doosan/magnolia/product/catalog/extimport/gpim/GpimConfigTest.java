package com.tbscg.doosan.magnolia.product.catalog.extimport.gpim;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class GpimConfigTest {

    @Test
    public void copy() {
        // given
        final GpimConfig gpimConfig = new GpimConfig();
        gpimConfig.setEndpoint("endpoint");

        // when
        final GpimConfig copy = gpimConfig.copy();

        // then
        assertThat(copy, equalTo(gpimConfig));
        assertThat(copy, not(sameInstance(gpimConfig)));
    }

}
