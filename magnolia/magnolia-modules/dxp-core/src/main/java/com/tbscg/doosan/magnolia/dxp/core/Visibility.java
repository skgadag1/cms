package com.tbscg.doosan.magnolia.dxp.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(setterPrefix = "with")
public class Visibility {
    private final String contractCode;
    private final String contractType;
    private final String dealerRole;
}
