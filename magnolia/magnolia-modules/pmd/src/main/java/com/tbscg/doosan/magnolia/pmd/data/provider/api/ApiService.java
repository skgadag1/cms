package com.tbscg.doosan.magnolia.pmd.data.provider.api;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tbscg.doosan.magnolia.pmd.PmdModule;
import com.tbscg.doosan.magnolia.pmd.data.model.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import javax.inject.Inject;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class ApiService {

    private final TokenService tokenService;
    private final PmdModule pmdModule;
    private final ApiOptions options;
    private Cache<String, String> cache;

    @Inject
    public ApiService(PmdModule pmdModule, TokenService tokenService) {
        this.pmdModule = pmdModule;
        this.tokenService = tokenService;
        this.options = pmdModule.getApiOptions();
        if (this.options.isCacheEnabled()) {
            this.cache = CacheBuilder.newBuilder()
                    .maximumSize(this.options.getCacheMaxSize())
                    .expireAfterWrite(this.options.getCacheTtl(), TimeUnit.SECONDS)
                    .build();
        }
    }

    private void invalidateCache(String pathPrefix) {
        this.cache.asMap()
                .keySet().stream()
                .filter(k -> k.startsWith(pathPrefix))
                .forEach(k -> {
                    log.info("Evicting API cache entry: {}", k);
                    this.cache.invalidate(k);
                });
    }

    private HttpService getHttpService() {
        HttpService http = new HttpService(this.options.getBaseUrl());
        http.setAccessTokenProvider(this.tokenService::getToken);
        return http;
    }

    @SneakyThrows
    private static String getQueryString(Object... params) {
        StringBuilder qs = new StringBuilder();
        for (int i = 0; i < params.length - 1; i += 2) {
            if (qs.length() == 0) {
                qs.append("?");
            } else {
                qs.append("&");
            }
            qs.append(params[i]);
            qs.append("=");
            qs.append(URLEncoder.encode(String.valueOf(params[i + 1]), StandardCharsets.UTF_8.name()));
        }
        return qs.toString();
    }

    @SneakyThrows
    private String getCachedResult(String path) {
        if (this.cache != null) {
            return this.cache.get(path, () -> getHttpService().get(path));
        } else {
            return getHttpService().get(path);
        }
    }

    public List<Brand> getBrands() {
        String result = getCachedResult("/hierarchy/brand");
        return ApiMapper.list(result, Brand.class)
                .stream().sorted(Comparator.comparing(Brand::getName))
                .collect(Collectors.toList());
    }

    private void invalidateBrands() {
        invalidateCache("/hierarchy/brand");
    }

    public Brand getBrand(long id) {
        String path = "/hierarchy/brand/" + id;
        String result = getCachedResult(path);
        return ApiMapper.map(result, Brand.class);
    }

    public void deleteBrand(long id) {
        String path = "/hierarchy/brand/" + id;
        getHttpService().delete(path);
        invalidateBrands();
    }

    public Brand addBrand(PmiData data) {
        String path = "/hierarchy/brand";
        String json = ApiMapper.toJson(data);
        String result = getHttpService().postJson(path, json);
        invalidateBrands();
        return ApiMapper.map(result, Brand.class);
    }

    public Brand updateBrand(long id, PmiData data) {
        String path = "/hierarchy/brand/" + id;
        String json = ApiMapper.toJson(data);
        String result = getHttpService().putJson(path, json);
        invalidateBrands();
        return ApiMapper.map(result, Brand.class);
    }

    public List<NameTranslation> getBrandTranslations(long id) {
        String transResult = getHttpService().get("/hierarchy/brand/" + id + "/translations");
        return ApiMapper.list(transResult, NameTranslation.class);
    }


    public List<Family> getFamilies(Brand brand) {
        String result = getCachedResult("/hierarchy/family" + getQueryString("brand_id", brand.getId()));
        return ApiMapper.list(result, Family.class)
                .stream().sorted(Comparator.comparing(Family::getName))
                .collect(Collectors.toList());
    }

    private void invalidateFamilies() {
        invalidateCache("/hierarchy/family");
    }

    public Family getFamily(long id) {
        String path = "/hierarchy/family/" + id;
        String result = getCachedResult(path);
        return ApiMapper.map(result, Family.class);
    }

    public void deleteFamily(long id) {
        String path = "/hierarchy/family/" + id;
        getHttpService().delete(path);
        invalidateFamilies();
    }

    public Family addFamily(FamilyData data) {
        String path = "/hierarchy/family";
        String json = ApiMapper.toJson(data);
        String result = getHttpService().postJson(path, json);
        invalidateFamilies();
        return ApiMapper.map(result, Family.class);
    }

    public Family updateFamily(long id, FamilyData data) {
        String path = "/hierarchy/family/" + id;
        String json = ApiMapper.toJson(data);
        String result = getHttpService().putJson(path, json);
        invalidateFamilies();
        return ApiMapper.map(result, Family.class);
    }

    public List<NameTranslation> getFamilyTranslations(long id) {
        String transResult = getHttpService().get("/hierarchy/family/" + id + "/translations");
        return ApiMapper.list(transResult, NameTranslation.class);
    }

    public List<Subfamily> getSubfamilies(Family family) {
        String result = getCachedResult("/hierarchy/subfamily" + getQueryString("family_id", family.getId()));
        return ApiMapper.list(result, Subfamily.class)
                .stream().sorted(Comparator.comparing(Subfamily::getName))
                .collect(Collectors.toList());
    }

    private void invalidateSubfamilies() {
        invalidateCache("/hierarchy/subfamily");
    }

    public Subfamily getSubfamily(long id) {
        String path = "/hierarchy/subfamily/" + id;
        String result = getCachedResult(path);
        return ApiMapper.map(result, Subfamily.class);
    }

    public void deleteSubfamily(long id) {
        String path = "/hierarchy/subfamily/" + id;
        getHttpService().delete(path);
        invalidateSubfamilies();
    }

    public Subfamily addSubfamily(SubfamilyData data) {
        String path = "/hierarchy/subfamily";
        String json = ApiMapper.toJson(data);
        String result = getHttpService().postJson(path, json);
        invalidateSubfamilies();
        return ApiMapper.map(result, Subfamily.class);
    }

    public Subfamily updateSubfamily(long id, SubfamilyData data) {
        String path = "/hierarchy/subfamily/" + id;
        String json = ApiMapper.toJson(data);
        String result = getHttpService().putJson(path, json);
        invalidateSubfamilies();
        return ApiMapper.map(result, Subfamily.class);
    }

    public List<NameTranslation> getSubfamilyTranslations(long id) {
        String transResult = getHttpService().get("/hierarchy/subfamily/" + id + "/translations");
        return ApiMapper.list(transResult, NameTranslation.class);
    }

    public List<Model> getModels(String name) {
        String result = getCachedResult("/hierarchy/model" + getQueryString("code", name));
        return ApiMapper.list(result, Model.class);
    }

    public List<Model> getModels(Subfamily subfamily) {
        String result = getCachedResult("/hierarchy/model" + getQueryString("subfamily_id", subfamily.getId()));
        return ApiMapper.list(result, Model.class)
                .stream().sorted(Comparator.comparing(Model::getCode))
                .collect(Collectors.toList());
    }

    private void invalidateModels() {
        invalidateCache("/hierarchy/model");
    }

    public Model getModel(long id) {
        String path = "/hierarchy/model/" + id;
        String result = getCachedResult(path);
        return ApiMapper.map(result, Model.class);
    }

    public void deleteModel(long id) {
        String path = "/hierarchy/model/" + id;
        getHttpService().delete(path);
        invalidateModels();
    }

    public Model addModel(ModelData data) {
        String path = "/hierarchy/model";
        String json = ApiMapper.toJson(data);
        String result = getHttpService().postJson(path, json);
        invalidateModels();
        return ApiMapper.map(result, Model.class);
    }

    public Model updateModel(long id, ModelData data) {
        String path = "/hierarchy/model/" + id;
        String json = ApiMapper.toJson(data);
        String result = getHttpService().putJson(path, json);
        invalidateModels();
        return ApiMapper.map(result, Model.class);
    }

    public List<Prefix> getPrefixes(String prefix) {
        String result = getCachedResult("/hierarchy/prefix" + getQueryString("prefix", prefix));
        return ApiMapper.list(result, Prefix.class);
    }

    public List<Prefix> getPrefixes(Model model) {
        String result = getCachedResult("/hierarchy/prefix" + getQueryString("model_id", model.getId()));
        return ApiMapper.list(result, Prefix.class)
                .stream().sorted(Comparator.comparing(Prefix::getPrefix))
                .collect(Collectors.toList());
    }

    private void invalidatePrefixes() {
        invalidateCache("/hierarchy/prefix");
    }

    public Prefix getPrefix(long id) {
        String path = "/hierarchy/prefix/" + id;
        String result = getHttpService().get(path);
        return ApiMapper.map(result, Prefix.class);
    }

    public void deletePrefix(long id) {
        String path = "/hierarchy/prefix/" + id;
        getHttpService().delete(path);
        invalidatePrefixes();
    }

    public Prefix addPrefix(PrefixData data) {
        String path = "/hierarchy/prefix";
        String json = ApiMapper.toJson(data);
        String result = getHttpService().postJson(path, json);
        invalidatePrefixes();
        return ApiMapper.map(result, Prefix.class);
    }

    public Prefix updatePrefix(long id, PrefixData data) {
        String path = "/hierarchy/prefix/" + id;
        String json = ApiMapper.toJson(data);
        String result = getHttpService().putJson(path, json);
        invalidatePrefixes();
        invalidateRanges();
        return ApiMapper.map(result, Prefix.class);
    }

    public List<Range> getRanges(Prefix prefix) {
        String result = getCachedResult("/hierarchy/range" + getQueryString("prefix_id", prefix.getId()));
        return ApiMapper.list(result, Range.class)
                .stream().sorted(Comparator.comparing(Range::getSuffixStart))
                .collect(Collectors.toList());
    }

    private void invalidateRanges() {
        invalidateCache("/hierarchy/range");
    }

    public Range getRange(long id) {
        String path = "/hierarchy/range/" + id;
        String result = getHttpService().get(path);
        return ApiMapper.map(result, Range.class);
    }

    public void deleteRange(long id) {
        String path = "/hierarchy/range/" + id;
        getHttpService().delete(path);
        invalidateRanges();
    }

    public Range addRange(RangeData data) {
        String path = "/hierarchy/range";
        String json = ApiMapper.toJson(data);
        String result = getHttpService().postJson(path, json);
        invalidateRanges();
        return ApiMapper.map(result, Range.class);
    }

    public Range updateRange(long id, RangeData data) {
        String path = "/hierarchy/range/" + id;
        String json = ApiMapper.toJson(data);
        String result = getHttpService().putJson(path, json);
        invalidateRanges();
        return ApiMapper.map(result, Range.class);
    }

    public List<Document> getDocuments(Object... params) {
        String result = getCachedResult("/document" + getQueryString(params));
        DocumentsResponse documents = ApiMapper.map(result, DocumentsResponse.class);
        return documents.getDocuments();
    }

    public Document getDocument(long id) {
        String result = getCachedResult("/document/" + id);
        return ApiMapper.map(result, Document.class);
    }

    public List<DocumentTranslation> getDocumentTranslations(long id) {
        String transResult = getCachedResult("/document/" + id + "/translations");
        return ApiMapper.list(transResult, DocumentTranslation.class);
    }

    public List<DocumentConnection> getDocumentConnections(long documentId) {
        String result = getCachedResult("/document/" + documentId + "/connection");
        return ApiMapper.list(result, DocumentConnection.class);
    }

    public List<DocumentFile> getDocumentFiles(long documentId) {
        String result = getCachedResult("/document/" + documentId + "/file");
        return ApiMapper.list(result, DocumentFile.class);
    }

    public List<Engine> getEngines() {
        String result = getCachedResult("/engine");
        List<Engine> engines = ApiMapper.list(result, Engine.class);
        return engines.stream()
            .filter(engine -> Objects.nonNull(engine.getName()))
            .sorted(Comparator.comparing(Engine::getName))
            .collect(Collectors.toList());
    }

    public List<DocumentType> getDocumentTypes() {
        String result = getCachedResult("/document-type");
        return ApiMapper.list(result, DocumentType.class);
    }

    public Id connectDocument(Document document, ConnectTarget target) {
        String path = "/document/" + document.getId() + "/connection";
        JSONObject json = new JSONObject()
                .put("target_id", target.getId())
                .put("target_type", target.getType());
        String result = getHttpService().postJson(path, json.toString());
        return ApiMapper.map(result, Id.class);
    }

    public Document addDocument(DocumentData doc) {
        String path = "/document";
        String json = ApiMapper.toJson(doc);
        String result = getHttpService().postJson(path, json);
        return ApiMapper.map(result, Document.class);
    }

    public DocumentFileResponse addDocumentFile(long documentId, DocumentFileData data) {
        String path = "/document/" + documentId + "/file";
        String json = ApiMapper.toJson(data);
        String result = getHttpService().postJson(path, json);
        return ApiMapper.map(result, DocumentFileResponse.class);
    }

    public Document updateDocument(long documentId, DocumentData doc) {
        String path = "/document/" + documentId;
        String json = ApiMapper.toJson(doc);
        String result = getHttpService().putJson(path, json);
        return ApiMapper.map(result, Document.class);
    }

    public void disconnectDocument(long documentId, long connectionId) {
        String path = "/document/" + documentId + "/connection/" + connectionId;
        getHttpService().delete(path);
    }

    public void deleteDocumentFile(long documentId, long fileId) {
        String path = "/document/" + documentId + "/file/" + fileId;
        getHttpService().delete(path);
    }

    public SchematicsResponse getSchematics(Object... params) {
        String result = getHttpService().get("/schematics" + getQueryString(params));
        return ApiMapper.map(result, SchematicsResponse.class);
    }

    public List<SchematicType> getSchematicTypes() {
        String result = getCachedResult("/schematics/categories");
        return ApiMapper.list(result, SchematicType.class);
    }

    public List<Locale> getLocales() {
        String result = getCachedResult("/locale");
        return ApiMapper.list(result, Locale.class).stream()
                .sorted(Comparator
                        .comparing(Locale::isFallback).reversed()
                        .thenComparing(Locale::getName))
                .collect(Collectors.toList());
    }

    public Locale getLocaleByCode(String localeCode) {
        return getLocales().stream()
                .filter(l -> localeCode.equals(l.getCode()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unsupported locale: " + localeCode));
    }

    public Schematic getSchematic(long id) {
        String result = getHttpService().get("/schematics/" + id);
        return ApiMapper.map(result, Schematic.class);
    }

    public void deleteSchematic(long schematicId) {
        String path = "/schematics/" + schematicId;
        getHttpService().delete(path);
    }

    public Schematic addSchematic(SchematicData data) {
        String path = "/schematics";
        String json = ApiMapper.toJson(data);
        String result = getHttpService().postJson(path, json);
        return ApiMapper.map(result, Schematic.class);
    }

    public Schematic updateSchematic(long schematicId, SchematicData data) {
        String path = "/schematics/" + schematicId;
        String json = ApiMapper.toJson(data);
        String result = getHttpService().putJson(path, json);
        return ApiMapper.map(result, Schematic.class);
    }

    public List<SchematicTranslation> getSchematicTranslations(long id) {
        String transResult = getHttpService().get("/schematics/" + id + "/translations");
        return ApiMapper.list(transResult, SchematicTranslation.class);
    }

    public List<SchematicConnection> getSchematicConnections(long schematicId) {
        String result = getHttpService().get("/schematics/" + schematicId + "/connection");
        return ApiMapper.list(result, SchematicConnection.class);
    }

    public List<DocumentFile> getSchematicFiles(long schematicId) {
        String result = getHttpService().get("/schematics/" + schematicId + "/file");
        return ApiMapper.list(result, DocumentFile.class);
    }

    public Id connectSchematic(Schematic schematic, SchematicConnectionData data) {
        String path = "/schematics/" + schematic.getId() + "/connection";
        String json = ApiMapper.toJson(data);
        String result = getHttpService().postJson(path, json);
        return ApiMapper.map(result, Id.class);
    }

    public void disconnectSchematic(long schematicId, long connectionId) {
        String path = "/schematics/" + schematicId + "/connection/" + connectionId;
        getHttpService().delete(path);
    }

    public DocumentFileResponse addSchematicFile(long schematicId, DocumentFileData data) {
        String path = "/schematics/" + schematicId + "/file";
        String json = ApiMapper.toJson(data);
        String result = getHttpService().postJson(path, json);
        return ApiMapper.map(result, DocumentFileResponse.class);
    }

    public void deleteSchematicFile(long schematicId, long fileId) {
        String path = "/schematics/" + schematicId + "/file/" + fileId;
        getHttpService().delete(path);
    }

    public SchedulesResponse getSchedules(Object... params) {
        String result = getHttpService().get("/service/schedules" + getQueryString(params));
        return ApiMapper.map(result, SchedulesResponse.class);
    }

    public Schedule getSchedule(long id) {
        String result = getHttpService().get("/service/schedules/" + id);
        return ApiMapper.map(result, Schedule.class);
    }

    public void deleteSchedule(long scheduleId) {
        getHttpService().delete("/service/schedules/" + scheduleId);
    }

    public Schedule addSchedule(ScheduleData data) {
        String path = "/service/schedules";
        String json = ApiMapper.toJson(data);
        String result = getHttpService().postJson(path, json);
        return ApiMapper.map(result, Schedule.class);
    }

    public Schedule updateSchedule(long scheduleId, ScheduleData data) {
        String path = "/service/schedules/" + scheduleId;
        String json = ApiMapper.toJson(data);
        String result = getHttpService().putJson(path, json);
        return ApiMapper.map(result, Schedule.class);
    }

    public List<ScheduleTranslation> getScheduleTranslations(long id) {
        String transResult = getHttpService().get("/service/schedules/" + id + "/translations");
        return ApiMapper.list(transResult, ScheduleTranslation.class);
    }

    private void invalidateScheduleConnections(long scheduleId) {
        invalidateCache("/service/schedules/" + scheduleId + "/connection");
    }

    public List<ScheduleConnection> getScheduleConnections(long scheduleId) {
        String result = getCachedResult("/service/schedules/" + scheduleId + "/connection");
        return ApiMapper.list(result, ScheduleConnection.class);
    }

    public Id connectSchedule(Schedule schedule, ScheduleConnectionData data) {
        String path = "/service/schedules/" + schedule.getId() + "/connection";
        String json = ApiMapper.toJson(data);
        String result = getHttpService().postJson(path, json);
        invalidateScheduleConnections(schedule.getId());
        return ApiMapper.map(result, Id.class);
    }

    public void disconnectSchedule(long scheduleId, long connectionId) {
        String path = "/service/schedules/" + scheduleId + "/connection/" + connectionId;
        getHttpService().delete(path);
        invalidateScheduleConnections(scheduleId);
    }

    public List<Interval> getIntervals(long scheduleId) {
        String result = getCachedResult("/service/schedules/" + scheduleId + "/intervals");
        return ApiMapper.list(result, Interval.class);
    }

    private void invalidateInterval(Interval interval) {
        if (interval != null) {
            invalidateCache("/service/schedules/" + interval.getScheduleId() + "/intervals");
        }
    }

    public Interval addInterval(Schedule schedule, IntervalData data) {
        String path = "/service/schedules/" + schedule.getId() + "/intervals";
        String json = ApiMapper.toJson(data);
        String result = getHttpService().postJson(path, json);
        Interval interval = ApiMapper.map(result, Interval.class);
        invalidateInterval(interval);
        return interval;
    }

    public Interval updateInterval(long intervalId, IntervalData data) {
        String path = "/service/intervals/" + intervalId;
        String json = ApiMapper.toJson(data);
        String result = getHttpService().putJson(path, json);
        Interval interval = ApiMapper.map(result, Interval.class);
        invalidateInterval(interval);
        return interval;
    }

    public Interval getInterval(long id) {
        String result = getHttpService().get("/service/intervals/" + id);
        return ApiMapper.map(result, Interval.class);
    }

    public void deleteInterval(long intervalId) {
        Interval interval = getInterval(intervalId);
        if (interval != null) {
            getHttpService().delete("/service/intervals/" + intervalId);
            invalidateInterval(interval);
        }
    }

    public List<NameTranslation> getIntervalTranslations(long intervalId) {
        String transResult = getHttpService().get("/service/intervals/" + intervalId + "/translations");
        return ApiMapper.list(transResult, NameTranslation.class);
    }

    public List<IntervalTriggerType> getIntervalTriggerTypes() {
        String result = getCachedResult("/service/trigger_types");
        return ApiMapper.list(result, IntervalTriggerType.class);
    }

    private void invalidateTriggerCache(Interval interval) {
        if (interval != null) {
            invalidateCache("/service/intervals/" + interval.getId() + "/triggers");
        }
    }

    public List<IntervalTrigger> getIntervalTriggers(long intervalId) {
        String result = getCachedResult("/service/intervals/" + intervalId + "/triggers");
        return ApiMapper.list(result, IntervalTrigger.class);
    }

    public IntervalTrigger addIntervalTrigger(Interval interval, IntervalTriggerData data) {
        String path = "/service/intervals/" + interval.getId() + "/triggers";
        String json = ApiMapper.toJson(data);
        String result = getHttpService().postJson(path, json);
        invalidateTriggerCache(interval);
        return ApiMapper.map(result, IntervalTrigger.class);
    }

    public void deleteIntervalTrigger(Interval interval, long triggerId) {
        getHttpService().delete("/service/intervals/" + interval.getId() + "/triggers/" + triggerId);
        invalidateTriggerCache(interval);
    }

    public List<ServiceActionType> getServiceActionTypes() {
        String result = getCachedResult("/service/action_types");
        return ApiMapper.list(result, ServiceActionType.class);
    }

    private void invalidateActionCache(Interval interval) {
        if (interval != null) {
            invalidateCache("/service/intervals/" + interval.getId() + "/actions");
        }
    }

    public List<ServiceAction> getServiceActions(long intervalId) {
        String result = getCachedResult("/service/intervals/" + intervalId + "/actions");
        return ApiMapper.list(result, ServiceAction.class);
    }

    public ServiceAction addServiceAction(Interval interval, ServiceActionData data) {
        String path = "/service/intervals/" + interval.getId() + "/actions";
        String json = ApiMapper.toJson(data);
        String result = getHttpService().postJson(path, json);
        invalidateActionCache(interval);
        return ApiMapper.map(result, ServiceAction.class);
    }

    public void deleteServiceAction(Interval interval, long actionId) {
        getHttpService().delete("/service/intervals/" + interval.getId() + "/actions/" + actionId);
        invalidateActionCache(interval);
    }

    private void invalidateKitCache(Interval interval) {
        if (interval != null) {
            invalidateCache("/service/intervals/" + interval.getId() + "/kits");
        }
    }

    public List<ServiceKit> getServiceKits(long intervalId) {
        String result = getCachedResult("/service/intervals/" + intervalId + "/kits");
        return ApiMapper.list(result, ServiceKit.class);
    }

    public ServiceKit addServiceKit(Interval interval, ServiceKitData data) {
        String path = "/service/intervals/" + interval.getId() + "/kits";
        String json = ApiMapper.toJson(data);
        String result = getHttpService().postJson(path, json);
        invalidateKitCache(interval);
        return ApiMapper.map(result, ServiceKit.class);
    }

    public void deleteServiceKit(Interval interval, long kitId) {
        getHttpService().delete("/service/intervals/" + interval.getId() + "/kits/" + kitId);
        invalidateKitCache(interval);
    }

    public List<BusinessUnit> getBusinessUnits() {
        String result = getCachedResult("/business_units");
        return ApiMapper.list(result, BusinessUnit.class);
    }
}
