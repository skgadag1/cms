package com.tbscg.doosan.magnolia.dxp.core.setup.placeholder;

import info.magnolia.context.MgnlContext;
import info.magnolia.importexport.BootstrapUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.value.ValueHelper;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFactory;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static info.magnolia.importexport.command.JcrExportCommand.Format.YAML;
import static java.util.Collections.emptyList;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.lang3.StringUtils.upperCase;

/**
 *  This logic find all placeholders $ in resource files, e.g.: ${jcr.cloudinary.api.secret}
 *
 *  Currently only support YAML files.
 *
 *  Please note that implementation for traversing properties in YAML file
 *  is mostly copied from info.magnolia.importexport.command.JcrImportCommand
 */
class PlaceholderVisitor {

    private static Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([\\w|\\.]+)\\}");

    List<Placeholder> getAllPlaceholders(String resourceName) {
        if (!StringUtils.equals(YAML.name(), upperCase(getExtension(resourceName)))) {
            return emptyList();
        }

        String fullPath = BootstrapUtil.getFullpathFromResource(resourceName);
        String parentPath = StringUtils.substringBeforeLast(fullPath, "/");

        String workspace = BootstrapUtil.getWorkspaceNameFromResource(resourceName);
        try {
            Session session = MgnlContext.getInstance().getJCRSession(workspace);
            final ValueFactory valueFactory = session.getValueFactory();
            final Yaml yaml = new Yaml(new JcrPropertySupportingConstructor(valueFactory));
            try (InputStream stream = BootstrapUtil.class.getResourceAsStream(resourceName)) {
                if (stream != null) {
                    final InputStreamReader inputStream = new InputStreamReader(stream, StandardCharsets.UTF_8);
                    Map<?, ?> content = (Map) yaml.load(inputStream);
                    List<Placeholder> placeholderList = doFind(content, parentPath);
                    return placeholderList;
                }
                return new ArrayList<>();
            }
        } catch (Exception e) {
            throw new PlaceholderRetrievingException(e);
        }
    }

    private List<Placeholder> doFind(Map<?, ?> content, String nodePath) throws RepositoryException {
        List<Placeholder> placeholderList = new ArrayList<>();

        for (Map.Entry entry : content.entrySet()) {
            String key = String.valueOf(entry.getKey());
            if (entry.getValue() instanceof Map) {
                placeholderList.addAll(doFind((Map) entry.getValue(), nodePath + "/" + key));
            } else if (!Arrays.asList(JcrConstants.JCR_PRIMARYTYPE, JcrConstants.JCR_UUID, JcrConstants.JCR_MIXINTYPES).contains(key)) {
                Matcher matcher = PLACEHOLDER_PATTERN.matcher(String.valueOf(entry.getValue()));
                if (entry.getValue() != null && matcher.matches()) {
                    String placeholder = matcher.group(1);
                    placeholderList.add(new Placeholder(nodePath, key, placeholder));
                }
            }
        }

        return placeholderList;
    }

    private static class JcrPropertySupportingConstructor extends Constructor {

        private JcrPropertySupportingConstructor(ValueFactory valueFactory) {
            yamlConstructors.put(new Tag("!" + StringUtils.lowerCase(PropertyType.TYPENAME_PATH)), new JcrConstruct(PropertyType.PATH, valueFactory));
            yamlConstructors.put(new Tag("!" + StringUtils.lowerCase(PropertyType.TYPENAME_URI)), new JcrConstruct(PropertyType.URI, valueFactory));
            yamlConstructors.put(new Tag("!" + StringUtils.lowerCase(PropertyType.TYPENAME_REFERENCE)), new JcrConstruct(PropertyType.REFERENCE, valueFactory));
            yamlConstructors.put(new Tag("!" + StringUtils.lowerCase(PropertyType.TYPENAME_WEAKREFERENCE)), new JcrConstruct(PropertyType.WEAKREFERENCE, valueFactory));
            yamlConstructors.put(new Tag("!" + StringUtils.lowerCase(PropertyType.TYPENAME_BINARY)), new JcrConstruct(PropertyType.BINARY, valueFactory));
        }
    }

    private static class JcrConstruct extends AbstractConstruct {
        private final int propertyType;
        private final ValueFactory valueFactory;

        private JcrConstruct(int propertyType, ValueFactory valueFactory) {
            this.propertyType = propertyType;
            this.valueFactory = valueFactory;
        }

        @Override
        public Object construct(org.yaml.snakeyaml.nodes.Node node) {
            try {
                return ValueHelper.deserialize(((ScalarNode) node).getValue(), propertyType, false, valueFactory);
            } catch (RepositoryException e) {
                return null;
            }
        }
    }
}
