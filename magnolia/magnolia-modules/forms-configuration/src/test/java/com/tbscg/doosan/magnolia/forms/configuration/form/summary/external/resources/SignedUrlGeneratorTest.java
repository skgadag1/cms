package com.tbscg.doosan.magnolia.forms.configuration.form.summary.external.resources;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.http.SdkHttpRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("SignedUrlGenerator class tests")
public class SignedUrlGeneratorTest {

    @Mock
    private S3Presigner s3Presigner;

    private SignedUrlGenerator signedUrlGenerator;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        signedUrlGenerator = new SignedUrlGenerator();
        signedUrlGenerator.setS3Presigner(s3Presigner);
        signedUrlGenerator.setBucketName("test");
    }

    @SneakyThrows
    @Test
    public void testGetSignerUrl() {
        // Given
        String key = "/test-key";
        ArgumentCaptor<GetObjectPresignRequest> getObjectPresignRequestCaptor =
                ArgumentCaptor.forClass(GetObjectPresignRequest.class);
        when(s3Presigner.presignGetObject(isA(GetObjectPresignRequest.class)))
                .thenReturn(PresignedGetObjectRequest.builder()
                        .httpRequest(SdkHttpRequest.builder()
                                .uri(new URI("https://example.com"))
                                .method(SdkHttpMethod.GET)
                                .build())
                        .expiration(Instant.now())
                        .isBrowserExecutable(true)
                        .signedHeaders(Collections.singletonMap("host", Collections.singletonList("example.com")))
                        .build());

        // When
        String url = signedUrlGenerator.getSignerUrl(key);
        verify(s3Presigner).presignGetObject(getObjectPresignRequestCaptor.capture());
        GetObjectPresignRequest capturedResource = getObjectPresignRequestCaptor.getValue();

        // Then
        assertEquals("https://example.com", url);
        assertEquals("test", capturedResource.getObjectRequest().bucket());
        assertEquals("test-key", capturedResource.getObjectRequest().key());
        assertEquals(Duration.ofDays(7), capturedResource.signatureDuration());
    }
}