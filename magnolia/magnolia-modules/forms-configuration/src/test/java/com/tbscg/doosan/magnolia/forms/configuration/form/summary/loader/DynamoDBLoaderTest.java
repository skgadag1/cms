package com.tbscg.doosan.magnolia.forms.configuration.form.summary.loader;

import com.tbscg.doosan.magnolia.forms.configuration.form.summary.external.resources.DynamoDBConnector;
import com.tbscg.doosan.magnolia.forms.configuration.form.summary.external.resources.SignedUrlGenerator;
import com.vaadin.ui.DateField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverterProvider;
import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("DynamoDBLoader class tests")
class DynamoDBLoaderTest {

    @Mock
    private DynamoDBConnector tableConnector;

    @Mock
    private SignedUrlGenerator signedUrlGenerator;

    private DynamoDBLoader dynamoDBLoader;

    private FormRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dynamoDBLoader = new DynamoDBLoader(tableConnector, signedUrlGenerator);
    }

    @BeforeEach
    void initFormRequest() {
        DateField from = new DateField();
        from.setValue(LocalDate.of(2022, 3, 1));
        DateField to = new DateField();
        to.setValue(LocalDate.of(2022, 3, 3));
        request = FormRequest.builder()
                .withFrom(from)
                .withTo(to)
                .withRegion("region")
                .withFormName("form-name")
                .build();
    }

    @Test
    void testGetData() {
        // given
        List<String> partitionKeys = Arrays.asList("forms#bobcat#region#form-name#3/1/2022",
                "forms#bobcat#region#form-name#3/2/2022",
                "forms#bobcat#region#form-name#3/3/2022");
        when(tableConnector.getRowsByKey(partitionKeys.get(0))).thenReturn(Stream.of(getItem()));
        when(tableConnector.getRowsByKey(partitionKeys.get(1))).thenReturn(Stream.of(getItem()));
        when(tableConnector.getRowsByKey(partitionKeys.get(2))).thenReturn(Stream.of(getItem()));
        when(signedUrlGenerator.getSignerUrl(anyString())).thenReturn("https://signedurl.example.com");

        // when
        List<DynamoDBItem> data = dynamoDBLoader.getData(request);

        // then
        assertEquals(3, data.size());
        DynamoDBItem item = data.get(0);
        assertEquals("partition-key", item.getKey());
        assertEquals(Arrays.asList("attachment1", "attachment2"), item.getAttachments());
        assertEquals(Arrays.asList("https://signedurl.example.com", "https://signedurl.example.com"), item.getAttachmentsUrls());
        assertEquals(Collections.singletonList(ContentItem.builder().withName("field1").withValue("value1").build()), item.getContentItems());
        verify(tableConnector, times(3)).getRowsByKey(anyString());
        verify(signedUrlGenerator, times(6)).getSignerUrl(anyString());
    }

    private FormRequest createFormRequest() {
        DateField from = new DateField();
        from.setValue(LocalDate.of(2022, 3, 1));
        DateField to = new DateField();
        to.setValue(LocalDate.of(2022, 3, 3));
        return FormRequest.builder().withFrom(from).withTo(to).withRegion("region").withFormName("form-name").build();

    }

    private EnhancedDocument getItem() {
        return EnhancedDocument.builder()
                .attributeConverterProviders(AttributeConverterProvider.defaultProvider())
                .putString("pk", "partition-key")
                .putString("attachments", "attachment1, attachment2")
                .putString("sendTime", "2023-03-01T12:00:00Z")
                .putString("content", "{\"name\":\"field1\", \"value\":\"value1\"}")
                .build();
    }
}
