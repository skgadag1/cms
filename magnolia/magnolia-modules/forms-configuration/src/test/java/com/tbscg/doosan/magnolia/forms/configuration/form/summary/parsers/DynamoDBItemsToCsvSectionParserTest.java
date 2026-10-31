package com.tbscg.doosan.magnolia.forms.configuration.form.summary.parsers;

import com.tbscg.doosan.magnolia.forms.configuration.form.summary.downloader.CSVSections;
import com.tbscg.doosan.magnolia.forms.configuration.form.summary.loader.ContentItem;
import com.tbscg.doosan.magnolia.forms.configuration.form.summary.loader.DynamoDBItem;
import com.tbscg.doosan.magnolia.forms.configuration.form.summary.loader.FormRequest;
import com.vaadin.ui.DateField;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("DynamoDBItemsToCsvSectionParser class tests")
class DynamoDBItemsToCsvSectionParserTest {
    private static final LocalDate FROM = LocalDate.of(2000, 1, 1);
    private static final LocalDate TO = LocalDate.of(2001, 1, 1);
    private static final ContentItem CONTENT_ITEM = ContentItem.builder()
            .withMappingId("1")
            .withName("test")
            .withValue("test")
            .build();
    private static final DynamoDBItem BASIC_ITEM = DynamoDBItem.builder()
            .withKey("1")
            .withDate(Date.valueOf(TO))
            .withAttachments(Arrays.asList("a", "b"))
            .withAttachmentsUrls(Arrays.asList("https://a.com", "https://b.com"))
            .withContentItems(Arrays.asList(CONTENT_ITEM, CONTENT_ITEM))
            .build();
    private static final List<String> EXPECTED_ROW = Arrays.asList("na", "test", "2001-01-01", "test", "test", "https://a.com", "https://b.com");
    private static final FormRequest REQUEST = FormRequest.builder()
            .withFormName("test")
            .withRegion("na")
            .withFrom(new DateField(FROM))
            .build();
    private DynamoDBItemsToCsvSectionParser parser;

    @BeforeEach
    void setUp() {
        parser = new DynamoDBItemsToCsvSectionParser();
    }

    @Test
    void testParseMethodReturnsCorrectCSVSections() {
        List<DynamoDBItem> items = Arrays.asList(BASIC_ITEM, BASIC_ITEM, BASIC_ITEM);
        CSVSections actualResponse = parser.parse(items, REQUEST);

        assertAll("CSVSections has correct values",
                () -> MatcherAssert.assertThat(actualResponse.getHeaders(), hasItems("1", "1")),
                () -> assertEquals(6, actualResponse.getHeaders().size()),
                () -> assertEquals(3, actualResponse.getRows().size()),
                () -> actualResponse.getRows().forEach(row -> assertEquals(EXPECTED_ROW, row))
        );
    }
}