package com.tbscg.doosan.magnolia.product.catalog.extimport.gpim;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Default implementation for {@link GpimClient}. Uses Fluent HttpComponents & Jackson for data retrieval / parse.
 */
@Slf4j
public class DefaultGpimClient implements GpimClient {

    private final ObjectMapper objectMapper;
    private final GpimConfig gpimConfig;
    private final static int TIMEOUT = 200_000;

    @Inject
    public DefaultGpimClient(final GpimConfig gpimConfig) {
        this.gpimConfig = gpimConfig;
        objectMapper = new ObjectMapper();
    }

    public GpimConfig getGpimConfig() {
        return this.gpimConfig.copy();
    }

    @Override
    public GpimCategory getCategory(final String categoryName, String locale) throws GpimClientException {
        try {
            return doGetCategory(categoryName, locale);
        } catch (final JsonParseException | JsonMappingException e) {
            throw new GpimClientException("Failed to process response for category '" + categoryName + "'", e);
        } catch (final IOException e) {
            throw new GpimClientException("Failed to get category '" + categoryName + "'", e);
        }
    }

    private GpimCategory doGetCategory(final String categoryName, String locale) throws IOException {
        log.debug("START: getting category from GPIM: '{}'", categoryName);
        final String secretKey = gpimConfig.getSecretKey();
        final Request request = Request.Get(gpimConfig.getEndpoint() +
                "/rest/enhancedCategory/catalog/Products/version/Online/category/" +
                URLEncoder.encode(categoryName, StandardCharsets.UTF_8).replace("+", "%20") +
                "/details/P1/levels/0?&locale=" +
                locale +
                "&uomSystem=us")
                .addHeader("x-api-key", secretKey)
                .connectTimeout(TIMEOUT)
                .socketTimeout(TIMEOUT);

        log.debug("Executing request = {}", request);

        final Response response = request.execute();
        log.debug("DONE: got response for category: '{}'", categoryName);

        return objectMapper.readValue(response.returnContent().asStream(), GpimCategory.class);
    }

    @Override
    public GpimProduct getProduct(final String categoryId) throws GpimClientException {
        try {
            return doGetProduct(categoryId);
        } catch (final JsonParseException | JsonMappingException e) {
            throw new GpimClientException("Failed to process response for categoryId '" + categoryId + "'", e);
        } catch (final IOException e) {
            throw new GpimClientException("Failed to get categoryId '" + categoryId + "'", e);
        }
    }

    private GpimProduct doGetProduct(final String categoryId) throws IOException {
        log.debug("START: getting product from GPIM: '{}'", categoryId);
        final String secretKey = gpimConfig.getSecretKey();
        final Request request = Request.Get(gpimConfig.getEndpoint() +
                "/rest/enhancedProduct/catalog/CompactNA/version/Online/product/" +
                URLEncoder.encode(categoryId, StandardCharsets.UTF_8).replace("+", "%20") +
                "?&locale=en_US&uomSystem=us")
                .addHeader("x-api-key", secretKey);

        log.debug("Executing request = {}", request);

        final Response response = request.execute();
        log.debug("DONE: got response for categoryId: '{}'", categoryId);

        return objectMapper.readValue(response.returnContent().asStream(), GpimProduct.class);
    }

}
