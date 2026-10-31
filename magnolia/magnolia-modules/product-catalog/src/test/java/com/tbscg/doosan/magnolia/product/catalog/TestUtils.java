package com.tbscg.doosan.magnolia.product.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@Slf4j
public final class TestUtils {

    private TestUtils() {
        //
    }

    public static String loadResourceFromPath(String resource) throws URISyntaxException, IOException {
        URL url = TestUtils.class.getClassLoader().getResource(resource);
        log.debug("Loading resource [{}] -> {}", resource, url);
        return Files.lines(Paths.get(url.toURI()), StandardCharsets.UTF_8).collect(Collectors.joining());
    }

    public static <T> T loadFromJsonFile(String resource, Class<T> asClass) throws URISyntaxException, IOException {
        return new ObjectMapper().readValue(loadResourceFromPath(resource), asClass);
    }

    public static <T> T loadFromXmlFile(String resource, Class<T> asClass) throws URISyntaxException, IOException {
        return new XmlMapper().readValue(loadResourceFromPath(resource), asClass);
    }
}
