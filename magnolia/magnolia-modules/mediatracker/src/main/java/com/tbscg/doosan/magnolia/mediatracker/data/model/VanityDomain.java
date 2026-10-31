package com.tbscg.doosan.magnolia.mediatracker.data.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class VanityDomain implements BaseVanityData {

    private String code;
    private Division division;
    private String fullDomainName;
    private Status status;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private OffsetDateTime creationTime;
    private OffsetDateTime modifiedTime;
    private String createdBy;
    private String modifiedBy;

    public VanityDomain(VanityDomain src) {
        this.code = src.code;
        this.division = src.division;
        this.fullDomainName = src.fullDomainName;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VanityDomain that = (VanityDomain) o;
        return new EqualsBuilder().append(code, that.code).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(code).toHashCode();
    }

}
