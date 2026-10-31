package com.tbscg.doosan.magnolia.product.catalog.field.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tbscg.doosan.magnolia.product.catalog.field.product.ProductApiRequest;
import com.tbscg.doosan.magnolia.product.catalog.field.product.ProductSpecification;

import info.magnolia.context.MgnlContext;
import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.rest.client.ConfiguredRestClientDefinition;
import info.magnolia.rest.client.RestClient;
import info.magnolia.rest.client.factory.RestClientFactory;
import info.magnolia.rest.client.registry.RestClientRegistry;
import info.magnolia.ui.editor.LocaleContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ProductApiService {
    public static final String PRODUCT_API_CLIENT_NAME = "productApi";
    public static final String PRODUCT_API_SPECS_ENDPOINT_NAME = "specs";
    public static final String PRODUCT_API_BASE_URI_PROPERTY = "magnolia.bobcat.product.api.server";
    public static final String PIM_BUSINESS_UNIT_NA = "na";
    public static final String PIM_BUSINESS_UNIT_EMEA = "emea";
    public static final String PIM_BUSINESS_UNIT_ALAO = "alao";
    public static final String PIM_BUSINESS_UNIT_OTHER = "other";
    public static final String PIM_DEFAULT_LOCALE = "en-EN";
    public static final String UOM_NA = "us";
    public static final String UOM_METRIC = "metric";

    private final RestClientRegistry restClientRegistry;
    private final RestClientFactory restClientFactory;
    private final MagnoliaConfigurationProperties properties;
    private final ObjectMapper mapper;

    @Inject
    public ProductApiService(RestClientRegistry restClientRegistry,
                             RestClientFactory restClientFactory,
                             MagnoliaConfigurationProperties properties) {
        this.restClientRegistry = restClientRegistry;
        this.restClientFactory = restClientFactory;
        this.properties = properties;
        this.mapper = new ObjectMapper();
    }

    public JsonNode fetchProductSpecifications(ProductApiRequest request) {
        try {
            ConfiguredRestClientDefinition clientDefinition =
                    (ConfiguredRestClientDefinition) restClientRegistry.getProvider(PRODUCT_API_CLIENT_NAME).get();
            clientDefinition.setBaseUrl(properties.getProperty(PRODUCT_API_BASE_URI_PROPERTY));

            try (RestClient restClient = restClientFactory.createClientIfAbsent(clientDefinition)) {
                Map<String, Object> params = buildApiParameters(request);
                log.info("Making API call with params: {}", params);
                Response response = restClient.invoke(PRODUCT_API_SPECS_ENDPOINT_NAME, params);
                if (response == null || response.getStatus() != 200) {
                    String statusCode = response != null ? String.valueOf(response.getStatus()) : "null";
                    String responseBody = response != null ? response.getEntity().toString() : "null";
                    log.error("Failed to get model data. Status code: {}. Response: {}", statusCode, responseBody);
                    return null;
                }
                return (JsonNode) response.getEntity();
            }
        } catch (Exception e) {
            log.error("Failed to fetch specifications", e);
            return null;
        }
    }

    public String getRegionFromPath(String path) throws RepositoryException {
        String[] pathTokens = StringUtils.split(path, "/");
        if (pathTokens.length <= 2) {
            throw new RepositoryException("Invalid path: " + path);
        }
        return pathTokens[1];
    }

    public String getBusinessUnitByRegion(String region) {
        try {
            Node businessUnitNode = getBusinessUnitNode(region);
            String businessUnit = businessUnitNode.getProperty("businessUnit").getString();
            if (businessUnit.equals(PIM_BUSINESS_UNIT_NA) ||
                businessUnit.equals(PIM_BUSINESS_UNIT_EMEA) ||
                businessUnit.equals(PIM_BUSINESS_UNIT_ALAO)) {
                return businessUnit;
            }
        } catch (Exception e) {
            log.error("Failed to resolve business unit for region: {}", region, e);
        }
        return PIM_BUSINESS_UNIT_OTHER;
    }

    public String getLocale(String region, LocaleContext localeContext) {
        try {
            Node businessUnitNode = getBusinessUnitNode(region);
            String language = localeContext != null ?
                localeContext.getCurrent().getLanguage() :
                MgnlContext.getLocale().getLanguage();
            Node languageNode = businessUnitNode.getNode(language);
            return languageNode.getProperty("locale").getString();
        } catch (Exception e) {
            log.error("Failed to get locale for region: {}", region, e);
        }
        return PIM_DEFAULT_LOCALE;
    }

    public String getLocale(String region) {
        return getLocale(region, null);
    }

    public ProductSpecification mapProductSpecification(JsonNode json) {
            try {
                return mapper.treeToValue(json, ProductSpecification.class);
            } catch (JsonProcessingException | IllegalArgumentException e) {
            log.error("Failed to map product specification", e);
        }
        return null;
    }

    private Map<String, Object> buildApiParameters(ProductApiRequest request) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", request.getProductId());
        params.put("businessUnit", request.getBusinessUnit());
        params.put("locale", request.getLocale());
        params.put("uom", PIM_BUSINESS_UNIT_NA.equals(request.getBusinessUnit()) ? UOM_NA : UOM_METRIC);
        params.put("specLevel", request.getSpecLevel());
        params.put("modelType", request.getModelType());
        params.put("region", request.getRegion());
        params.put("productDetailsDataSource", request.getProductDetailsDataSource());
        params.put("instance", request.getInstance());
        params.put("lang", "en");
        return params;
    }

    private Node getBusinessUnitNode(String region) throws RepositoryException {
        Session session = MgnlContext.getJCRSession("regions");
        Node regionRootNode = session.getNode("/bobcat");
        return regionRootNode.getNode(region);
    }
} 