package com.tbscg.doosan.magnolia.mediatracker.data.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UrlFilters extends MediatrackerFilters {

    private Region region;
    private String path;
    private String domain;
    private String domainCode;
    private String placement;
    private String trackingParameter;

    private String redirectLocation;
    private String redirectLocationMobile;
    private String redirectLocationDesktop;
    private String redirectLocationTablet;
    private String redirectLocationOther;

    private LocalDate startFrom;
    private LocalDate startTo;
    private LocalDate endFrom;
    private LocalDate endTo;
    private LocalDate createdFrom;
    private LocalDate createdTo;
    private LocalDate modifiedFrom;
    private LocalDate modifiedTo;

}
