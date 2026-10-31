package com.tbscg.doosan.magnolia.bobcat.templating;

import com.tbscg.doosan.magnolia.bobcat.BobcatModule;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.WebContext;
import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.templating.functions.TemplatingFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class BobcatTemplatingFunctions {

    private static final String REQUEST_SCRIPTS_ATTRIBUTE_KEY = "request.scripts.list";
    public static final String NAME = "bobcatfn";

    private MagnoliaConfigurationProperties properties;
    private final BobcatModule bobcatModule;
    private final Provider<WebContext> webContextProvider;
    private final Provider<I18nContentSupport> i18nContentSupport;
    private final TemplatingFunctions cmsfn;

    @Inject
    public BobcatTemplatingFunctions(
            MagnoliaConfigurationProperties properties,
            BobcatModule bobcatModule,
            Provider<WebContext> webContextProvider,
            Provider<I18nContentSupport> i18nContentSupport,
            TemplatingFunctions cmsfn) {
        this.properties = properties;
        this.bobcatModule = bobcatModule;
        this.webContextProvider = webContextProvider;
        this.i18nContentSupport = i18nContentSupport;
        this.cmsfn = cmsfn;
    }

    public String get(String key) {
        return getProperty(key);
    }

    public String getProperty(String key) {
        return this.properties.getProperty(key);
    }

    public BobcatConfig getConfig() {
        return new BobcatConfig();
    }

    public class BobcatConfig {

        public String getStaticServer() {
            return getProperty("magnolia.bobcat.static.server");
        }

        public String getContentApiServer() {
            return getProperty("magnolia.bobcat.content.api.server");
        }

        public String getApiServersJSON() {
            JsonObject json = Json.createObjectBuilder()
                    .add("content-api", getContentApiServer())
                    .build();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            JsonWriter writer = Json.createWriter(out);
            writer.writeObject(json);
            writer.close();
            return out.toString();
        }
    }

    public void addRequestScript(String script) {
        HttpServletRequest req = this.webContextProvider.get().getRequest();
        List<String> scripts = (List<String>) req.getAttribute(REQUEST_SCRIPTS_ATTRIBUTE_KEY);
        if (scripts == null) {
            scripts = new ArrayList<>();
            req.setAttribute(REQUEST_SCRIPTS_ATTRIBUTE_KEY, scripts);
        }
        if (!scripts.contains(script)) {
            scripts.add(script);
        }
    }

    public List<String> getRequestScripts() {
        HttpServletRequest req = this.webContextProvider.get().getRequest();
        List<String> scripts = (List<String>) req.getAttribute(REQUEST_SCRIPTS_ATTRIBUTE_KEY);
        if (scripts != null) {
            return Collections.unmodifiableList(scripts);
        }
        return Collections.emptyList();
    }

    public Locale getLocale() {
        return webContextProvider.get().getAggregationState().getLocale();
    }
}
