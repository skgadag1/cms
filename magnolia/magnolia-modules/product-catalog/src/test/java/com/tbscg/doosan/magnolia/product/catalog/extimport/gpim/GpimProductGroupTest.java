package com.tbscg.doosan.magnolia.product.catalog.extimport.gpim;

import org.junit.Test;

import static org.junit.Assert.*;

public class GpimProductGroupTest {

    @Test
    public void testGetLifecycle() {
        assertEquals("Historical", GpimProductGroup.builder().build().getLifecycle());
        assertEquals("Historical", GpimProductGroup.builder().visibility("unknown").build().getLifecycle());
        assertEquals("Historical", GpimProductGroup.builder().lifecycleStatus("unknown").build().getLifecycle());
        assertEquals("Historical", GpimProductGroup.builder().lifecycleStatus("unknown").visibility("unknown").build().getLifecycle());
        assertEquals("Historical", GpimProductGroup.builder().lifecycleStatus("unknown").visibility("PUBLIC").build().getLifecycle());
        assertEquals("Historical", GpimProductGroup.builder().lifecycleStatus("ACTIVE").visibility("unknown").build().getLifecycle());
        assertEquals("Current", GpimProductGroup.builder().lifecycleStatus("ACTIVE").visibility("PUBLIC").build().getLifecycle());
    }

    @Test
    public void testIsActivePublic() {
        assertFalse(GpimProductGroup.builder().build().isActivePublic());
        assertFalse(GpimProductGroup.builder().visibility("unknown").build().isActivePublic());
        assertFalse(GpimProductGroup.builder().lifecycleStatus("unknown").build().isActivePublic());
        assertFalse(GpimProductGroup.builder().lifecycleStatus("unknown").visibility("unknown").build().isActivePublic());
        assertFalse(GpimProductGroup.builder().lifecycleStatus("unknown").visibility("PUBLIC").build().isActivePublic());
        assertFalse(GpimProductGroup.builder().lifecycleStatus("ACTIVE").visibility("unknown").build().isActivePublic());
        assertTrue(GpimProductGroup.builder().lifecycleStatus("ACTIVE").visibility("PUBLIC").build().isActivePublic());
    }
}