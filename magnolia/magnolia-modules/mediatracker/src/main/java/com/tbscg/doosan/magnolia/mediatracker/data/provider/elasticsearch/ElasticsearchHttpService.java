package com.tbscg.doosan.magnolia.mediatracker.data.provider.elasticsearch;

import com.tbscg.doosan.magnolia.mediatracker.MediatrackerModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.auth.aws.signer.AwsV4HttpSigner;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Class executing calls to Elasticsearch backend service.
 */
@Slf4j
public class ElasticsearchHttpService {

    private static final AwsCredentialsProvider credentialsProvider = DefaultCredentialsProvider.builder()
            .build();

    private MediatrackerModule mediatrackerModule;

    @Inject
    public ElasticsearchHttpService(MediatrackerModule mediatrackerModule) {
        this.mediatrackerModule = mediatrackerModule;
    }

    /**
     * Executes call to Elasticsearch backend service
     *
     * @param jsonPayload JSON query string
     * @return query result
     */
    public String getDocument(final String jsonPayload) {
        String responseString = "";
        String url = buildURL();
        log.debug("url is {}", url);
        try {
            HttpGetWithEntity get = new HttpGetWithEntity(url);
            get.setHeader("Content-type", "application/json");
            StringEntity stringEntity = new StringEntity(jsonPayload, StandardCharsets.UTF_8);
            get.setEntity(stringEntity);
            responseString = sendHTTPTransaction(get);
        } catch (Exception e) {
            log.error("HttpGet with entity failed: " + e.getLocalizedMessage());
        }
        return responseString;
    }

    private String buildURL() {
        MediatrackerModule.ElasticsearchConfig esConfig = mediatrackerModule.getElasticsearchConfig();
        String index = esConfig.getIndexName();
        String suffix = esConfig.getSearchSuffix();
        String url = esConfig.getEndpoint();
        if (index != null && index.length() > 0) {
            url = url + "/" + index;
        }
        if (suffix != null && suffix.length() > 0) {
            url = url + "/" + suffix;
        }
        return url;
    }

    private String sendHTTPTransaction(HttpUriRequest request) {
        String httpResult = null;
        CloseableHttpClient httpClient = signedClient();
        try {
            HttpResponse response = httpClient.execute(request);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                log.error("Failed to send HTTP transaction to {}. Response is: {}", request.getURI(), response.getStatusLine());
            }
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            httpResult = IOUtils.toString(bufferedReader);
        } catch (IOException e) {
            log.error("HTTP Result error: {}", e.getLocalizedMessage());
        }
        return httpResult;
    }

    private CloseableHttpClient signedClient() {
        AwsV4HttpSigner signer = AwsV4HttpSigner.create();
        MediatrackerModule.ElasticsearchConfig esConfig = mediatrackerModule.getElasticsearchConfig();
        HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor(esConfig.getServiceName(), esConfig.getRegion(), signer, credentialsProvider);
        return HttpClients.custom()
                .addInterceptorLast(interceptor)
                .build();
    }

}
