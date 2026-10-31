package com.tbscg.doosan.magnolia.forms.configuration.form.summary.external.resources;

import com.tbscg.doosan.magnolia.forms.configuration.FormsConfigurationModule;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;

@Singleton
@Slf4j
public class SignedUrlGenerator {
    @Setter
    @Getter
    private String bucketName;

    @Setter
    @Getter
    private S3Presigner s3Presigner;

    @Inject
    private FormsConfigurationModule formsConfigurationModule;

    @PostConstruct
    private void init() {
        s3Presigner = formsConfigurationModule.getS3Presigner();
        bucketName = formsConfigurationModule.getDownloadFormSummaryConfig().getBucketName();
    }

    public String getSignerUrl(String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(normalizeKey(key))
                .build();


        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofDays(7))
                .getObjectRequest(request)
                .build();


        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    private String normalizeKey(String key) {
        return key.startsWith("/") ? key.substring(1) : key;
    }
}
