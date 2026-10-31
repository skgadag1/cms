package com.tbscg.doosan.magnolia.dxp.core;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Wrapper for site page paths that include information like site name, page path, region and language.
 */
@Getter
public class SitePagePath {

    private final String site;
    private final String region;
    private final String path;

    public SitePagePath(final String path) {
        final String[] pathArray = normalizePath(path).split("/", 4);
        this.site = pathArray.length > 1 ? pathArray[1] : "";
        this.region = pathArray.length > 2 ? pathArray[2] : "";
        this.path = pathArray.length > 3 ? parsePath(pathArray[3]) : "";
    }

    /**
     * For given language, builds site page full path string composed of site name, region, language and path.
     *
     * @return site page path string composed of region, language and path
     */
    public String buildPathForLanguage(final String language) {
        if (region.isEmpty()) {
            return "";
        }
        return String.format(
                "/%s%s%s",
                this.region,
                isBlank(language) ? "" : "/" + language,
                this.path.isEmpty() ? "" : "/" + this.path);
    }

    @Override
    public String toString() {
        return String.format("/%s/%s/%s", site, region, path);
    }

    private String parsePath(final String path) {
        return Arrays.stream(path.split("/")).filter(StringUtils::isNotEmpty).collect(Collectors.joining("/"));
    }

    private String normalizePath(String path) {
        if (path == null) {
            return "";
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path.toLowerCase();
    }

}
