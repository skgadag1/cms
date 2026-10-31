package com.tbscg.doosan.magnolia.mediatracker.data.provider.elasticsearch;

/*
 * Copyright 2012-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

import org.apache.http.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.http.ContentStreamProvider;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.http.SdkHttpRequest;
import software.amazon.awssdk.http.auth.aws.signer.AwsV4HttpSigner;
import software.amazon.awssdk.http.auth.spi.signer.SignedRequest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.apache.http.protocol.ExecutionContext.HTTP_TARGET_HOST;

/**
 * An {@link HttpRequestInterceptor} that signs requests using any AWS {@link AwsV4HttpSigner}
 * and {@link AwsCredentialsProvider}.
 */
public class AWSRequestSigningApacheInterceptor implements HttpRequestInterceptor {
    /**
     * The service that we're connecting to.
     */
    private final String service;

    /**
     * The region for the endpoint.
     */
    private final String region;

    /**
     * The particular signer implementation.
     */
    private final AwsV4HttpSigner signer;

    /**
     * The source of AWS credentials for signing.
     */
    private final AwsCredentialsProvider awsCredentialsProvider;

    /**
     *
     * @param service service that we're connecting to
     * @param region region of the endpoint
     * @param signer particular signer implementation
     * @param awsCredentialsProvider source of AWS credentials for signing
     */
    public AWSRequestSigningApacheInterceptor(final String service,
                                              final String region,
                                              final AwsV4HttpSigner signer,
                                              final AwsCredentialsProvider awsCredentialsProvider) {
        this.service = service;
        this.region = region;
        this.signer = signer;
        this.awsCredentialsProvider = awsCredentialsProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(final HttpRequest request, final HttpContext context)
            throws HttpException, IOException {
        URIBuilder uriBuilder;
        try {
            uriBuilder = new URIBuilder(request.getRequestLine().getUri());
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URI" , e);
        }

        HttpHost host = (HttpHost) context.getAttribute(HTTP_TARGET_HOST);
        if (host != null) {
            uriBuilder.setHost(host.getHostName());
            uriBuilder.setPort(host.getPort());
            uriBuilder.setScheme(host.getSchemeName());
        }

        SdkHttpRequest signableRequest;
        try {
            signableRequest = SdkHttpRequest.builder()
                    .uri(uriBuilder.build())
                    .method(SdkHttpMethod.fromValue(request.getRequestLine().getMethod()))
                    .rawQueryParameters(nvpToMapParams(uriBuilder.getQueryParams()))
                    .headers(headerArrayToMap(request.getAllHeaders()))
                    .build();
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URI" , e);
        }

        final ContentStreamProvider payload;
        if (request instanceof HttpEntityEnclosingRequest httpEntityEnclosingRequest) {
            if (httpEntityEnclosingRequest.getEntity() == null) {
                payload = ContentStreamProvider.fromByteArray(new byte[0]);
            } else {
                payload = ContentStreamProvider.fromInputStream(httpEntityEnclosingRequest.getEntity().getContent());
            }
        } else {
            payload = null;
        }


        SignedRequest signedRequest = signer.sign(r -> r.identity(awsCredentialsProvider.resolveCredentials())
                            .request(signableRequest)
                            .payload(payload)
                .putProperty(AwsV4HttpSigner.SERVICE_SIGNING_NAME, service)
                .putProperty(AwsV4HttpSigner.REGION_NAME, region)
        );

        // Now copy everything back
        request.setHeaders(mapToHeaderArray(signedRequest.request().headers()));
        if (request instanceof HttpEntityEnclosingRequest) {
            HttpEntityEnclosingRequest httpEntityEnclosingRequest =
                    (HttpEntityEnclosingRequest) request;
            if (httpEntityEnclosingRequest.getEntity() != null) {
                BasicHttpEntity basicHttpEntity = new BasicHttpEntity();
                signedRequest.payload().ifPresent(p -> basicHttpEntity.setContent(p.newStream()));
                httpEntityEnclosingRequest.setEntity(basicHttpEntity);
            }
        }
    }

    /**
     *
     * @param params list of HTTP query params as NameValuePairs
     * @return a multimap of HTTP query params
     */
    private static Map<String, List<String>> nvpToMapParams(final List<NameValuePair> params) {
        Map<String, List<String>> parameterMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (NameValuePair nvp : params) {
            List<String> argsList =
                    parameterMap.computeIfAbsent(nvp.getName(), k -> new ArrayList<>());
            argsList.add(nvp.getValue());
        }
        return parameterMap;
    }

    /**
     * @param headers modeled Header objects
     * @return a multimap of header entries
     */
    private static Map<String, List<String>> headerArrayToMap(final Header[] headers) {
        Map<String, List<String>> headersMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Header header : headers) {
            if (!skipHeader(header)) {
                List<String> argsList =
                        headersMap.computeIfAbsent(header.getName(), k -> new ArrayList<>());
                argsList.add(header.getValue());
            }
        }
        return headersMap;
    }

    /**
     * @param header header line to check
     * @return true if the given header should be excluded when signing
     */
    private static boolean skipHeader(final Header header) {
        return ("content-length".equalsIgnoreCase(header.getName())
                && "0".equals(header.getValue())) // Strip Content-Length: 0
                || "host".equalsIgnoreCase(header.getName()); // Host comes from endpoint
    }

    /**
     * @param mapHeaders Map of header entries
     * @return modeled Header objects
     */
    private static Header[] mapToHeaderArray(final Map<String, List<String>> mapHeaders) {
        Header[] headers = new Header[mapHeaders.size()];
        int i = 0;
        for (Map.Entry<String, List<String>> headerEntry : mapHeaders.entrySet()) {
            headers[i++] = new BasicHeader(headerEntry.getKey(), headerEntry.getValue().get(0));
        }
        return headers;
    }
}