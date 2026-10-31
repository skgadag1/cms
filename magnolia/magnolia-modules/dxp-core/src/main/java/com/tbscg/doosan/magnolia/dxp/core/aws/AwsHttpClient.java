package com.tbscg.doosan.magnolia.dxp.core.aws;

import com.google.common.net.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.auth.signer.params.Aws4SignerParams;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.*;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class AwsHttpClient {

    private static final String SERVICE_NAME = "execute-api";
    private static final Region REGION = Region.US_EAST_1;
    private final URI hostUrl;
    private final SdkHttpClient httpClient;
    private final DefaultCredentialsProvider credentialsProvider;

    public AwsHttpClient(final String hostUrl) {
        this.hostUrl = URI.create(hostUrl);
        this.credentialsProvider = DefaultCredentialsProvider.create();
        this.httpClient = ApacheHttpClient.builder().build();
    }

    public String getResource(String resourcePath, NameValuePair... params) {
        try {
            log.debug("Getting resource from api {}", resourcePath);
            SdkHttpFullRequest request = getSignedRequest(resourcePath, params);
            return getResponse(request);
        } catch (Exception e) {
            log.error("Failed to get resource {}", hostUrl + resourcePath, e);
            return null;
        }
    }

    private SdkHttpFullRequest getSignedRequest(String resourceParams, NameValuePair... params) {
        Aws4Signer signer = Aws4Signer.create();
        SdkHttpFullRequest request = getRequest(resourceParams, params);
        Aws4SignerParams signerParams = Aws4SignerParams.builder()
                .awsCredentials(credentialsProvider.resolveCredentials())
                .signingName(SERVICE_NAME)
                .signingRegion(REGION)
                .build();
        return signer.sign(request, signerParams);
    }

    private SdkHttpFullRequest getRequest(String resourcePath, NameValuePair... params) {
        return SdkHttpFullRequest.builder()
                .encodedPath(resourcePath)
                .uri(hostUrl)
                .protocol("https")
                .rawQueryParameters(parseQueryParams(params))
                .method(SdkHttpMethod.GET)
                .putHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .build();
    }

    private Map<String, List<String>> parseQueryParams(NameValuePair... params) {
        return Arrays.stream(params)
                .collect(Collectors.groupingBy(NameValuePair::getName, Collectors.mapping(NameValuePair::getValue, Collectors.toList())));
    }

    String getResponse(SdkHttpFullRequest request) {
        log.debug("Path: {}", request.encodedPath());
        log.debug("Headers {}", request.headers().toString());
        HttpExecuteRequest httpExecuteRequest = HttpExecuteRequest.builder().request(request).build();
        try {
            HttpExecuteResponse httpResponse = httpClient.prepareRequest(httpExecuteRequest).call();
            if (httpResponse.responseBody().isPresent()) {
                InputStream inputStream = httpResponse.responseBody().get();
                return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            }
            log.error("Response body is missing for request: {}", request);
            return null;
        } catch (IOException e) {
            log.error("Failed to get response for request: {}", request, e);
            return null;
        }
    }
}
