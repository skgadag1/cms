package com.tbscg.doosan.magnolia.passport.data.provider.api;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tbscg.doosan.magnolia.dxp.core.aws.AwsHttpClient;
import com.tbscg.doosan.magnolia.passport.PassportModule;
import com.tbscg.doosan.magnolia.passport.data.model.*;
import com.tbscg.doosan.magnolia.passport.data.model.Locale;
import com.tbscg.doosan.magnolia.passport.data.model.request.DocumentFileResponse;
import com.tbscg.doosan.magnolia.passport.util.DocumentConstants;
import info.magnolia.connector.sso.util.JSONUtils;
import info.magnolia.context.MgnlContext;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class ApiService {

    private final ApiOptions options;
    private Cache<String, String> cache;
    private final TokenService tokenService;
    private static final List<Locale> allowedlocales = DocumentConstants.allowedLocales();
    private static final String EMPLOYEE_NUMBER = "employeeNumber";
    private static final String ACCESS_TOKEN = "access_token";
    private final AwsHttpClient client;
    private final long documentWorkspaceId;

    @Inject
    public ApiService(PassportModule passportModule, TokenService tokenService) {
        this.tokenService = tokenService;
        this.options = passportModule.getApiOptions();
        this.client = new AwsHttpClient(options.getBaseUrl());
        if (this.options.isCacheEnabled()) {
            this.cache = CacheBuilder.newBuilder()
                    .maximumSize(this.options.getCacheMaxSize())
                    .expireAfterWrite(this.options.getCacheTtl(), TimeUnit.SECONDS)
                    .build();
        }
        this.documentWorkspaceId = this.options.getDocumentWorkspaceId();
    }

    public PassportDocumentResponse getPassportDocuments(Object... params) {
        String result = getHttpService().get("/documents" + getQueryString(params));

        return ApiMapper.map(result, PassportDocumentResponse.class);
    }

    public PassportDocumentWithDetails getDocument(long id) {
        String result = getHttpService().get("/documents/" + id);
        return ApiMapper.map(result, PassportDocumentWithDetails.class);
    }

    public PassportDocumentCreateResponse addDocument(PassportDocumentData data) {
        String path = "/documents";
        data.setEmployeeNumber(getEmployeeNumber());
        String json = ApiMapper.toJson(data);
        String result = getHttpService().postJson(path, json);

        return ApiMapper.map(result, PassportDocumentCreateResponse.class);
    }

    public UpdateDocumentResponse updateDocument(long documentId, PassportDocumentData data) {
        String path = "/documents/" + documentId;
        data.setEmployeeNumber(getEmployeeNumber());
        String json = ApiMapper.toJson(data);
        String result = getHttpService().putJson(path, json);
        return ApiMapper.map(result, UpdateDocumentResponse.class);
    }

    public UpdateDocumentResponse updateDocument(PassportDocumentSavePayload data) {
        String path = "/documents/" + data.getDocumentId() + "?employee_number=" + getEmployeeNumber();
        data.setEmployeeNumber(getEmployeeNumber());
        String json = ApiMapper.toJson(data);
        String result = getHttpService().putJson(path, json);
        return ApiMapper.map(result, UpdateDocumentResponse.class);
    }

    public void deleteDocument(long documentId) {
        String path = "/documents/" + documentId + "?employee_number=" + getEmployeeNumber();
        getHttpService().delete(path);
    }

    public List<PropertySimple> getBrands() {
        String result = getCachedResult("/documents/brands");

        if (!result.isEmpty()) {
            return ApiMapper.map(result, BrandsResponse.class).getBrands()
                    .stream().sorted(Comparator.comparing(PropertySimple::getName))
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    public List<PropertySimple> getRegions() {
        String result = getCachedResult("/documents/regions");

        if (!result.isEmpty()) {
            return ApiMapper.map(result, RegionsResponse.class).getRegions()
                    .stream().sorted(Comparator.comparing(PropertySimple::getName))
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    public List<Locale> getLocales() {
        return allowedlocales;
    }

    private void invalidateCache(String pathPrefix) {
        this.cache.asMap()
                .keySet().stream()
                .filter(k -> k.startsWith(pathPrefix))
                .forEach(k -> {
                    log.info("Evicting API cache entry: {}", k);
                    this.cache.invalidate(k);
                });
    }

    private HttpService getHttpService() {
        HttpService http = new HttpService(this.options.getBaseUrl());
        http.setAccessTokenProvider(this.tokenService::getToken);
        return http;
    }

    @SneakyThrows
    private String getCachedResult(String path) {
        return this.getCachedResult(path, () -> getHttpService().get(path));
    }

    @SneakyThrows
    private String getCachedResult(String path, Callable<String> loader) {
        if (this.cache != null) {
            return this.cache.get(path, loader);
        } else {
            log.info("Result path: {}", path);
            return loader.call();
        }
    }

    @SneakyThrows
    private static String getQueryString(Object... params) {
        StringBuilder qs = new StringBuilder();
        for (int i = 0; i < params.length - 1; i += 2) {
            if (qs.length() == 0) {
                qs.append("?");
            } else {
                qs.append("&");
            }
            qs.append(params[i]);
            qs.append("=");
            qs.append(URLEncoder.encode(String.valueOf(params[i + 1]), StandardCharsets.UTF_8.name()));
        }
        return qs.toString();
    }

    public DocumentFileResponse addDocumentFile(long documentId, DocumentFileData data) {
        String path = "/documents/" + documentId + "/files/" + data.getLocale();
        data.setEmployeeNumber(getEmployeeNumber());
        String json = ApiMapper.toJson(data);
        String result = getHttpService().postJson(path, json);
        return ApiMapper.map(result, DocumentFileResponse.class);
    }

    public void deleteDocumentFile(long documentId, PassportFileTranslation file) {
        String path = "/documents/" + documentId + "/files/" + file.getLocale() + "?employee_number=" + getEmployeeNumber();
        getHttpService().delete(path);
    }

    public List<PassportFileTranslation> getDocumentFiles(long documentId) {
        String path = "/documents/" + documentId + "/files";
        String result = getHttpService().get(path);

        if (!result.isEmpty()) {
            List<PassportFileTranslation> l = ApiMapper.map(result, DocumentFilesResponse.class).getFiles()
                    .stream().sorted(Comparator.comparing(PassportFileTranslation::getStorageReference))
                    .collect(Collectors.toList());
            return l;
        } else {
            return Collections.emptyList();
        }
    }

    public PassportDocumentDefinitionResponse getDefinitions() {
        String path = "/documents/definitions";
        String result = getHttpService().get(path);

        if (!result.isEmpty()) {
            return ApiMapper.map(result, PassportDocumentDefinitionResponse.class);
        } else {
            return new PassportDocumentDefinitionResponse();
        }
    }

    public List<Application> getApplications() {
        String result = getCachedResult(
            "/passport/applications",
            () -> client.getResource("/passport/applications", this.buildGetApplicationParams())
        );
        if (StringUtils.isEmpty(result)) {
            return Collections.emptyList();
        }
        ApplicationResponse response = ApiMapper.map(result, ApplicationResponse.class);
        return response == null || response.getApplications() == null ? Collections.emptyList() : response.getApplications();
    }

    public List<PropertySimple> getBaseApplications() {
        return this.getApplications()
                .stream()
                .map(app -> new PropertySimple(app.getApplicationBaseId(), app.getApplicationBaseName()))
                .distinct()
                .collect(Collectors.toList());
    }

    private String getEmployeeNumber() {
        String ctxEmployeeNumber = getFieldFromMagnoliaContext(EMPLOYEE_NUMBER);
        // Remove hardcoded SN after context confirmed in DEV testing
        return ctxEmployeeNumber.isEmpty() ? "01064895" : ctxEmployeeNumber;
    }

    private String getFieldFromMagnoliaContext(String fieldName) {
        if (MgnlContext.hasAttribute(ACCESS_TOKEN)) {
            return JSONUtils.getFieldValue(getDecodedToken(), fieldName);
        }
        log.info("Field {} not found in Magnolia context.", fieldName);
        return "";
    }

    private static JsonObject getDecodedToken() {
        final String accessToken = MgnlContext.getAttribute(ACCESS_TOKEN);

        String[] split_string = accessToken.split("\\.");
        String base64EncodedBody = split_string[1];
        byte[] bytes = Base64.getUrlDecoder().decode(base64EncodedBody);
        String decodedString = new String(bytes, StandardCharsets.UTF_8);

        return JsonParser.parseString(decodedString).getAsJsonObject();
    }

    private NameValuePair[] buildGetApplicationParams() {
        return new NameValuePair[]{
                new BasicNameValuePair("active_only", Boolean.TRUE.toString()),
                new BasicNameValuePair("workspace_id", Long.toString(this.documentWorkspaceId))
        };
    }
}
