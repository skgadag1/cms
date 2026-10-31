package com.tbscg.doosan.magnolia.dxp.workflow.model;

import lombok.Getter;

import java.util.Map;

@Getter
public class NodeDetails {
    final String uuid;
    final String path;
    final String name;
    final String id;
    final Map<String, String> urls;

    public NodeDetails(String uuid, String path, String name, String id) {
        this(uuid, path, name, id, null);
    }

    public NodeDetails(String uuid, String path, String name, String id, Map<String, String> urls) {
        this.uuid = uuid;
        this.path = path;
        this.name = name;
        this.id = id;
        this.urls = urls;
    }
}
