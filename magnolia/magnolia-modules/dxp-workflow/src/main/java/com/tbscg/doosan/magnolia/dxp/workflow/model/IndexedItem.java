package com.tbscg.doosan.magnolia.dxp.workflow.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class IndexedItem {
    final String uuid;
    final String path;
    final long lastModified;
}
