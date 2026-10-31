package com.tbscg.doosan.magnolia.passport.data.model.request;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PassportDocumentViewRequest extends PassportRequest {

    private long id;

    public PassportDocumentViewRequest(long id) {
        this.id = id;
    }
}
