package com.tbscg.doosan.magnolia.mediatracker.data.provider.dynamodb;

import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityDomain;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityUrl;
import com.tbscg.doosan.magnolia.mediatracker.util.DateFormatUtil;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument;

import static com.tbscg.doosan.magnolia.mediatracker.data.provider.elasticsearch.ElasticsearchMapper.*;

/**
 * Class aggregating DynamoDB mapping functions.
 */
public class DynamoDbItemMapper {

    public static final String CODE = "code";
    private static final String FULL_DOMAIN_NAME = "fullDomainName";
    private static final String STATUS = "status";
    private static final String DIVISION = "division";
    private static final String CREATED_BY = "createdBy";
    private static final String CREATION_TIME = "creationTime";
    private static final String MODIFIED_BY = "modifiedBy";
    private static final String MODIFIED_TIME = "modifiedTime";
    private static final String VANITY_DOMAIN = "vanityDomain";
    private static final String START_DATE = "startDate";
    private static final String END_DATE = "endDate";
    private static final String DEVICE_DEPENDENT_REDIRECT = "deviceDependentRedirect";
    private static final String REDIRECT_TYPE = "redirectType";

    /**
     * Maps VanityDomain object to DynamoDB item
     *
     * @param d content app domain entity
     * @return DynamoDB item
     */
    public static EnhancedDocument fromVanityDomain(VanityDomain d) {
        EnhancedDocument.Builder itemBuilder = EnhancedDocument.builder()
                .putString(CODE, d.getCode())
                .putString(FULL_DOMAIN_NAME, d.getFullDomainName())
                .putString(STATUS, d.getStatus().name().toUpperCase())
                .putString(DIVISION, d.getDivision().toString().toUpperCase())
                .putString(CREATION_TIME, d.getCreationTime().format(DateFormatUtil.FORMATTER))
                .putString(CREATED_BY, d.getModifiedBy())
                .putString(MODIFIED_TIME, d.getModifiedTime().format(DateFormatUtil.FORMATTER))
                .putString(MODIFIED_BY, d.getModifiedBy());

        addStringIfNotEmpty(itemBuilder, START_DATE, d.getStartDate() != null ? d.getStartDate().format(DateFormatUtil.FORMATTER) : "");
        addStringIfNotEmpty(itemBuilder, END_DATE, d.getEndDate() != null ? d.getEndDate().format(DateFormatUtil.FORMATTER) : "");

        return itemBuilder.build();
    }

    /**
     * Creates DynamoDB representation of primary key object for domain table
     *
     * @param code value of code parameter
     * @return primary key item
     */
    public static Key toPrimaryKey(String code) {
        return Key.builder().partitionValue(code).build();
    }

    public static EnhancedDocument fromVanityUrl(VanityUrl url) {
        EnhancedDocument.Builder itemBuilder = EnhancedDocument.builder()
                .putString(CODE, url.getCode())
                .putString(VANITY_DOMAIN, url.getVanityDomain() != null ? url.getVanityDomain().getCode() : "")
                .putString(PATH, url.getPath())
                .putString(STATUS, url.getStatus().name().toUpperCase())
                .putString(DIVISION, url.getDivision().toString().toUpperCase())
                .putString(REGION, url.getRegion().toString())
                .putString(CREATION_TIME, url.getCreationTime().format(DateFormatUtil.FORMATTER))
                .putString(MODIFIED_TIME, url.getModifiedTime().format(DateFormatUtil.FORMATTER))
                .putString(CREATED_BY, url.getCreatedBy())

                .putNumber(DEVICE_DEPENDENT_REDIRECT, url.isDeviceDependentRedirect() ? 1 : 0)
                .putString(REDIRECT_TYPE, url.getRedirectType().toString());


        addStringIfNotEmpty(itemBuilder, PLACEMENT, url.getPlacement());
        addStringIfNotEmpty(itemBuilder, TRACKING_PARAMETER, url.getTrackingParameter());
        addStringIfNotEmpty(itemBuilder, MODIFIED_BY, StringUtils.isNotBlank(url.getModifiedBy()) ? url.getModifiedBy() : "");
        addStringIfNotEmpty(itemBuilder, REDIRECT_LOCATION, StringUtils.isNotBlank(url.getRedirectLocation()) ? url.getRedirectLocation() : "");
        addStringIfNotEmpty(itemBuilder, REDIRECT_LOCATION_DESKTOP, StringUtils.isNotBlank(url.getRedirectLocationDesktop()) ? url.getRedirectLocationDesktop() : "");
        addStringIfNotEmpty(itemBuilder, REDIRECT_LOCATION_MOBILE, StringUtils.isNotBlank(url.getRedirectLocationMobile()) ? url.getRedirectLocationMobile() : "");
        addStringIfNotEmpty(itemBuilder, REDIRECT_LOCATION_TABLET, StringUtils.isNotBlank(url.getRedirectLocationTablet()) ? url.getRedirectLocationTablet() : "");
        addStringIfNotEmpty(itemBuilder, REDIRECT_LOCATION_OTHER, StringUtils.isNotBlank(url.getRedirectLocationOther()) ? url.getRedirectLocationOther() : "");
        addStringIfNotEmpty(itemBuilder, START_DATE, url.getStartDate() != null ? url.getStartDate().format(DateFormatUtil.FORMATTER) : "");
        addStringIfNotEmpty(itemBuilder, END_DATE, url.getEndDate() != null ? url.getEndDate().format(DateFormatUtil.FORMATTER) : "");

        return itemBuilder.build();
    }

    private static void addStringIfNotEmpty(EnhancedDocument.Builder itemBuilder, String name, String val) {
        if (StringUtils.isNotBlank(val)) {
            itemBuilder.putString(name, val);
        }
    }

}
