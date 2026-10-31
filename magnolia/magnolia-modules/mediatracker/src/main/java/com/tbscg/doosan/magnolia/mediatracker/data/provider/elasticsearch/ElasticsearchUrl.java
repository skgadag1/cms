package com.tbscg.doosan.magnolia.mediatracker.data.provider.elasticsearch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tbscg.doosan.magnolia.mediatracker.data.model.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ElasticsearchUrl {
    public static final String EMPTY_STRING = "";

    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss.SSSX");

    private String tableName;
    private String division;
    private String modifiedTime;
    private String code;
    private String creationTime;
    private String createdBy;
    private String modifiedBy;
    private String startDate;
    private String endDate;
    private String status;
    private String vanityDomain;
    private String region;
    private String path;
    private String placement;
    private String trackingParameter;
    private String deviceDependentRedirect;
    private String redirectDomain;
    private String redirectType;
    private String redirectLocation;
    private String redirectLocationMobile;
    private String redirectLocationDesktop;
    private String redirectLocationTablet;
    private String redirectLocationOther;

    /**
     * Maps Elasticsearch representation of url entity into POJO object
     *
     * @param esUrl ES representation of url entity
     * @return POJO object
     */
    public static VanityUrl toVanityUrl(ElasticsearchUrl esUrl) {
        VanityUrl vanityUrl = new VanityUrl();
        vanityUrl.setCode(esUrl.getCode());
        vanityUrl.setPath(esUrl.getPath());
        vanityUrl.setVanityDomain(getVanityDomain(esUrl.vanityDomain));
        vanityUrl.setPlacement(esUrl.getPlacement());
        vanityUrl.setTrackingParameter(esUrl.getTrackingParameter());
        vanityUrl.setDeviceDependentRedirect(convertToBoolean(esUrl.getDeviceDependentRedirect()));
        vanityUrl.setRedirectLocation(esUrl.getRedirectLocation());
        vanityUrl.setRedirectLocationMobile(esUrl.getRedirectLocationMobile());
        vanityUrl.setRedirectLocationDesktop(esUrl.getRedirectLocationDesktop());
        vanityUrl.setRedirectLocationTablet(esUrl.getRedirectLocationTablet());
        vanityUrl.setRedirectLocationOther(esUrl.getRedirectLocationOther());
        vanityUrl.setRedirectType(Strings.isNotBlank(esUrl.getRedirectType()) ? RedirectType.valueOf(esUrl.getRedirectType()) : null);
        vanityUrl.setDivision(Division.find(esUrl.getDivision()));
        vanityUrl.setModifiedTime(esUrl.getModifiedTime() != null ? OffsetDateTime.parse(esUrl.getModifiedTime(), formatter) : null);
        vanityUrl.setCreationTime(esUrl.getCreationTime() != null ? OffsetDateTime.parse(esUrl.getCreationTime(), formatter) : null);
        vanityUrl.setCreatedBy(esUrl.getCreatedBy());
        vanityUrl.setModifiedBy(esUrl.getModifiedBy());
        vanityUrl.setStartDate(esUrl.getStartDate() != null && StringUtils.isNotBlank(esUrl.getStartDate()) ? OffsetDateTime.parse(esUrl.getStartDate(), formatter) : null);
        vanityUrl.setEndDate(esUrl.getEndDate() != null && StringUtils.isNotBlank(esUrl.getEndDate()) ? OffsetDateTime.parse(esUrl.getEndDate(), formatter) : null);
        vanityUrl.setStatus(Strings.isNotBlank(esUrl.getStatus()) ? Status.findIgnoreCase(esUrl.getStatus().trim()) : null);
        if (!EMPTY_STRING.equals(esUrl.getRegion())) {
            vanityUrl.setRegion(Region.valueOf(esUrl.getRegion()));
        }

        return vanityUrl;
    }

    private static boolean convertToBoolean(String deviceDependentRedirect) {
        if (Strings.isNotBlank(deviceDependentRedirect)) {
            return Integer.parseInt(deviceDependentRedirect) == 1;
        } else {
            return false;
        }
    }

    private static VanityDomain getVanityDomain(String domain) {
        VanityDomain d = new VanityDomain();
        d.setCode(domain);
        return d;
    }

}
