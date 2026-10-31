package com.tbscg.doosan.magnolia.mediatracker.data.model.request;

import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityDomain;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ShowRelatedUrlsRequest {

    private String code;
    private String division;

    public ShowRelatedUrlsRequest(VanityDomain vanityDomain) {
        this.code = vanityDomain.getCode();
        this.division = vanityDomain.getDivision().toString();
    }

}
