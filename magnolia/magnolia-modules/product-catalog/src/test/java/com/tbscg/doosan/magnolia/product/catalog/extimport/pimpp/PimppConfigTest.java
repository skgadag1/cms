package com.tbscg.doosan.magnolia.product.catalog.extimport.pimpp;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class PimppConfigTest {

    @Test
    public void copy() {
        // given
        final PimppConfig pimppConfig = new PimppConfig();
        pimppConfig.setEndpoint("endpoint");

        // when
        final PimppConfig copy = pimppConfig.copy();

        // then
        assertThat(copy, equalTo(pimppConfig));
        assertThat(copy, not(sameInstance(pimppConfig)));
    }

}
