package com.tbscg.doosan.magnolia.mediatracker.data.model.request;

import com.tbscg.doosan.magnolia.mediatracker.data.model.Division;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityDomain;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityUrl;

public class EditRequest extends MediatrackerRequest {

    private String code;

    public EditRequest() {
    }

    public EditRequest(Division division, String code) {
        super(division);
        this.code = code;
    }

    public EditRequest(VanityDomain domain) {
        super(domain != null ? domain.getDivision() : null);
        this.code = domain != null ? domain.getCode() : "";
    }

    public EditRequest(VanityUrl url) {
        super(url != null ? url.getDivision() : null);
        this.code = url != null ? url.getCode() : "";
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
