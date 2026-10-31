package com.tbscg.doosan.magnolia.mediatracker.data.model.request;

import com.tbscg.doosan.magnolia.mediatracker.data.model.Division;

public class MediatrackerRequest {

    private Division division;

    public MediatrackerRequest() {
    }

    public MediatrackerRequest(Division division) {
        this.division = division;
    }

    public Division getDivision() {
        return division;
    }

    public void setDivision(Division division) {
        this.division = division;
    }
}
