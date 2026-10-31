package com.tbscg.doosan.magnolia.forms.configuration.field.datasource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tbscg.doosan.magnolia.bobcat.BobcatModule;
import com.tbscg.doosan.magnolia.forms.configuration.FormsConfigurationModule;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import info.magnolia.context.MgnlContext;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.datasource.optionlist.Option;
import info.magnolia.ui.datasource.optionlist.OptionListDefinition;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ContractCodesOptionListDefinition extends OptionListDefinition {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String CONTRACT_CODES_PATH = "/dealer-locator/contract-codes";
    private static final String DEFAULT_LANG = "en";

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private static final Map<String, List<Option>> cache = new LinkedHashMap<>();

    @Override
    public List<Option> getOptions() {
        String baseUrl = getContractCodesBaseUrl();
        if (StringUtils.isBlank(baseUrl)) {
            log.error("Contract codes API base URL is not configured");
            return List.of();
        }

        String region = StringUtils.defaultString(resolveRegionFromCurrentPath());

        if (cache.containsKey(region)) {
            return cache.get(region);
        }

        String url = buildUrl(baseUrl, StringUtils.isNotBlank(region) ? region : null);

        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET();

            String apiKey = getApiKey();
            if (StringUtils.isNotBlank(apiKey)) {
                requestBuilder.header("x-api-key", apiKey);
            }

            HttpResponse<String> response = HTTP_CLIENT.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Contract codes endpoint returned HTTP {}", response.statusCode());
                return List.of();
            }

            JsonNode array = MAPPER.readTree(response.body());

            Map<String, Option> seen = new LinkedHashMap<>();
            for (JsonNode item : array) {
                String id = item.path("id").asText();
                if (!seen.containsKey(id)) {
                    seen.put(id, option(id, item.path("fullName").asText()));
                }
            }

            List<Option> options = new ArrayList<>(seen.values());
            cache.put(region, options);
            return options;

        } catch (Exception e) {
            log.error("Failed to load options from contract codes endpoint", e);
            return List.of();
        }
    }

    private String buildUrl(String baseUrl, String region) {
        StringBuilder url = new StringBuilder(baseUrl).append(CONTRACT_CODES_PATH)
                .append("?lang=").append(DEFAULT_LANG)
                .append("&instance=public");
        if (StringUtils.isNotBlank(region)) {
            url.append("&region=").append(region);
        }
        return url.toString();
    }

    private String resolveRegionFromCurrentPath() {
        try {
            RegionsService regionsService = Components.getComponent(RegionsService.class);
            String path = getCurrentContentPath();
            if (StringUtils.isNotBlank(path)) {
                return regionsService.getRegionFromPath(path).getName();
            }
        } catch (Exception e) {
            log.warn("Could not resolve region from current path: {}", e.getMessage());
        }
        return null;
    }

    private String getCurrentContentPath() {
        try {
            if (MgnlContext.hasInstance() && MgnlContext.isWebContext()) {
                String path = MgnlContext.getWebContext().getRequest().getParameter("mgnlPath");
                if (StringUtils.isBlank(path)) {
                    path = MgnlContext.getWebContext().getRequest().getParameter("nodePath");
                }
                if (StringUtils.isBlank(path)) {
                    path = (String) MgnlContext.getWebContext().getRequest().getAttribute("mgnlCurrentContentPath");
                }
                return path;
            }
        } catch (Exception e) {
            log.debug("Could not get current content path from web context", e);
        }
        return null;
    }

    private String getContractCodesBaseUrl() {
        try {
            FormsConfigurationModule module = Components.getComponent(FormsConfigurationModule.class);
            return module.getContractCodesApiBaseUrl();
        } catch (Exception e) {
            log.error("Could not load FormsConfigurationModule", e);
            return null;
        }
    }

    private String getApiKey() {
        try {
            BobcatModule bobcatModule = Components.getComponent(BobcatModule.class);
            return bobcatModule.getBobcatApiKey();
        } catch (Exception e) {
            log.warn("Could not load bobcat API key", e);
            return null;
        }
    }

    private Option option(String value, String label) {
        Option opt = new Option();
        opt.setValue(value);
        opt.setLabel(label);
        opt.setName(value);
        return opt;
    }
}
