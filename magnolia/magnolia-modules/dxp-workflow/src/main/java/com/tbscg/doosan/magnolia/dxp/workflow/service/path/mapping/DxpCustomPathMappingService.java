package com.tbscg.doosan.magnolia.dxp.workflow.service.path.mapping;

import com.google.common.collect.ImmutableMap;
import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import com.tbscg.doosan.magnolia.dxp.core.util.PropertyUtil;
import com.tbscg.doosan.magnolia.dxp.workflow.model.PathMapping;
import com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.PathMappingServiceConfiguration;
import com.tbscg.doosan.magnolia.dxp.workflow.service.exception.CustomPathMappingException;
import com.tbscg.doosan.magnolia.regions.model.Region;
import com.tbscg.doosan.magnolia.regions.service.RegionNotFoundException;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.predicate.NodeTypePredicate;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.SessionUtil;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant.*;
import static com.tbscg.doosan.magnolia.dxp.workflow.util.WorkflowUtil.filterOutLockedNodes;
import static java.util.function.Predicate.not;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Slf4j
public class DxpCustomPathMappingService {

    private final DxpCustomPathResolver dxpCustomPathResolver;
    private final PathMappingServiceConfiguration pathMappingServiceConfiguration;
    private final RegionsService regionsService;

    @Inject
    public DxpCustomPathMappingService(final RegionsService regionsService,
                                       final DxpCustomPathResolver dxpCustomPathResolver,
                                       final PathMappingServiceConfiguration pathMappingServiceConfiguration) {
        this.regionsService = Objects.requireNonNull(regionsService, "regionsService");
        this.dxpCustomPathResolver = Objects.requireNonNull(dxpCustomPathResolver, "dxpCustomPathResolver");
        this.pathMappingServiceConfiguration = Objects.requireNonNull(pathMappingServiceConfiguration, "pathMappingServiceConfiguration");
    }

    public void updatePageWithCustomName(final Node node, final String path, final String nodeType, final String workspace) {
        try {
            final Region regionFromPath = this.getRegionForPath(path);
            final Map<String, String> customNameByLang = this.getPageCustomNameMap(path, workspace, nodeType, regionFromPath);
            log.debug("updatePageWithCustomName({}, {}, {})", path, nodeType, customNameByLang);
            if (!customNameByLang.isEmpty()) {
                log.debug("Adding custom name to the page {}", path);
                final Node pathConfigurationNode = NodeUtil.getOrAddNode(node, CUSTOM_NAME_FOR_LANG, NodeTypes.ContentNode.NAME);
                NodeUtil.setOrUpdateProperty(pathConfigurationNode, REGION_PROPERTY, regionFromPath.getName());
                for (final Map.Entry<String, String> entry : customNameByLang.entrySet()) {
                    final Node langNode = NodeUtil.getOrAddNode(pathConfigurationNode, entry.getKey(), NodeTypes.ContentNode.NAME);
                    NodeUtil.setOrUpdateProperty(langNode, CUSTOM_NAME_PROPERTY, entry.getValue());
                }
                node.getSession().save();
            }
        } catch (Exception e) {
            log.error("Error while adding custom name to the page", e);
        }
    }

    public void updateMappings(final String path, final String workspace, final boolean recursive) throws CustomPathMappingException {
        if (isMappableWorkspace(workspace) && isMappablePath(path)) {
            log.debug("updateMappings({}, {})", path, recursive);
            resolveMappingsForPath(path, workspace, recursive);
        }
    }

    private PathMapping resolveMappingsForPath(final String path, final String workspace, final boolean recursive) throws CustomPathMappingException {
        final Node node = SessionUtil.getNode(workspace, path);
        if (node == null) {
            throw new CustomPathMappingException("Failed to map node to " + PathMapping.class.getName());
        }
        updateCustomName(node, workspace);
        final PathMapping pathMapping = dxpCustomPathResolver.resolveCustomPaths(node);
        if (recursive) {
            this.resolveMappingsForChildren(path, node);
        }
        return pathMapping;
    }

    private void resolveMappingsForChildren(final String path, final Node node) throws CustomPathMappingException {
        try {
            final Iterable<Node> children = info.magnolia.jcr.util.NodeUtil.collectAllChildren(node, new NodeTypePredicate(NodeTypes.Page.NAME));
            final List<Node> unlockedChildren = filterOutLockedNodes(info.magnolia.jcr.util.NodeUtil.asList(children));

            unlockedChildren.stream().filter(not(NodeUtil::isDeleted)).forEach(unlockedChild -> {
                updateCustomName(unlockedChild);
                dxpCustomPathResolver.resolveCustomPaths(unlockedChild);
            });
        } catch (RepositoryException e) {
            throw new CustomPathMappingException("Failed to resolve mappings for node's children " + path);
        }
    }

    private Map<String, String> getPageCustomNameMap(final String path,
                                                     final String workspace,
                                                     final String nodeType,
                                                     final Region region) throws CustomPathMappingException {
        if (isMappableWorkspace(workspace) && isMappablePath(path) && isMappableType(nodeType)) {
            return this.getPageCustomNameByLang(path, region);
        }
        return Collections.emptyMap();
    }

    private Map<String, String> getPageCustomNameByLang(final String path, final Region regionFromPath) throws CustomPathMappingException {
        try {
            final Node node = MgnlContext
                    .getJCRSession(WEBSITE_WORKSPACE)
                    .getNode(path);
            return buildLangToCustomNameMap(node, regionFromPath);
        } catch (final Exception e) {
            throw new CustomPathMappingException("Failed to retrieve node hierarchy.", e);
        }
    }

    private static Map<String, String> buildLangToCustomNameMap(final Node node, final Region region) throws RepositoryException {
        final Collection<Locale> regionLocales = region.getI18n().getLocales();
        final Optional<Locale> defaultLocale = region.getI18n().getRegionDefaultLocale(region.getName());
        final String fallbackLang = defaultLocale.map(Locale::getLanguage).orElse(DEFAULT_LANG);
        log.debug("buildLangToCustomNameMap({}, {})", node, region);
        final Map<String, String> langToCustomNameMap = PropertyUtil.asList(node.getProperties("customNodeName*")).stream()
                .collect(Collectors.toMap(
                        property -> tryGetLanguageOfProperty(property, fallbackLang),
                        property -> PropertyUtil.getStringValue(property).orElseThrow(() -> new CustomPathMappingException("Failed to get string value of property."))
                ));
        final String nodeName = node.getName();
        final Map<String, String> nonCustomLangToNameMap = regionLocales
                .stream()
                .map(Locale::getLanguage)
                .filter(key -> !langToCustomNameMap.containsKey(key))
                .collect(Collectors.toMap(lang -> lang, lang -> nodeName));
        log.debug("buildLangToCustomNameMap({}, {}) = {}, {}", node, region, langToCustomNameMap, nonCustomLangToNameMap);
        return ImmutableMap.<String, String>builder()
                .putAll(langToCustomNameMap)
                .putAll(nonCustomLangToNameMap)
                .build();
    }

    private static String tryGetLanguageOfProperty(final Item item, final String fallbackLang) {
        try {
            return getLangFromName(item.getName(), fallbackLang);
        } catch (final RepositoryException e) {
            throw new CustomPathMappingException("Failed to get language from item name.", e);
        }
    }

    private static String getLangFromName(final String name, final String fallbackLang) {
        final String[] split = name.split("_");
        return split.length > 1 ? split[split.length - 1] : fallbackLang;
    }

    private boolean isMappableWorkspace(final String workspace) {
        return WEBSITE_WORKSPACE.equals(workspace);
    }

    private boolean isMappableType(final String nodeType) {
        return NodeTypes.Page.NAME.equals(nodeType);
    }

    private boolean isMappablePath(final String path) throws CustomPathMappingException {
        try {
            return pathMappingServiceConfiguration.getMappableSites()
                    .stream()
                    .map(siteName -> "/" + siteName)
                    .anyMatch(path::startsWith);
        } catch (RepositoryException exception) {
            throw new CustomPathMappingException("Error while getting mappable sites", exception);
        }
    }

    private Region getRegionForPath(final String path) {
        try {
            return regionsService.getRegionFromPath(path);
        } catch (RegionNotFoundException e) {
            throw new CustomPathMappingException("Failed to retrieve region for path.", e);
        }
    }

    private void updateCustomName(final Node node, final String workspace) {
        final String nodeType = NodeUtil.getPrimaryNodeType(node);
        this.updatePageWithCustomName(node, NodeUtil.getPath(node), nodeType, workspace);
    }

    private void updateCustomName(final Node node) {
        final String workspace = NodeUtil.tryGetNodeWorkspace(node).orElse(EMPTY);
        this.updateCustomName(node, workspace);
    }
}
