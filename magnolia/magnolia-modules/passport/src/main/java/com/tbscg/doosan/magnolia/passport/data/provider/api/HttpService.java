package com.tbscg.doosan.magnolia.passport.data.provider.api;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@Slf4j
public class HttpService {

    private final String baseUrl;
    private Supplier<String> accessTokenProvider;
    private List<? extends NameValuePair> headers = Collections.emptyList();

    public HttpService(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public static NameValuePair getBasicAuthHeader(String user, String password) {
        Base64.Encoder base64 = Base64.getEncoder();
        String credentials = user + ":" + password;
        String encodedCreds = base64.encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return new BasicNameValuePair("Authorization", "Basic " + encodedCreds);
    }

    public void setHeaders(final List<? extends NameValuePair> headers) {
        this.headers = headers;
    }

    private void applyHeaders(HttpUriRequest method, NameValuePair... defaultHeaders) {
        for (NameValuePair pair : defaultHeaders) {
            if (this.headers.stream().noneMatch(h -> h.getName().equals(pair.getName()))) {
                method.setHeader(pair.getName(), pair.getValue());
            }
        }
        for (NameValuePair pair : this.headers) {
            method.setHeader(pair.getName(), pair.getValue());
        }
        if (this.accessTokenProvider != null) {
            String token = this.accessTokenProvider.get();
            method.setHeader("Authorization", "Bearer " + token);
        }
    }

    public String get(final String path) {
        return executeApi(() -> {
            String url = getUrl(path);
            log.info("HTTP GET " + url);
            HttpGet method = new HttpGet(url);
            applyHeaders(method);
            return sendHTTPTransaction(method);
        });
    }

    public String delete(String path) {
        return executeApi(() -> {
            String url = getUrl(path);
            log.info("HTTP DELETE " + url);
            HttpDelete method = new HttpDelete(url);
            applyHeaders(method);
            return sendHTTPTransaction(method);
        });
    }

    public String post(final String path) {
        return executeApi(() -> {
            String url = getUrl(path);
            log.info("HTTP POST " + url);
            HttpPost method = new HttpPost(url);
            applyHeaders(method);
            return sendHTTPTransaction(method);
        });
    }

    public String postJson(final String path, final String jsonBody) {
        return executeApi(() -> {
            log.info("HTTP POST WITH JSON BODY: " + path + "|" + jsonBody);
            HttpPost method = new HttpPost(getUrl(path));
            NameValuePair contentType = new BasicNameValuePair(
                    "Content-type",
                    "application/json");
            applyHeaders(method, contentType);
            method.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));
            return sendHTTPTransaction(method);
        });
    }

    public String putJson(final String path, final String jsonBody) {
        return executeApi(() -> {
            log.info("HTTP PUT WITH JSON BODY: " + path + "|" + jsonBody);
            HttpPut method = new HttpPut(getUrl(path));
            NameValuePair contentType = new BasicNameValuePair(
                    "Content-type",
                    "application/json");
            applyHeaders(method, contentType);
            method.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));
            return sendHTTPTransaction(method);
        });
    }

    public String putFile(final String path, final File file, final String contentType) {
        return executeApi(() -> {
            log.info("HTTP PUT WITH FILE: " + path);
            HttpPut method = new HttpPut(getUrl(path));
            applyHeaders(method);
            method.setEntity(new FileEntity(file));
            return sendHTTPTransaction(method);
        });
    }

    public String postHttpForm(final String path, final List<? extends NameValuePair> form) {
        return executeApi(() -> {
            HttpPost method = new HttpPost(getUrl(path));
            NameValuePair contentType = new BasicNameValuePair(
                    "Content-Type",
                    "application/x-www-form-urlencoded");
            applyHeaders(method, contentType);
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, StandardCharsets.UTF_8);
            log.info("HTTP POST WITH FORM BODY: " + path + "|" + entity.toString());
            method.setEntity(entity);
            return sendHTTPTransaction(method);
        });
    }

    public String put(final String path, final String body) {
        return executeApi(() -> {
            log.info("HTTP PUT WITH BODY: " + path + "|" + body);
            HttpPut method = new HttpPut(getUrl(path));
            applyHeaders(method);
            method.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
            return sendHTTPTransaction(method);
        });
    }

    private String executeApi(Supplier<String> apiCall) {
        try {
            return apiCall.get();
        } catch (ApiError apiError) {
            throw apiError;
        } catch (Exception e) {
            throw  new ApiError("API call failed", e);
        }
    }

    private String trim(String s) {
        return s.length() > 1000 ? s.substring(0, 1000) + "...<TRUNCATED>" : s;
    }

    private String sendHTTPTransaction(HttpUriRequest request) {
        try (CloseableHttpClient httpClient = getClient()) {
            HttpResponse response = httpClient.execute(request);
            Charset charset = StandardCharsets.UTF_8;

            String responseBody = "";
            if(response.getEntity() != null) {
                InputStreamReader streamReader = new InputStreamReader(response.getEntity().getContent(), charset);
                BufferedReader bufferedReader = new BufferedReader(streamReader);
                responseBody = IOUtils.toString(bufferedReader);
            }
            int statusCode = response.getStatusLine().getStatusCode();
            if (200 <= statusCode && statusCode < 300) {
                return responseBody;
            } else {
                JsonNode errorJson = ApiMapper.jsonNode(responseBody);
                if (errorJson != null) {
                    JsonNode errorMessage = errorJson.get("error_message");
                    if (errorMessage != null) {
                        throw new ApiError(errorMessage.asText());
                    }
                }
                throw new ApiError(statusCode + " response from API: " + request.getURI());
            }
        } catch (IOException e) {
            throw new ApiError(e);
        }
    }

    private String getUrl(String path) {
        return this.baseUrl + path;
    }

    private CloseableHttpClient getClient() {
        return HttpClients.custom().build();
    }

    public void setAccessTokenProvider(Supplier<String> accessTokenProvider) {
        this.accessTokenProvider = accessTokenProvider;
    }

    private static class ApiError extends RuntimeException {
        public ApiError(String message) {
            super(message);
        }
        public ApiError(Throwable cause) {
            super(cause);
        }
        public ApiError(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
