package com.tbscg.doosan.magnolia.bobcat.rest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@Slf4j
public class PageVisibility {
    private String contractCode;
    private String contractType;
    private String dealerRole;
}
