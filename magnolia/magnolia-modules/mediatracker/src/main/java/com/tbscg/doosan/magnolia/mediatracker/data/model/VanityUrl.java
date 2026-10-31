package com.tbscg.doosan.magnolia.mediatracker.data.model;

import lombok.*;
import org.apache.commons.lang.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class VanityUrl implements BaseVanityData {

    private String code;
    private String path;
    private String redirectLocation;
    private boolean deviceDependentRedirect;
    private String redirectLocationMobile;
    private String redirectLocationDesktop;
    private String redirectLocationTablet;
    private String redirectLocationOther;
    private RedirectType redirectType = RedirectType.PERMANENT_301;
    private String placement;
    private String trackingParameter;
    private VanityDomain vanityDomain;
    private OffsetDateTime creationTime;
    private OffsetDateTime modifiedTime;
    private String createdBy;
    private String modifiedBy;
    private Division division;
    private Region region = Region.GLOBAL;
    private Status status = Status.DRAFT;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;

    public VanityUrl() {

    }

    public VanityUrl(VanityUrl src) {
        this.code = src.code;
        this.division = src.division;
        this.region = src.region;
        this.vanityDomain = src.vanityDomain;
        this.path = src.path;
        this.redirectLocation = src.redirectLocation;
        this.redirectLocationMobile = src.redirectLocationMobile;
        this.redirectLocationDesktop = src.redirectLocationDesktop;
        this.redirectLocationTablet = src.redirectLocationTablet;
        this.redirectLocationOther = src.redirectLocationOther;
        this.redirectType = src.redirectType;
        this.placement = src.placement;
        this.trackingParameter = src.trackingParameter;
        this.status = src.status;
        this.startDate = src.startDate;
        this.endDate = src.endDate;
        this.creationTime = src.creationTime;
        this.modifiedTime = src.modifiedTime;
        this.createdBy = src.createdBy;
        this.modifiedBy = src.modifiedBy;
    }

    public String getDisplayedStatus() {
        return status != null ? status.getLabel() : "";
    }

    public VanityDomain scanForDomain(List<VanityDomain> domains) {
        for (VanityDomain domain : domains) {
            if (domain.getCode().equals(this.getVanityDomain().getCode())) {
                return domain;
            }
        }
        return null;
    }

    public String getFullURL() {
        return buildFullURL(this.vanityDomain.getFullDomainName());
    }

    public String getFullURL(VanityDomain vanityDomain) {
        return buildFullURL(vanityDomain.getFullDomainName());
    }

    private String buildFullURL(String domainName) {
        domainName = StringUtils.removeEnd(domainName, "/");
        domainName = StringUtils.removeEnd(domainName, "\\");
        String path = StringUtils.removeStart(this.path, "/");
        path = StringUtils.removeStart(path, "\\");
        return domainName + "/" + path;
    }

}
