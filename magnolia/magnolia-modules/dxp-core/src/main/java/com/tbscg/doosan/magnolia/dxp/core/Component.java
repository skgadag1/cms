package com.tbscg.doosan.magnolia.dxp.core;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder(setterPrefix = "with")
@Data
public class Component {
    private final String id;
    private final String path;
    private final List<Visibility> visibility;
    private final List<Content> content;
}
