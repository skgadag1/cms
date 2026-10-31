package com.tbscg.doosan.magnolia.kags.renderers;

import info.magnolia.freemarker.FreemarkerHelper;
import info.magnolia.jcr.util.ContentMap;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.engine.RenderingEngine;
import info.magnolia.rendering.renderer.FreemarkerRenderer;
import info.magnolia.rendering.template.RenderableDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

public class FreemarkerExtendedRenderer extends FreemarkerRenderer {
    private final Logger log;
    private final HashSet<String> specialProperties;

    @Inject
    FreemarkerExtendedRenderer(FreemarkerHelper fmRenderer, RenderingEngine renderingEngine) {
        super(fmRenderer, renderingEngine);
        this.log = LoggerFactory.getLogger(this.getClass());
        this.specialProperties = new HashSet<>(Arrays.asList("id", "path", "depth", "name", "nodeType"));
    }

    protected void encodeNodeInPlace(Node node) throws RepositoryException {
        for (PropertyIterator it = node.getProperties(); it.hasNext(); ) {
            try {
                final Property property = it.nextProperty();
                if (property.isMultiple() || property.getType() != PropertyType.STRING) continue;

                final String key = property.getName();
                if (this.specialProperties.contains(key) || key.contains(":")) continue;

                String value = property.getValue().getString();
                value = value.replace("\\", "\\\\")
                        .replace("\"", "\\&quot;");
                property.setValue(value);
            } catch (Exception e) {
                this.log.error(e.getMessage(), e);
            }
        }

        for (NodeIterator children = node.getNodes(); children.hasNext(); ) {
            try {
                final Node child = children.nextNode();
                if (!child.getPrimaryNodeType().getName().equals("mgnl:contentNode")) continue;

                this.encodeNodeInPlace(child);
            } catch (Exception e) {
                this.log.error(e.getMessage(), e);
            }
        }
    }

    protected void encodeContentInPlace(ContentMap contentMap) throws RepositoryException {
        if (contentMap == null) return;

        final Node node = contentMap.getJCRNode();
        if (node == null) return;

        this.encodeNodeInPlace(node);
    }

    @Override
    protected void onRender(Node content, RenderableDefinition definition, RenderingContext renderingCtx, Map<String, Object> ctx, String templateScript) throws RenderException {
        final ContentMap contentMap = (ContentMap) ctx.get("content");
        try {
            this.encodeContentInPlace(contentMap);
        } catch (RepositoryException e) {
            this.log.error("Unable to complete encoding", e);
        }

        super.onRender(content, definition, renderingCtx, ctx, templateScript);
    }
}
