package com.tbscg.doosan.magnolia.custom.renderer.definitions;

import info.magnolia.rendering.spa.renderer.ConfiguredSpaRenderableDefinition;

public class CustomSpaRenderableDefinition extends ConfiguredSpaRenderableDefinition {
    private String pageType;

    public String getPageType() {
        return this.pageType;
    }

    public void setPageType(String pageType) {
        this.pageType = pageType;
    }

    @Override
    public String toString() {
        return "ConfiguredSpaRenderableDefinition(jsPaths=" + this.getJsPaths() + ", cssPaths=" + this.getCssPaths() + ", baseUrl=" + this.getBaseUrl() + ", routeTemplate=" + this.getRouteTemplate() + ", spaRouterClass=" + this.getSpaRouterClass() + ", pageType=" + this.getPageType() + ")";
    }
}
