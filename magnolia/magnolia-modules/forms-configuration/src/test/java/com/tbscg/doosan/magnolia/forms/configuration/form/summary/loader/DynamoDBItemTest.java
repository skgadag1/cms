package com.tbscg.doosan.magnolia.forms.configuration.form.summary.loader;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.TestInstance;
import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("DynamoDBItem class tests")
public class DynamoDBItemTest {

    @Test
    public void testParseContent() {
        String content = "{\"mappingId\":1,\"name\":\"Test1\"};{\"mappingId\":2,\"name\":\"Test2\"};{\"mappingId\":3,\"name\":\"Test3\"}";
        List<ContentItem> contentItems = DynamoDBItem.parseContent(content);
        assertNotNull(contentItems);
        assertEquals(3, contentItems.size());
        assertEquals("Test1", contentItems.get(0).getName());
        assertEquals("Test2", contentItems.get(1).getName());
        assertEquals("Test3", contentItems.get(2).getName());
    }

    @Test
    public void testParseDateString() {
        Date date = DynamoDBItem.parseDateString("2023-03-10T15:32:00.000Z");
        assertNotNull(date);
        assertEquals(2023, date.getYear() + 1900);
        assertEquals(2, date.getMonth());
        assertEquals(10, date.getDate());
        assertEquals(15, date.getHours());
        assertEquals(32, date.getMinutes());
    }

    @Test
    public void testWithDefault() {
        EnhancedDocument item = EnhancedDocument.fromJson("{\"attr1\":\"value1\"}");
        String value1 = DynamoDBItem.getStringWithDefault(item, "attr1");
        String value2 = DynamoDBItem.getStringWithDefault(item, "attr2");
        String value3 = DynamoDBItem.getStringWithDefault(item, "attr3");
        assertEquals("value1", value1);
        assertEquals("", value2);
        assertEquals("", value3);
    }

    @Test
    public void testMapJson() {
        String json = "{\"mappingId\":\"value1\",\"name\":\"name1\",\"value\":\"value1\"}";
        ContentItem contentItem = DynamoDBItem.mapJson(json);
        assertNotNull(contentItem);
        assertEquals("value1", contentItem.getMappingId());
        assertEquals("name1", contentItem.getName());
        assertEquals("value1", contentItem.getValue());

    }

}