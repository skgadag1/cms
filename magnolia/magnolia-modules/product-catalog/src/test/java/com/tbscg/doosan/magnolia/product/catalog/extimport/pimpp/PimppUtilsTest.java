package com.tbscg.doosan.magnolia.product.catalog.extimport.pimpp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PimppUtilsTest {

    @Test
    public void testParsePMI() {
        assertEquals("PIM1", PimppUtils.parsePMI("blah (PIM1 whatever)", "ignored"));
        assertEquals("PIM2", PimppUtils.parsePMI("blah (PIM2 whatever)", null));
        assertEquals("defaultPIM", PimppUtils.parsePMI("no PIM", "defaultPIM"));
        assertEquals(null, PimppUtils.parsePMI("no PIM)", null));
        assertEquals("defaultPIM", PimppUtils.parsePMI(null, "defaultPIM"));
        assertEquals(null, PimppUtils.parsePMI(null, null));
    }
}