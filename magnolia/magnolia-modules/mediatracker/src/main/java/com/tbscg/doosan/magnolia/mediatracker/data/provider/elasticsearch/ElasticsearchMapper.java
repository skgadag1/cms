package com.tbscg.doosan.magnolia.mediatracker.data.provider.elasticsearch;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.tbscg.doosan.magnolia.mediatracker.data.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Class aggregating Elasticsearch mapping functions.
 */
@Slf4j
public class ElasticsearchMapper {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int MAX_RESULTS = 10000;
    private static final String SIZE = "size";
    private static final String QUERY = "query";
    private static final String BOOL = "bool";
    private static final String MUST = "must";
    private static final String MATCH = "match";
    private static final String WILDCARD = "wildcard";
    private static final String RANGE = "range";
    private static final String OPERATOR = "operator";
    private static final String SORT = "sort";
    private static final String SEARCH_AFTER = "search_after";
    private static final String TRACK_TOTAL_HITS = "track_total_hits";
    private static final String DIVISION = "division";
    private static final String TABLE = "tableName";
    private static final String STATUS = "status";
    private static final String CODE = "code.keyword";
    private static final String START_DATE_KEYWORD = "startDate.keyword";
    private static final String END_DATE_KEYWORD = "endDate.keyword";
    private static final String CREATION_DATE_KEYWORD = "creationTime.keyword";
    private static final String MODIFIED_DATE_KEYWORD = "modifiedTime.keyword";
    private static final String FULL_DOMAIN_NAME = "fullDomainName";
    private static final String FULL_DOMAIN_NAME_KEYWORD = "fullDomainName.keyword";
    private static final String DOMAIN = "domain";
    private static final String VANITY_DOMAIN = "vanityDomain";

    public static final String REGION = "region";
    public static final String PATH = "path";
    public static final String PATH_KEYWORD = "path.keyword";
    public static final String PLACEMENT = "placement";
    public static final String TRACKING_PARAMETER = "trackingParameter";
    public static final String REDIRECT_LOCATION = "redirectLocation";
    public static final String REDIRECT_LOCATION_DESKTOP = "redirectLocationDesktop";
    public static final String REDIRECT_LOCATION_MOBILE = "redirectLocationMobile";
    public static final String REDIRECT_LOCATION_TABLET = "redirectLocationTablet";
    public static final String REDIRECT_LOCATION_OTHER = "redirectLocationOther";

    /**
     * Extracts list of domains from Elasticsearch query results.
     *
     * @param jsonQueryResults json string with Elasticsearch query response
     * @return list of domains
     */
    public static List<VanityDomain> resultsToDomainsList(String jsonQueryResults) {
        List<VanityDomain> results = new ArrayList<>();
        if (jsonQueryResults != null && jsonQueryResults.length() > 0) {
            JsonNode node = stringToJsonNode(jsonQueryResults);
            node = node.get("hits");
            if (node != null) {
                JsonNode objArray = node.get("hits");
                if (objArray.isArray()) {
                    for (JsonNode domainContainer : objArray) {
                        JsonNode domainJson = domainContainer.get("_source");
                        ElasticsearchDomain elasticsearchDomain = MAPPER.convertValue(domainJson, ElasticsearchDomain.class);
                        VanityDomain domain = ElasticsearchDomain.toVanityDomain(elasticsearchDomain);
                        if (domain != null) {
                            results.add(domain);
                        }
                    }
                }
            }
        }
        return results;
    }

    /**
     * Extracts list of urls from Elasticsearch query results.
     *
     * @param queryResult json string with Elasticsearch query response
     * @return list of urls
     */
    public static List<VanityUrl> resultsToUrlsList(String queryResult) {
        final JsonNode hits = getHits(queryResult);
        if (hits == null) {
            return Collections.emptyList();
        }

        List<VanityUrl> results = new ArrayList<>();
        for (JsonNode entityContainer : hits) {
            JsonNode entityJson = entityContainer.get("_source");
            ElasticsearchUrl elasticsearchEntity = MAPPER.convertValue(entityJson, ElasticsearchUrl.class);
            VanityUrl entity = ElasticsearchUrl.toVanityUrl(elasticsearchEntity);
            if (entity != null) {
                results.add(entity);
            }
        }
        return results;
    }

    /**
     * Extracts total amount of hits from Elasticsearch query results.
     *
     * @param jsonQueryResults json string with Elasticsearch query response
     * @return total amount of hits
     */
    public static int resultsToTotalHitsValue(String jsonQueryResults) {
        int total = 0;
        if (jsonQueryResults != null && jsonQueryResults.length() > 0) {
            JsonNode node = stringToJsonNode(jsonQueryResults);
            node = node.get("hits");
            if (node != null) {
                JsonNode totalNode = node.get("total");
                if (totalNode != null) {
                    if (totalNode.get("value") != null) {
                        total = totalNode.get("value").asInt();
                    }
                }
            }
        }
        return total;
    }

    /**
     * Builds Elasticsearch JSON query string base on provided parameters.
     *
     * @param q query parameters for Elasticsearch query
     * @return Elasticsearch JSON query string
     */
    public static String toJsonQueryString(ElasticsearchQueryParams q) {
        JSONArray conditions = new JSONArray().putAll(createConditions(q));
        JSONObject query = new JSONObject()
                .put(QUERY, new JSONObject()
                        .put(BOOL, new JSONObject()
                                .put(MUST, new JSONArray()
                                        .putAll(conditions)
                                )
                        )
                );
        if (q.isSkipResults()) {
            query.put("_source", false);
        }
        addSort(q, query);
        addSize(q, query);
        addTrackTotalHits(q, query);
        addSearchAfter(q, query);
        return query.toString();
    }

    private static JsonNode getHits(String jsonQueryResults) {
        if (jsonQueryResults != null && jsonQueryResults.length() > 0) {
            JsonNode node = stringToJsonNode(jsonQueryResults);
            node = node.get("hits");
            if (node != null) {
                JsonNode objArray = node.get("hits");
                if (objArray.isArray()) {
                    return objArray;
                }
            }
        }
        return null;
    }

    public static String[] getSortOfLast(String jsonQueryResults) {
        return getSortOfIndex(jsonQueryResults, SortIndex.LAST);
    }

    public static String[] getSortOfFirst(String jsonQueryResults) {
        return getSortOfIndex(jsonQueryResults, SortIndex.FIRST);
    }

    private static String[] getSortOfIndex(String jsonQueryResults, SortIndex sortIndex) {
        String[] sortOfLast = {};
        if (StringUtils.isNotEmpty(jsonQueryResults)) {
            JsonNode node = stringToJsonNode(jsonQueryResults);
            node = node.get("hits");
            if (node != null) {
                JsonNode objArray = node.get("hits");
                boolean isFirst = SortIndex.FIRST.equals(sortIndex);
                if (objArray.isArray() && objArray.size() > 0 && objArray.get(isFirst ? 0 : objArray.size() - 1).has("sort")) {
                    JsonNode nodeSort = (isFirst ? objArray.get(0) : objArray.get(objArray.size() - 1)).get("sort");
                    ObjectReader objectReader = MAPPER.reader().forType(new TypeReference<String[]>(){});
                    try {
                        sortOfLast = objectReader.readValue(nodeSort);
                    } catch (IOException e) {
                        log.warn("Cannot read sort value from node " + nodeSort.toPrettyString());
                    }
                }
            }
        }
        return sortOfLast;
    }

    private static List<JSONObject> createConditions(ElasticsearchQueryParams q) {
        List<JSONObject> conditions = new ArrayList<>();
        addMatchCondition(conditions, DIVISION, q.getDivision());
        addMatchCondition(conditions, TABLE, q.getTable());
        addMatchCondition(conditions, STATUS, q.getStatus());
        addMatchCondition(conditions, CODE, q.getCode());
        addRangeCondition(conditions, START_DATE_KEYWORD, q.getStartDateFrom(), q.getStartDateTo());
        addRangeCondition(conditions, END_DATE_KEYWORD, q.getEndDateFrom(), q.getEndDateTo());
        addRangeCondition(conditions, CREATION_DATE_KEYWORD, q.getCreationDateFrom(), q.getCreationDateTo());
        addRangeCondition(conditions, MODIFIED_DATE_KEYWORD, q.getModifiedDateFrom(), q.getModifiedDateTo());
        addMatchConditionWithOperator(conditions, FULL_DOMAIN_NAME, q.getFullDomainName(), q.getDomainNameSearchOperator());
        addWildcardCondition(conditions, FULL_DOMAIN_NAME_KEYWORD, q.getFullDomainNameWildcard());

        addMatchCondition(conditions, REGION, q.getRegion());
        if (q.isStrict()) {
            addMatchConditionWithOperator(conditions, PATH_KEYWORD, q.getPath(), q.getPathSearchOperator());
        } else {
            addWildcardCondition(conditions, PATH_KEYWORD, q.getPath(), true);
        }
        addMatchCondition(conditions, DOMAIN, q.getDomain());
        addMatchConditionWithOperator(conditions, VANITY_DOMAIN, q.getVanityDomain(), q.getVanityDomainOperator());
        addMatchCondition(conditions, PLACEMENT, q.getPlacement());
        addMatchCondition(conditions, TRACKING_PARAMETER, q.getTrackingParameter());
        addMatchCondition(conditions, REDIRECT_LOCATION, q.getRedirectLocation());
        addMatchCondition(conditions, REDIRECT_LOCATION_MOBILE, q.getRedirectLocationMobile());
        addMatchCondition(conditions, REDIRECT_LOCATION_DESKTOP, q.getRedirectLocationDesktop());
        addMatchCondition(conditions, REDIRECT_LOCATION_TABLET, q.getRedirectLocationTablet());
        addMatchCondition(conditions, REDIRECT_LOCATION_OTHER, q.getRedirectLocationOther());

        return conditions;
    }

    private static void addRangeCondition(List<JSONObject> conditions, String name, String from, String to) {
        if (StringUtils.isNotBlank(from) || StringUtils.isNotBlank(to)) {
            conditions.add(range(name, from, to));
        }
    }

    private static void addMatchCondition(List<JSONObject> conditions, String name, String value) {
        if (StringUtils.isNotBlank(value)) {
            conditions.add(match(name, value));
        }
    }

    private static void addMatchConditionWithOperator(List<JSONObject> conditions, String name, String value, Operator operator) {
        if (StringUtils.isNotBlank(value)) {
            if (operator != null) {
                JSONObject queryWithOperator = new JSONObject().put(QUERY, value);
                addOperator(queryWithOperator, operator.getValue());
                conditions.add(match(name, queryWithOperator));
            } else {
                addMatchCondition(conditions, name, value);
            }
        }
    }

    private static void addOperator(JSONObject query, String operator) {
        query.put(OPERATOR, operator);
    }

    private static void addWildcardCondition(List<JSONObject> conditions, String name, String value) {
        addWildcardCondition(conditions, name, value, false);
    }

    private static void addWildcardCondition(List<JSONObject> conditions, String name, String value, boolean isCaseSensitive) {
        if (StringUtils.isNotBlank(value)) {
            conditions.add(wildcard(name, value, isCaseSensitive));
        }
    }

    private static JSONObject match(String name, String value) {
        return new JSONObject().put(MATCH, new JSONObject().put(name, value));
    }

    private static JSONObject match(String name, JSONObject value) {
        return new JSONObject().put(MATCH, new JSONObject().put(name, value));
    }

    private static JSONObject wildcard(String name, String value, boolean isCaseSensitive) {
        return new JSONObject().put(WILDCARD, new JSONObject().put(name, new JSONObject().put("value", "*" + value + "*").put("case_insensitive", isCaseSensitive)));
    }

    private static JSONObject range(String name, String from, String to) {
        JSONObject range = new JSONObject();
        if (StringUtils.isNotBlank(from)) {
            range.put("gte", from);
        }
        if (StringUtils.isNotBlank(to)) {
            range.put("lte", to);
        }
        return new JSONObject().put(RANGE, new JSONObject().put(name, range));
    }

    private static void addSort(ElasticsearchQueryParams q, JSONObject query) {
        if (q.getSort() != null && StringUtils.isNoneEmpty(q.getSort().getSortField(), q.getSort().getSortOrder())) {
            JSONArray sort = new JSONArray();
            String direction = q.getSort().getSortOrder();
            sort.put(new JSONObject().put(q.getSort().getSortField(), direction));
            sort.put(new JSONObject().put("_id", direction));
            query.put(SORT, sort);
        }
    }

    private static void addSize(ElasticsearchQueryParams q, JSONObject query) {
        Integer size = q.isFetchAll() || q.getSize() == null ? MAX_RESULTS : q.getSize();
        query.put(SIZE, size);
    }

    private static void addTrackTotalHits(ElasticsearchQueryParams q, JSONObject query) {
        if (q.isTrackTotalHits()) {
            query.put(TRACK_TOTAL_HITS, true);
        }
    }

    private static void addSearchAfter(ElasticsearchQueryParams q, JSONObject query) {
        if (q.getSearchAfter() != null && q.getSearchAfter().length > 0) {
            query.put(SEARCH_AFTER, q.getSearchAfter());
        }
    }

    private static JsonNode stringToJsonNode(final String jsonStr) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(jsonStr);
            return jsonNode;
        } catch (IOException e) {
            log.error("Error processing JSON: " + e.getLocalizedMessage());
            return null;
        }
    }
}
