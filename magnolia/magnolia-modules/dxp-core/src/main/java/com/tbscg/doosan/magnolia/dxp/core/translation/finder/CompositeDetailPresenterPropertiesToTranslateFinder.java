package com.tbscg.doosan.magnolia.dxp.core.translation.finder;

import info.magnolia.context.Context;
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.translation.TranslationModule;
import info.magnolia.translation.finder.AbstractPropertiesToTranslateFinder;
import info.magnolia.translation.finder.PropertyToTranslate;
import info.magnolia.ui.api.app.registry.AppDescriptorRegistry;
import info.magnolia.ui.dialog.DialogDefinitionRegistry;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.jcr.Node;
import java.util.ArrayList;
import java.util.List;

public class CompositeDetailPresenterPropertiesToTranslateFinder extends AbstractPropertiesToTranslateFinder {

    private final TranslationModule module;
    private final I18nizer i18nizer;
    private final AppDescriptorRegistry appRegistry;
    private final DialogDefinitionRegistry dialogDefinitionRegistry;
    private final String formPath;
    private final Context context;

    @Inject
    public CompositeDetailPresenterPropertiesToTranslateFinder(
            TranslationModule module,
            I18nizer i18nizer,
            AppDescriptorRegistry appRegistry,
            DialogDefinitionRegistry dialogDefinitionRegistry,
            String formPath,
            Context context) {

        super(module, i18nizer);
        this.module = module;
        this.i18nizer = i18nizer;
        this.appRegistry = appRegistry;
        this.dialogDefinitionRegistry = dialogDefinitionRegistry;
        this.formPath = formPath;
        this.context = context;
    }

    /**
     * Extracts list of dialog fields from all dialog forms specified in path variable which are supported by
     * translation module. There can be the same properties in multiple dialogs and in order to do not export
     * the same properties multiple times for the same node, duplicates have to be removed.
     *
     * @return list of unique dialog form properties.
     */
    @Override
    public List<PropertyToTranslate> getPropertiesToTranslate(Node node) {
        List<PropertyToTranslate> list = new ArrayList<>();
        for (String path : getAllFormsPaths()) {
            list.addAll(getPropertiesToTranslateForPath(node, path));
        }
        return removeDuplicates(list);
    }

    private List<PropertyToTranslate> removeDuplicates(List<PropertyToTranslate> list) {
        List<String> seen = new ArrayList<>();
        List<PropertyToTranslate> tmp = new ArrayList<>();
        list.stream().forEachOrdered(p -> {
            if(!seen.contains(p.getPropertyName())) {
                tmp.add(p);
                seen.add(p.getPropertyName());
            }
        });
        return tmp;
    }

    private String[] getAllFormsPaths() {
        if (StringUtils.isNotBlank(this.formPath)) {
            return this.formPath.split("\\|");
        }
        return new String[0];
    }

    private List<PropertyToTranslate> getPropertiesToTranslateForPath(Node node, String path) {
        CustomDetailPresenterPropertiesToTranslateFinder customFinder = new CustomDetailPresenterPropertiesToTranslateFinder (
                module, i18nizer, appRegistry, path, context, dialogDefinitionRegistry);
        return customFinder.getPropertiesToTranslate(node);
    }
}