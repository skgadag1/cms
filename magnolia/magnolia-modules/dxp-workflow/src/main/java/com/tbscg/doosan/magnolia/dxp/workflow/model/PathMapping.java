package com.tbscg.doosan.magnolia.dxp.workflow.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.Map;

@Data
@Builder(setterPrefix = "with")
public class PathMapping {

    @NonNull
    private String domain;

    @NonNull
    private Map<String, PathMappingEntry> lang2MappingMap;



}
