package com.tbscg.doosan.magnolia.bobcat.rest.delivery;

import com.machinezoo.noexception.Exceptions;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.context.WebContext;
import info.magnolia.rest.reference.jcr.JcrReferenceResolver;
import info.magnolia.rest.reference.jcr.JcrReferenceResolverDefinition;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Slf4j
public class FeatureParentTitleResolver extends JcrReferenceResolver  {
    private static final String PRIMARY_PROPERTY = "title";
    private static final String PRODUCT_GROUP_TYPE = "dxp:product";
    private static final String MAGNOLIA_FOLDER_TYPE = "mgnl:folder";
    private static final String EMPTY = "";
    private static final String LANG_PARAM = "lang";
    private static final String FALLBACK_LANG = "en";

    @Inject
    public FeatureParentTitleResolver(Provider<Context> contextProvider, JcrReferenceResolverDefinition definition) {
        super(contextProvider, definition);
    }

    @Override
    public Optional<Object> resolve(String itemID) {
        final Optional<Object> resolve = super.resolve(itemID);
        if(resolve.isPresent()) {
            Node item = (Node) resolve.get();
            Optional<String> title = null;
            try {
                String language = getLanguageFromContext();
                log.debug("Resolving feature category title with language: {}", language);
                title = getParentTitle(item, language);
                if (title.isPresent()) {
                    log.debug("Setting featureCateogryTitle to: {}", title.get());
                    item.setProperty("featureCateogryTitle", title.get());
                }
            } catch (RepositoryException e) {
                log.warn("Error on resolving item {}", itemID);
            }

        }
        return resolve;
    }

    private String getLanguageFromContext() {
        // Get the current context - might be a SystemContext
        Context currentContext = MgnlContext.getInstance();
        Context contextToUse = currentContext;
        
        // If we're in a SystemContext, get the original WebContext
        if (currentContext instanceof SystemContext) {
            Context originalContext = ((SystemContext) currentContext).getOriginalContext();
            log.debug("Running in SystemContext, switching to original context: {}", originalContext);
            if (originalContext instanceof WebContext) {
                contextToUse = originalContext;
            }
        }
        
        // First try context attribute (set by REST API framework)
        String langFromContext = contextToUse.getAttribute(LANG_PARAM);
        if (langFromContext != null) {
            log.debug("Got language from context attribute: {}", langFromContext);
            return langFromContext;
        }
        
        // Try to get from HTTP request parameter directly
        if (contextToUse instanceof WebContext) {
            WebContext webContext = (WebContext) contextToUse;
            HttpServletRequest request = webContext.getRequest();
            if (request != null) {
                String langParam = request.getParameter(LANG_PARAM);
                if (langParam != null && !langParam.isEmpty()) {
                    log.debug("Got language from HTTP request parameter: {}", langParam);
                    return langParam;
                }
            }
        }
        
        log.debug("No language found, using fallback: {}", FALLBACK_LANG);
        return FALLBACK_LANG;
    }

    private Optional<String> getParentTitle(Node node, String language) throws RepositoryException {
        return Optional.ofNullable(getCategoryLikeParent(node)).map(Exceptions.wrap().function(n -> getTitle(n, language)));
    }

    private String getTitle(Node node, String language) throws RepositoryException {
        // Try localized property first (e.g., title_es)
        if (!FALLBACK_LANG.equals(language)) {
            String localizedProperty = PRIMARY_PROPERTY + "_" + language;
            if (node.hasProperty(localizedProperty)) {
                String localizedValue = node.getProperty(localizedProperty).getValue().getString();
                if (!localizedValue.equals(EMPTY)) {
                    log.debug("Feature Category Localized Title ({}) {}", language, localizedValue);
                    return localizedValue;
                }
            }
        }

        // Fall back to default title
        if (node.hasProperty(PRIMARY_PROPERTY) && !node.getProperty(PRIMARY_PROPERTY).getValue().getString().equals(EMPTY)) {
            log.debug("Feature Category Title {}", node.getProperty(PRIMARY_PROPERTY).getValue().getString());
            return node.getProperty(PRIMARY_PROPERTY).getValue().getString();
        }

        return node.getName();
    }

    private Node getCategoryLikeParent(Node node) throws RepositoryException {
        Node parent = node.getParent();
        if (parent.getPrimaryNodeType().getName().equals(MAGNOLIA_FOLDER_TYPE)) {
            return null;
        }

        if (parent.getPrimaryNodeType().getName().equals(PRODUCT_GROUP_TYPE)) {
            return getCategoryLikeParent(parent);
        }
        return parent;
    }

}
