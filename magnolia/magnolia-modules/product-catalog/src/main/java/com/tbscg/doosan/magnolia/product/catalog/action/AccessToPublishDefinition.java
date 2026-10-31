package com.tbscg.doosan.magnolia.product.catalog.action;

import com.google.common.collect.ImmutableMap;
import com.tbscg.doosan.magnolia.regions.model.Region;
import com.tbscg.doosan.magnolia.regions.service.RegionNotFoundException;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import info.magnolia.context.MgnlContext;
import info.magnolia.ui.api.availability.AbstractAvailabilityRule;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.jcr.Node;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
public class AccessToPublishDefinition extends AbstractAvailabilityRule {
    private static final List<String> DEFAULT_ROLES = Arrays.asList("superuser", "dxp-bobcat-admins");
    private static final ImmutableMap<String, List<String>> REGIONS_MAPPINGS = ImmutableMap.<String, List<String>>builder()
            .put("na", Arrays.asList("dxp-bobcat-admin-na", "dxp-bobcat-publishers-na"))
            .put("eu", Arrays.asList("dxp-bobcat-admin-emea", "dxp-bobcat-publishers-emea"))
            .put("cis", Arrays.asList("dxp-bobcat-admin-emea", "dxp-bobcat-publishers-emea"))
            .put("mea", Arrays.asList("dxp-bobcat-admin-emea", "dxp-bobcat-publishers-emea"))
            .put("kr", Arrays.asList("dxp-bobcat-admin-alao", "dxp-bobcat-publishers-alao"))
            .put("la", Arrays.asList("dxp-bobcat-admin-alao", "dxp-bobcat-publishers-alao"))
            .put("sea", Arrays.asList("dxp-bobcat-admin-alao", "dxp-bobcat-publishers-alao"))
            .put("in", Arrays.asList("dxp-bobcat-admin-alao", "dxp-bobcat-publishers-alao"))
            .put("cn", Arrays.asList("dxp-bobcat-admin-alao", "dxp-bobcat-publishers-alao"))
            .put("oc", Arrays.asList("dxp-bobcat-admin-alao", "dxp-bobcat-publishers-alao"))
            .put("br", Arrays.asList("dxp-bobcat-admin-alao", "dxp-bobcat-publishers-alao"))
            .put("ap", Arrays.asList("dxp-bobcat-admin-alao", "dxp-bobcat-publishers-alao"))
            .build();
    private final RegionsService regionService;

    @Inject
    public AccessToPublishDefinition(RegionsService regionService) {
        this.regionService = regionService;
    }

    @Override
    protected boolean isAvailableForItem(Object node) {
        try {
            String path = ((Node) node).getPath();
            List<String> roles = Stream.concat(DEFAULT_ROLES.stream(), getAdditionalRoles(path).stream()).collect(Collectors.toList());

            return MgnlContext.getUser().getAllRoles().stream()
                    .anyMatch(roles::contains);
        } catch (Exception e) {
            log.error("Failed to validate", e);
            return false;
        }
    }

    private List<String> getAdditionalRoles(String path) {
        try {
            Region region = regionService.getRegionFromPath(path);
            List<String> roles = REGIONS_MAPPINGS.get(region.getName());

            if (roles == null) {
                return Collections.emptyList();
            }

            if (path.endsWith(region.getName())) {
                return Collections.singletonList(roles.get(0));
            }

            return roles;
        } catch (RegionNotFoundException e) {
            return Collections.emptyList();
        }
    }
}
