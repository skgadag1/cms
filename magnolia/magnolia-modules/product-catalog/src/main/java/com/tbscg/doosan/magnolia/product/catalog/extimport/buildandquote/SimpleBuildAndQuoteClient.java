package com.tbscg.doosan.magnolia.product.catalog.extimport.buildandquote;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.tbscg.doosan.magnolia.product.catalog.ProductCatalogModule;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Provider;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

@Slf4j
public class SimpleBuildAndQuoteClient implements BuildAndQuoteClient {

    private static final String GET_CATEGORIES_ENDPOINT = "/categories";

    private final ObjectMapper objectMapper;
    private final CollectionType collectionType;

    private final URL getCategoriesURL;

    public SimpleBuildAndQuoteClient(final Provider<ProductCatalogModule> productCatalogModuleProvider) {
        final String host = productCatalogModuleProvider.get().getBuildAndQuoteConfig().getHost();
        getCategoriesURL = makeGetCategoriesURL(host);

        objectMapper = createObjectMapper();
        collectionType = createCollectionType();
    }

    private static URL makeGetCategoriesURL(final String buildAndQuoteHost) {
        try {
            return new URL(buildAndQuoteHost + GET_CATEGORIES_ENDPOINT);
        } catch (final MalformedURLException e) {
            throw new BuildAndQuoteClientException("Invalid B&Q url", e);
        }
    }

    private CollectionType createCollectionType() {
        return objectMapper.getTypeFactory().constructCollectionType(List.class, BuildAndQuoteCategory.class);
    }

    private static ObjectMapper createObjectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return objectMapper;
    }

    @Override
    public List<BuildAndQuoteCategory> getCategoryList() {
        try {
            log.debug("Getting category list from {}", getCategoriesURL);
            return objectMapper.readValue(getCategoriesURL, collectionType);
        } catch (final IOException e) {
            throw new BuildAndQuoteClientException("Failed to get category list from B&Q", e);
        }
    }

}
