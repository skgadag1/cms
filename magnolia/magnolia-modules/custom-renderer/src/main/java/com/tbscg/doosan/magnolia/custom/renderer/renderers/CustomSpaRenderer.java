package com.tbscg.doosan.magnolia.custom.renderer.renderers;

import com.tbscg.doosan.magnolia.custom.renderer.CustomRendererConfig;
import com.tbscg.doosan.magnolia.custom.renderer.definitions.CustomSpaRenderableDefinition;
import info.magnolia.context.WebContext;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.module.resources.ResourceLinker;
import info.magnolia.module.site.SiteManager;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.engine.RenderingEngine;
import info.magnolia.rendering.spa.extended.renderer.SpaRenderer;
import info.magnolia.rendering.template.RenderableDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Map;

@Slf4j
public class CustomSpaRenderer extends SpaRenderer {
    private CustomRendererConfig config;

    @Inject
    public CustomSpaRenderer(RenderingEngine renderingEngine, TemplateDefinitionRegistry registry,
                             ResourceLinker linker, Provider<WebContext> webContextProvider,
                             CustomRendererConfig config, SiteManager siteManager) {
        super(renderingEngine, registry, linker, webContextProvider, siteManager);
        this.config = config;
    }

    @Override
    protected void onRender(Node node, RenderableDefinition definition, RenderingContext renderingContext, Map<String, Object> context, String templateScript) throws RenderException {
        String pageType = ((CustomSpaRenderableDefinition) definition).getPageType();
        String template = this.config.getTemplateScript().get(pageType);
        if (StringUtils.isNotEmpty(template)) {
            try {
                PropertyUtil.setProperty(node, "templateScript", template);
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
            super.onRender(node, definition, renderingContext, context, template);
        } else {
            log.error("Missing templateScript property in custom-renderer module configuration!");
        }
    }
}
