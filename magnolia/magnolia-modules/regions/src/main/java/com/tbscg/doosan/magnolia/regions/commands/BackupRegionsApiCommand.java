package com.tbscg.doosan.magnolia.regions.commands;

import com.tbscg.doosan.magnolia.dxp.core.util.TracingUtil;
import com.tbscg.doosan.magnolia.regions.RegionsModule;
import info.magnolia.commands.impl.BaseRepositoryCommand;
import info.magnolia.context.Context;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.StorageClass;


import javax.inject.Provider;
import java.io.*;

@Getter
@Setter
public class BackupRegionsApiCommand extends BaseRepositoryCommand {
    private final S3Client s3Client;
    private String regionsApiBackupPath = "";
    private String apiUrl;
    private String bucketName;

    public BackupRegionsApiCommand(final Provider<RegionsModule> regionsModuleProvider) {
        bucketName = regionsModuleProvider.get().getBackupRegionsApiCommandConfig().getS3Bucket();
        apiUrl = regionsModuleProvider.get().getBackupRegionsApiCommandConfig().getApiUrl();
        s3Client = S3Client.create();
    }

    @Override
    public boolean execute(Context context) throws Exception {
        InputStream regionsApiResponse = getRegionsFromApi();
        try {
            uploadRegionsApiToS3(regionsApiResponse);
        } catch (Exception e) {
            log.error("Error while uploading regions API to S3");
            throw e;
        }
        return true;
    }

    private InputStream getRegionsFromApi() throws IOException {
        try {
            HttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(apiUrl);
            HttpResponse response = httpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                return response.getEntity().getContent();
            } else {
                log.error("API request failed with status code: " + statusCode);
                throw new RuntimeException("API request failed with status code: " + statusCode);
            }
        } catch (Exception e) {
            log.error("Error while getting regions from API");
            throw e;
        }
    }

    private void uploadRegionsApiToS3(InputStream inputStream) {
        final PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(regionsApiBackupPath)
                .contentType("application/json")
                .storageClass(StorageClass.STANDARD_IA)
                .build();
        final RequestBody body = RequestBody.fromContentProvider(
                () -> inputStream,
                "application/octet-stream"
        );
        TracingUtil.callWithTracing(traceID -> s3Client.putObject(request, body));
    }
}
