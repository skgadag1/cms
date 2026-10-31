package com.tbscg.doosan.magnolia.dxp.core.setup.placeholder;

import info.magnolia.context.MgnlContext;
import info.magnolia.importexport.BootstrapUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.jcr.util.SessionUtil;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.Session;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Allows dry-run and apply for updating placeholders into their replacements for given module and resource files. Apply mode will update JCR.
 */
@Singleton
public class PlaceholderFacade {
    private final PlaceholderVisitor visitor;
    private final PlaceholderResolver resolver;

    @Inject
    public PlaceholderFacade(@Named("environment") Provider<String> currentEnvironment) {
        visitor = new PlaceholderVisitor();
        resolver = new PlaceholderResolverFactory(currentEnvironment.get()).create();
    }

    public void dryCheck(String[] resourceNames, String moduleName) {
        for (String resource : resourceNames) {
            dryCheck(resource, moduleName);
        }
    }

    public void dryCheck(String resourceName, String moduleName) {
        List<Placeholder> allPlaceholders = visitor.getAllPlaceholders(resourceName);
        List<Placeholder> unresolvedPlaceholders = allPlaceholders.stream()
                .filter(placeholder -> !resolver.resolve(placeholder, moduleName).isPresent()).collect(toList());
        if (!unresolvedPlaceholders.isEmpty()) {
            throw new PlaceholderResolvingException(unresolvedPlaceholders);
        }
    }

    public void apply(String[] resourceNames, String moduleName) {
        for (String resource : resourceNames) {
            apply(resource, moduleName);
        }
    }

    public void apply(String resourceName, String moduleName) {
        String workspace = BootstrapUtil.getWorkspaceNameFromResource(resourceName);
        try {
            Session session = MgnlContext.getInstance().getJCRSession(workspace);

            List<Placeholder> allPlaceholders = visitor.getAllPlaceholders(resourceName);
            for (Placeholder placeholder : allPlaceholders) {
                String resolvedValue = resolver.resolve(placeholder, moduleName).get();
                Node node = SessionUtil.getNode(session, placeholder.nodePath);
                PropertyUtil.setProperty(node, placeholder.propertyName, resolvedValue);
            }
        } catch (Exception e) {
            throw new PlaceholderApplyingException(e);
        }
    }
}
