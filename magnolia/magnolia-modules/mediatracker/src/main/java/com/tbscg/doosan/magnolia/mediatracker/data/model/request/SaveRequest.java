package com.tbscg.doosan.magnolia.mediatracker.data.model.request;

import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityDomain;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityUrl;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@NoArgsConstructor
public class SaveRequest extends EditRequest {

    private OffsetDateTime lastUpdatedTime;

    private Boolean isDeleted;

    public SaveRequest(VanityUrl url, boolean isDeleted) {
        super(url);
        this.lastUpdatedTime = url.getModifiedTime();
        this.isDeleted = isDeleted;
    }

    public SaveRequest(VanityDomain domain, boolean isDeleted) {
        super(domain);
        this.lastUpdatedTime = domain.getModifiedTime();
        this.isDeleted = isDeleted;
    }

    public OffsetDateTime getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }
}
