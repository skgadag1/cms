package com.tbscg.doosan.magnolia.mediatracker.data.provider.elasticsearch;

import com.tbscg.doosan.magnolia.mediatracker.data.model.*;
import com.tbscg.doosan.magnolia.mediatracker.util.DateFormatUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.swing.text.html.Option;
import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

/**
 * Class aggregating parameters used by Elasticsearch methods.
 */
@Getter
@Setter
@Builder
public class ElasticsearchQueryParams {

    //common
    private String code;
    private String division;
    private String status;
    private String table;
    private String startDateFrom;
    private String startDateTo;
    private String endDateFrom;
    private String endDateTo;
    private String creationDateFrom;
    private String creationDateTo;
    private String modifiedDateFrom;
    private String modifiedDateTo;
    private boolean skipResults;

    //domains only
    private String fullDomainName;
    private String fullDomainNameWildcard;
    private Operator domainNameSearchOperator;

    //urls only
    private String domain;
    private String region;
    private String path;
    private boolean isStrict;
    private Operator pathSearchOperator;
    private String placement;
    private String trackingParameter;
    private String redirectLocation;
    private String redirectLocationMobile;
    private String redirectLocationDesktop;
    private String redirectLocationTablet;
    private String redirectLocationOther;
    private String vanityDomain;
    private Operator vanityDomainOperator;

    //pagination
    private Sort sort;
    private Integer size;
    private String[] searchAfter;
    private boolean fetchAll;
    private boolean trackTotalHits;

    /**
     * Maps code param to ElasticsearchQueryParams
     *
     * @param code value of code parameter
     * @param table table to use
     * @return ElasticsearchQueryParams object
     */
    public static ElasticsearchQueryParams fromCode(String code, String table) {
        return getByCodeAndTable(code, table);
    }

    /**
     * Maps vanityDomain param to ElasticsearchQueryParams
     *
     * @param vanityDomain value of vanityDomain parameter
     * @param table table to use
     * @return ElasticsearchQueryParams object
     */
    public static ElasticsearchQueryParams fromVanityDomain(String vanityDomain, String table) {
        return getByVanityDomainAndTable(vanityDomain, table);
    }

    /**
     * Maps domainName param to ElasticsearchQueryParams
     *
     * @param domainName value of domainName parameter
     * @return ElasticsearchQueryParams object
     */
    public static ElasticsearchQueryParams fromDomainName(String domainName, String domainsTable) {
        return ElasticsearchQueryParams.builder()
                .table(domainsTable)
                .fullDomainName(domainName)
                .domainNameSearchOperator(Operator.AND)
                .build();
    }

    private static ElasticsearchQueryParams getByCodeAndTable(String code, String table) {
        return ElasticsearchQueryParams.builder()
                .table(table)
                .code(code)
                .size(1)
                .build();
    }

    private static ElasticsearchQueryParams getByVanityDomainAndTable(String vanityDomain, String table) {
        return ElasticsearchQueryParams.builder()
                .table(table)
                .vanityDomain(vanityDomain)
                .build();
    }

    /**
     * Maps DomainFilters object to ElasticsearchQueryParams
     *
     * @param df DomainFilters object
     * @param domainsTable name of Elasticsearch table storing domains
     * @return ElasticsearchQueryParams object
     */
    public static ElasticsearchQueryParams fromDomainFilters(DomainFilters df, String domainsTable) {
        return ElasticsearchQueryParams.builder()
                .table(domainsTable)
                .division(convertDivision(df.getDivision()))
                .status(convertStatus(df.getStatus()))
                .fullDomainNameWildcard(df.getName())
                .startDateFrom(convertFromDate(df.getStartFrom()))
                .startDateTo(convertToDate(df.getStartTo()))
                .endDateFrom(convertFromDate(df.getEndFrom()))
                .endDateTo(convertToDate(df.getEndTo()))
                .creationDateFrom(convertFromDate(df.getCreatedFrom()))
                .creationDateTo(convertToDate(df.getCreatedTo()))
                .modifiedDateFrom(convertFromDate(df.getModifiedFrom()))
                .modifiedDateTo(convertToDate(df.getModifiedTo()))
                .sort(df.getSort())
                .size(df.getSize())
                .fetchAll(df.isFetchAll())
                .searchAfter(convertSearchAfter(df.getSearchAfter(), df.getSearchBefore()))
                .build();
    }

    public static ElasticsearchQueryParams fromDivision(Division division, String domainsTable) {
        return ElasticsearchQueryParams.builder()
                .table(domainsTable)
                .division(convertDivision(division))
                .build();
    }

    /**
     * Maps DomainFilters object to ElasticsearchQueryParams
     *
     * @param f UrlFilters object
     * @param urlsTable name of Elasticsearch table storing domains
     * @return ElasticsearchQueryParams object
     */
    public static ElasticsearchQueryParams fromUrlFilters(UrlFilters f, String urlsTable) {
        return ElasticsearchQueryParams.builder()
                .table(urlsTable)
                .division(convertDivision(f.getDivision()))
                .status(convertStatus(f.getStatus()))
                .vanityDomain(f.getDomainCode())
                .startDateFrom(convertFromDate(f.getStartFrom()))
                .startDateTo(convertToDate(f.getStartTo()))
                .endDateFrom(convertFromDate(f.getEndFrom()))
                .endDateTo(convertToDate(f.getEndTo()))
                .creationDateFrom(convertFromDate(f.getCreatedFrom()))
                .creationDateTo(convertToDate(f.getCreatedTo()))
                .modifiedDateFrom(convertFromDate(f.getModifiedFrom()))
                .modifiedDateTo(convertToDate(f.getModifiedTo()))
                .region(f.getRegion() != null ? f.getRegion().toString() : null)
                .path(f.getPath())
                .placement(f.getPlacement())
                .trackingParameter(f.getTrackingParameter())
                .redirectLocation(f.getRedirectLocation())
                .redirectLocationMobile(f.getRedirectLocationMobile())
                .redirectLocationDesktop(f.getRedirectLocationDesktop())
                .redirectLocationTablet(f.getRedirectLocationTablet())
                .redirectLocationOther(f.getRedirectLocationOther())
                .sort(f.getSort())
                .size(f.getSize())
                .fetchAll(f.isFetchAll())
                .searchAfter(convertSearchAfter(f.getSearchAfter(), f.getSearchBefore()))
                .build();
    }

    /**
     * Maps urlPath param to ElasticsearchQueryParams
     *
     * @param urlPath value of url path parameter
     * @param domainCode value of vanity domain's code
     * @param urlsTable name of Elasticsearch table storing urls
     * @return ElasticsearchQueryParams object
     */
    public static ElasticsearchQueryParams fromUrlPath(String urlPath, String domainCode, String urlsTable, boolean isStrict) {
        return ElasticsearchQueryParams.builder()
                .table(urlsTable)
                .path(urlPath)
                .isStrict(isStrict)
                .pathSearchOperator(Operator.AND)
                .vanityDomain(domainCode)
                .vanityDomainOperator(Operator.AND)
                .build();
    }

    private static String convertDivision(Division division) {
        return division != null ? division.toString() : "";
    }

    private static String convertStatus(Status[] s) {
        return s != null && s[0] != null ? s[0].name() : "";
    }

    private static String convertFromDate(LocalDate date) {
        return convertDate(date, LocalTime.MIN, OffsetDateTime.now().getOffset());
    }

    private static String convertToDate(LocalDate date) {
        return convertDate(date, LocalTime.MAX, OffsetDateTime.now().getOffset());
    }

    private static String convertDate(LocalDate date, LocalTime time, ZoneOffset offset) {
        if (date != null) {
            return DateFormatUtil.FORMATTER.format(OffsetDateTime.of(date, time, offset));
        } else {
            return "";
        }
    }

    private static String[] convertSearchAfter(String[] searchAfter, String[] searchBefore) {
        return Optional.ofNullable(searchAfter).map(Array::getLength).orElse(0) > 0 ? searchAfter : Optional.ofNullable(searchBefore).orElse(new String[]{});
    }

}
