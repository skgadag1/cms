package com.tbscg.doosan.magnolia.product.catalog.extimport;

import org.junit.Test;

import static org.junit.Assert.*;

public class PimFilterTest {

    @Test(expected = NullPointerException.class)
    public void testBuilder_whenNoMandatoryParams() {
        PimFilter.builder().build();
    }

    @Test
    public void testBuilder_whenOnlyMandatoryParams() {
        // when
        PimFilter filter = PimFilter.builder().modelType(PimModelType.PRODUCT).build();
        // then
        assertNotNull(filter.getModelType());
        assertFalse(filter.isIncludeHistorical());
        assertEquals(PimModelType.PRODUCT, filter.getModelType());
    }

    @Test(expected = NullPointerException.class)
    public void testBuilder_whenNoModelType() {
        PimFilter.builder().includeHistorical(true).build();
    }

    @Test
    public void testBuilder_shouldBuildOK() {
        // when
        PimFilter filter = PimFilter.builder().includeHistorical(true).modelType(PimModelType.ATTACHMENT).build();
        // then
        assertNotNull(filter.getModelType());
        assertTrue(filter.isIncludeHistorical());
        assertEquals(PimModelType.ATTACHMENT, filter.getModelType());
    }

}