package com.tbscg.doosan.magnolia.forms.configuration.form.summary.parsers;

import com.tbscg.doosan.magnolia.forms.configuration.form.summary.downloader.CSVSections;
import com.tbscg.doosan.magnolia.forms.configuration.form.summary.loader.ContentItem;
import com.tbscg.doosan.magnolia.forms.configuration.form.summary.loader.DynamoDBItem;
import com.tbscg.doosan.magnolia.forms.configuration.form.summary.loader.FormRequest;

import java.util.*;
import java.util.stream.Collectors;

public class DynamoDBItemsToCsvSectionParser implements Parser<DynamoDBItem, CSVSections> {
    @Override
    public CSVSections parse(List<DynamoDBItem> items, FormRequest formRequest) {
        return CSVSections.builder().withHeaders(generateHeaders(items.get(0))).withRows(generateRows(items, formRequest)).build();
    }

    private List<List<String>> generateRows(List<DynamoDBItem> items, FormRequest formRequest) {
        return items.stream().map(item -> generateRow(item, formRequest)).collect(Collectors.toList());
    }

    private List<String> generateRow(DynamoDBItem dynamoDBItem, FormRequest formRequest) {
        List<String> row = new ArrayList<>(Arrays.asList(formRequest.getRegion(), formRequest.getFormName(), dynamoDBItem.getDate().toString()));
        dynamoDBItem.getContentItems().stream()
                .map(ContentItem::getValue)
                .forEach(row::add);
        row.addAll(dynamoDBItem.getAttachmentsUrls());

        return row;
    }

    private List<String> generateHeaders(DynamoDBItem dynamoDBItem) {
        List<String> headers = new ArrayList<>(Arrays.asList("region", "formName", "date"));
        dynamoDBItem.getContentItems().stream()
                .map(item -> Optional.ofNullable(item.getMappingId()).orElse(item.getName()))
                .forEach(headers::add);
        headers.add("attachments");
        return headers;
    }
}
