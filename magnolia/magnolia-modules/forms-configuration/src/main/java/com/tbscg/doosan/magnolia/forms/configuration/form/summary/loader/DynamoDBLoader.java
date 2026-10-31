package com.tbscg.doosan.magnolia.forms.configuration.form.summary.loader;

import com.tbscg.doosan.magnolia.forms.configuration.form.summary.external.resources.DynamoDBConnector;
import com.tbscg.doosan.magnolia.forms.configuration.form.summary.external.resources.SignedUrlGenerator;
import com.tbscg.doosan.magnolia.forms.configuration.form.summary.external.resources.TableConnector;
import com.vaadin.ui.DateField;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.tbscg.doosan.magnolia.forms.configuration.form.summary.loader.DynamoDBItem.*;

@Slf4j
public class DynamoDBLoader implements DataLoader<DynamoDBItem> {
    private static final String BASE_PREFIX = "forms#bobcat";
    private final TableConnector<EnhancedDocument> tableConnector;
    private final SignedUrlGenerator signedUrlGenerator;

    @Inject
    public DynamoDBLoader(DynamoDBConnector tableConnector, SignedUrlGenerator signedUrlGenerator) {
        this.tableConnector = tableConnector;
        this.signedUrlGenerator = signedUrlGenerator;
    }

    @Override
    public List<DynamoDBItem> getData(FormRequest request) {
        return getPartitionKeys(request)
                .stream()
                .flatMap(tableConnector::getRowsByKey)
                .map(this::parseItem)
                .map(this::fillSignedUrls)
                .collect(Collectors.toList());
    }

    private DynamoDBItem fillSignedUrls(DynamoDBItem item) {
        List<String> urls = item.getAttachments().stream().map(signedUrlGenerator::getSignerUrl).collect(Collectors.toList());
        item.setAttachmentsUrls(urls);
        return item;
    }

    private DynamoDBItem parseItem(EnhancedDocument item) {
        return builder()
                .withKey(getStringWithDefault(item, "pk"))
                .withAttachments(Arrays.stream(getStringWithDefault(item, "attachments").split(",")).map(String::trim).filter(StringUtils::isNotBlank).collect(Collectors.toList()))
                .withDate(parseDateString(getStringWithDefault(item, "sendTime")))
                .withContentItems(parseContent(getStringWithDefault(item, "content")))
                .build();
    }

    private List<String> getPartitionKeys(FormRequest request) {
        String prefix = getPartitionKeyPrefix(request);
        List<String> dates = getDaysInRange(request.getFrom(), request.getTo());
        return buildPartitionKeys(prefix, dates);
    }

    private List<String> buildPartitionKeys(String prefix, List<String> dates) {
        return dates.stream().map(date -> prefix + date).collect(Collectors.toList());

    }

    private String getPartitionKeyPrefix(FormRequest request) {
        return String.format("%s#%s#%s#", BASE_PREFIX, request.getRegion(), request.getFormName());
    }

    private List<String> getDaysInRange(DateField from, DateField to) {
        LocalDate start = from.getValue();
        LocalDate end = to.getValue();
        long numOfDaysBetween = ChronoUnit.DAYS.between(start, end) + 1;
        return Stream.iterate(start, date -> date.plusDays(1))
                .limit(numOfDaysBetween)
                .map(this::parseDateToString)
                .collect(Collectors.toList());
    }

    private String parseDateToString(LocalDate localDate) {
        int year = localDate.getYear();
        int month = localDate.getMonthValue();
        int day = localDate.getDayOfMonth();
        return String.format("%d/%d/%d", month, day, year);
    }
}
