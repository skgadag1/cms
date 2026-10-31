package com.tbscg.doosan.magnolia.forms.configuration.form.summary.loader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Data
@Builder(setterPrefix = "with")
public class DynamoDBItem {
    private static final String DELIMITER = ";";
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private List<String> attachmentsUrls;
    private List<String> attachments;
    private String key;
    private List<ContentItem> contentItems;
    private Date date;

    public static String getStringWithDefault(EnhancedDocument item, String attr) {
        return Optional.ofNullable(item.getString(attr))
                .orElse("");
    }

    public static List<ContentItem> parseContent(String content) {
        return Arrays.stream(content.split(DELIMITER)).map(DynamoDBItem::mapJson).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static ContentItem mapJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, ContentItem.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse {}", json);
            return null;
        }
    }


    public static Date parseDateString(String dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
            return dateFormat.parse(dateString);
        } catch (Exception e) {
            log.error("Failed to parse date");
            return new Date();
        }
    }
}