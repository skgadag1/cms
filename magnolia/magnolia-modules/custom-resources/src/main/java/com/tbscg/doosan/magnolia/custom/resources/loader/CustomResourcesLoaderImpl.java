package com.tbscg.doosan.magnolia.custom.resources.loader;

import com.tbscg.doosan.magnolia.custom.resources.vaadin.CssResourcesComponent;
import com.tbscg.doosan.magnolia.custom.resources.vaadin.JsResourcesComponent;
import com.vaadin.ui.UI;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Singleton
public class CustomResourcesLoaderImpl implements CustomResourcesLoader {
    private final CssResourcesComponent cssComponent;
    private final JsResourcesComponent jsComponent;

    // CSS constants
    private static final String PRODUCT_DELETION_ALERT = """
            .product-deletion, div.v-window.v-widget.alert.v-window-alert.warning.v-window-warning.light.v-window-light.product-deletion.v-window-product-deletion > div > div > div.v-window-contents {
             max-width: 1000px;
            }
            """;

    private static final String MOVE_ITEM_DIALOG = """
            div.v-window.v-widget.dialog.v-window-dialog.choose-dialog.v-window-choose-dialog.medium.v-window-medium {
             height: auto !important;
            }
            """;

    private static final String SIDE_NAV_GALLERY_SELECT_ASSET = """
            .sng-asset-image div.v-slot.v-slot-magnoliabutton.v-slot-select-new-button > button {
             white-space: nowrap;
            }""";

    private static final String POPUP_HELP_TEXT = """
            .v-contexthelp-bubble div.popupContent {
            word-break: break-word;
            }""";

    // JS paths
    private static final List<String> DEFAULT_JS_PATHS = List.of(
            "com/tbscg/doosan/magnolia/custom/resources/vaadin/cloudinary-media-library.js"
    );

    @Inject
    public CustomResourcesLoaderImpl(CssResourcesComponent cssComponent,
                                     JsResourcesComponent jsComponent) {
        this.cssComponent = cssComponent;
        this.jsComponent = jsComponent;
        this.initialize();
    }

    public void initialize() {
        log.info("Initializing custom resources components");
        this.cssComponent.extend(UI.getCurrent());
        log.info("Custom CSS component initialized");
        this.jsComponent.extend(UI.getCurrent());
        log.info("Custom JS component initialized");
        this.load();
    }

    @Override
    public void load() {
        loadCss();
        loadJs();
    }

    private void loadCss() {
        String contentToLoad = PRODUCT_DELETION_ALERT + MOVE_ITEM_DIALOG + SIDE_NAV_GALLERY_SELECT_ASSET + POPUP_HELP_TEXT;
        this.cssComponent.getState().setContent(contentToLoad);
        log.info("Custom CSS loaded");
    }

    private void loadJs() {
        StringBuilder combinedContent = new StringBuilder();

        for (String path : DEFAULT_JS_PATHS) {
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
                if (inputStream != null) {
                    String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                    combinedContent.append(content).append(StringUtils.LF);
                    log.info("JS resource loaded from path: {}", path);
                } else {
                    log.warn("JS resource not found at path: {}", path);
                }
            } catch (IOException e) {
                log.error("Failed to read JS file at path: {}", path, e);
            }
        }

        if (!combinedContent.isEmpty()) {
            this.jsComponent.getState().setContent(combinedContent.toString());
        }
    }
}