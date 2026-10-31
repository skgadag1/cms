package com.tbscg.doosan.magnolia.dxp.workflow.model;

import org.junit.Test;

import static org.junit.Assert.assertThrows;

public class PathMappingTest {

    @Test
    public void setLang2MappingMap_shouldNotAllowNullLang2MappingMap() {
        assertThrows(NullPointerException.class, () -> PathMapping.builder().build());
    }

}
