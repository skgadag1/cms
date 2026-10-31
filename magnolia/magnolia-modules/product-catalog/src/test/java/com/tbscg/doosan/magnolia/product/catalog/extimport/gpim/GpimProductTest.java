package com.tbscg.doosan.magnolia.product.catalog.extimport.gpim;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class GpimProductTest {

    @Test
    public void testGetLifecycle() {
        assertEquals("Historical", GpimProduct.builder().build().getLifecycle());
        assertEquals("Historical", GpimProduct.builder().visibility("unknown").build().getLifecycle());
        assertEquals("Historical", GpimProduct.builder().lifecycleStatus("unknown").build().getLifecycle());
        assertEquals("Historical", GpimProduct.builder().lifecycleStatus("unknown").visibility("unknown").build().getLifecycle());
        assertEquals("Historical", GpimProduct.builder().lifecycleStatus("unknown").visibility("PUBLIC").build().getLifecycle());
        assertEquals("Historical", GpimProduct.builder().lifecycleStatus("ACTIVE").visibility("unknown").build().getLifecycle());
        assertEquals("Current", GpimProduct.builder().lifecycleStatus("ACTIVE").visibility("PUBLIC").build().getLifecycle());
    }

    @Test
    public void testIsActivePublic() {
        assertFalse(GpimProduct.builder().build().isActivePublic());
        assertFalse(GpimProduct.builder().visibility("unknown").build().isActivePublic());
        assertFalse(GpimProduct.builder().lifecycleStatus("unknown").build().isActivePublic());
        assertFalse(GpimProduct.builder().lifecycleStatus("unknown").visibility("unknown").build().isActivePublic());
        assertFalse(GpimProduct.builder().lifecycleStatus("unknown").visibility("PUBLIC").build().isActivePublic());
        assertFalse(GpimProduct.builder().lifecycleStatus("ACTIVE").visibility("unknown").build().isActivePublic());
        assertTrue(GpimProduct.builder().lifecycleStatus("ACTIVE").visibility("PUBLIC").build().isActivePublic());
    }

    @Test
    public void testHasVariants() {
        assertFalse(GpimProduct.builder().build().hasVariants());
        assertFalse(GpimProduct.builder().variants(Collections.emptyList()).build().hasVariants());
        assertTrue(GpimProduct.builder().variants(Arrays.asList(GpimProduct.builder().build())).build().hasVariants());
    }
}