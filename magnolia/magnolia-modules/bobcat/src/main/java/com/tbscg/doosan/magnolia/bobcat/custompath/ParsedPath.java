package com.tbscg.doosan.magnolia.bobcat.custompath;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Builder(setterPrefix = "with", toBuilder = true)
@Slf4j
public class ParsedPath {

    private static final Pattern parsedPathPattern;

    static {
        parsedPathPattern = Pattern.compile("/(\\S+?)/([^/]+)(/\\S*)?");
    }

    private String path;
    private String lang;
    private String region;

    public String toPathWithLang() {
        return "/" + getRegion() + "/" + getLang() + Optional.ofNullable(getPath()).orElse("");
    }

    public static ParsedPath toParsedPath(final String path, final String lang) {
        Matcher matcher = parsedPathPattern.matcher(path);
        if (matcher.matches()) {
            String region = matcher.group(2);
            String relativePath = matcher.group(3);
            return ParsedPath.builder()
                    .withRegion(region)
                    .withLang(lang)
                    .withPath(relativePath)
                    .build();
        }
        log.error("Path not eligible for custom path resolver: {}", path);
        throw new CustomPathServiceException("Path not eligible for custom path resolver");
    }

}
