package com.tbscg.doosan.magnolia.bobcat.renderers;

import info.magnolia.context.WebContext;
import info.magnolia.module.resources.ResourceLinker;
import info.magnolia.rendering.engine.RenderingEngine;
import info.magnolia.rendering.spa.renderer.SpaRenderer;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import org.jsoup.nodes.Document;

import javax.inject.Inject;
import javax.inject.Provider;

public class BobcatSpaRenderer extends SpaRenderer {

    @Inject
    public BobcatSpaRenderer(RenderingEngine renderingEngine, TemplateDefinitionRegistry registry,
                             ResourceLinker linker, Provider<WebContext> webContextProvider) {
        super(renderingEngine, registry, linker, webContextProvider);
    }

    @Override
    protected void setBaseHref(Document htmlDocument, String path) {
    }
}
