package com.tbscg.doosan.magnolia.mediatracker.data.provider.elasticsearch;


import com.tbscg.doosan.magnolia.mediatracker.data.model.Division;
import com.tbscg.doosan.magnolia.mediatracker.data.model.Status;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityDomain;
import com.tbscg.doosan.magnolia.mediatracker.util.DateFormatUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.util.Strings;

import java.time.OffsetDateTime;

/**
 * Class representing Elasticsearch domain entity.
 */
@Getter
@Setter
public class ElasticsearchDomain {

    private String tableName;
    private String division;
    private String modifiedTime;
    private String code;
    private String creationTime;
    private String createdBy;
    private String fullDomainName;
    private String modifiedBy;
    private String startDate;
    private String endDate;
    private String status;

    /**
     * Maps Elasticsearch domain entity to its content app version
     *
     * @param d Elasticsearch domain entity
     * @return content app domain entity
     */
    public static VanityDomain toVanityDomain(ElasticsearchDomain d) {
        VanityDomain domain = new VanityDomain();
        domain.setDivision(Division.find(d.getDivision()));
        domain.setModifiedTime(d.getModifiedTime() != null ? parse(d.getModifiedTime()) : null);
        domain.setCode(d.getCode());
        domain.setCreationTime(d.getCreationTime() != null ? OffsetDateTime.parse(d.getCreationTime(), DateFormatUtil.FORMATTER) : null);
        domain.setCreatedBy(d.getCreatedBy());
        domain.setFullDomainName(d.getFullDomainName());
        domain.setModifiedBy(d.getModifiedBy());
        domain.setStartDate(d.getStartDate() != null ? OffsetDateTime.parse(d.getStartDate(), DateFormatUtil.FORMATTER) : null);
        domain.setEndDate(d.getEndDate() != null ? OffsetDateTime.parse(d.getEndDate(), DateFormatUtil.FORMATTER) : null);
        domain.setStatus(Strings.isNotBlank(d.getStatus()) ? Status.findIgnoreCase(d.getStatus().trim()) : null);
        return domain;
    }

    private static OffsetDateTime parse(String datetime) {
        return OffsetDateTime.parse(datetime, DateFormatUtil.FORMATTER);
    }

}
