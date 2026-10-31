package com.tbscg.doosan.magnolia.bobcat.richtext;

import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.context.WebContext;
import info.magnolia.link.LinkException;
import info.magnolia.link.LinkTransformer;
import info.magnolia.link.LinkTransformerManager;
import info.magnolia.rest.reference.ReferenceResolver;
import info.magnolia.rest.reference.jcr.JcrReferenceResolver;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Optional;

@Slf4j
public class DxpRichTextLinkResolver implements ReferenceResolver<String, String> {
    private static final String langParam = "lang";
    private static final String fallbackLang = "en";

    private final DxpRichTextLinkResolverDefinition definition;
    private final DxpLinkUtil dxpLinkUtil;

    @Inject
    public DxpRichTextLinkResolver(DxpRichTextLinkResolverDefinition definition, DxpLinkUtil dxpLinkUtil) {
        this.definition = definition;
        this.dxpLinkUtil = dxpLinkUtil;
    }

    @Override
    public Optional<String> resolve(String richText) {
        // Try to use WebContext in order to be able to generate link.
        Context currentContext = MgnlContext.getInstance();
        try {
            if (currentContext instanceof SystemContext) {
                Context originalContext = ((SystemContext) currentContext).getOriginalContext();
                if (originalContext instanceof WebContext) {
                    MgnlContext.setInstance(originalContext);
                }
            }

            // Should return complete url for link in richtext,
            // so that client side don't need to parse the content to add the context path again
            String langFromContext = MgnlContext.getInstance().getAttribute(langParam);
            String lang = langFromContext != null ? langFromContext : fallbackLang;
            LinkTransformer transformer = definition.isCompleteUrl() ? LinkTransformerManager.getInstance().getCompleteUrl() : LinkTransformerManager.getInstance().getAbsolute(true);
            String links = dxpLinkUtil.convertCustomLinksFromUUIDPattern(richText, transformer, lang);

            return Optional.ofNullable(links);
        } catch (LinkException e) {
            log.warn("Error on resolving {}", richText);
        } finally {
            MgnlContext.setInstance(currentContext);
        }

        return Optional.ofNullable(richText);
    }
}
