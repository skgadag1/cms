package com.tbscg.doosan.magnolia.dxp.core.setup.placeholder;

import com.tbscg.doosan.magnolia.dxp.core.setup.placeholder.PlaceholderResolver.PlaceholderResolverStrategy;
import info.magnolia.cms.util.ClasspathResourcesUtil;
import org.apache.jackrabbit.ocm.repository.NodeUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.tbscg.doosan.magnolia.dxp.core.util.EnvironmentConstants.LOCAL_ENVIRONMENT;

/**
 * Creates 2 strategies to resolve placeholder
 *
 * 1) Passing VM options, e.g.: -Djcr.cloudinary.api.secret=mySecret
 *
 * 2) Using resource file local.properties in mgnl-bootstrap\<module_name>\ directory, works only for "local" environment
 */
class PlaceholderResolverFactory {

    private final String currentEnvironment;

    PlaceholderResolverFactory(String currentEnvironment) {
        this.currentEnvironment = currentEnvironment;
    }

    PlaceholderResolver create() {
        List<PlaceholderResolverStrategy> strategies = new ArrayList();

        strategies.add(new EnvironmentVariableStrategy());

        if (currentEnvironment.equals(LOCAL_ENVIRONMENT)) {
            strategies.add(new LocalPropertiesStrategy());
        }

        return new PlaceholderResolver(strategies);
    }

    private static class EnvironmentVariableStrategy implements PlaceholderResolverStrategy {

        public Optional<String> resolve(Placeholder placeholder, String moduleName) {
            return Optional.ofNullable(System.getProperty(placeholder.placeholderName));
        }
    }

    private static class LocalPropertiesStrategy implements PlaceholderResolverStrategy {

        Map<String, Properties> cachedModuleProperties = new HashMap<>();

        @Override
        public Optional<String> resolve(Placeholder placeholder, String moduleName) {
            Properties properties = cachedModuleProperties.get(moduleName);
            if (properties == null) {
                properties = loadProperties(moduleName);
            }

            return Optional.ofNullable(properties.getProperty(placeholder.placeholderName));
        }

        private Properties loadProperties(String moduleName) {
            Properties properties;
            properties = new Properties();
            cachedModuleProperties.put(moduleName, properties);

            try {
                try (InputStream stream = ClasspathResourcesUtil.getStream("/mgnl-bootstrap/" + moduleName + NodeUtil.PATH_SEPARATOR + "local.properties")) {
                    if (stream != null) {
                        properties.load(stream);
                    }
                }
            } catch (IOException e) {
                //
            }
            return properties;
        }
    }
}
