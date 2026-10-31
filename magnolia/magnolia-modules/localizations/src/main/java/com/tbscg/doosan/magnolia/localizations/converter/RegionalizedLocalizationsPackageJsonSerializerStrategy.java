package com.tbscg.doosan.magnolia.localizations.converter;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tbscg.doosan.magnolia.localizations.model.LanguageGroupsPair;
import com.tbscg.doosan.magnolia.localizations.model.RegionLanguagesPair;
import com.tbscg.doosan.magnolia.localizations.model.RegionalizedLocalizationsPackage;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Set;

/**
 * Specialized serializer strategy for {@link RegionalizedLocalizationsPackage}.
 *
 * @author apuigescobar
 */
public class RegionalizedLocalizationsPackageJsonSerializerStrategy extends AbstractLocalizationsPackageJsonSerializerStrategy {

    private static final String PROPERTY_IS_REGIONALIZED = "isRegionalized";

    protected static JsonElement serializeLocalizationPackage(RegionalizedLocalizationsPackage localizationsPackage) {
        final String appName = localizationsPackage.getAppName();
        Preconditions.checkArgument(StringUtils.isNotBlank(appName), "App name can't be blank");

        final JsonObject localizationsPackageJson = new JsonObject();
        localizationsPackageJson.addProperty(PROPERTY_APP_NAME, appName);

        final JsonObject localizationsJson = new JsonObject();

        final Collection<RegionLanguagesPair> regionLanguagesPairs = localizationsPackage.getRegionLanguagesPairs();
        Preconditions.checkNotNull(regionLanguagesPairs, "Language - group pair can't be null");

        for (final RegionLanguagesPair regionLanguagesPair : regionLanguagesPairs) {
            localizationsJson.add(regionLanguagesPair.getRegionName(), serializeRegionLanguagesPair(regionLanguagesPair));
        }
        localizationsPackageJson.add(PROPERTY_LOCALIZATIONS, localizationsJson);
        localizationsPackageJson.addProperty(PROPERTY_IS_REGIONALIZED, true);
        return localizationsPackageJson;
    }

    private static JsonElement serializeRegionLanguagesPair(RegionLanguagesPair regionLanguagesPair) {
        final Set<LanguageGroupsPair> languageGroupsPairs = regionLanguagesPair.getLanguageGroupsPairs();
        Preconditions.checkNotNull(languageGroupsPairs, "Language - groups pair can't be null");
        Preconditions.checkArgument(!languageGroupsPairs.isEmpty(), "Language - groups pair can't be empty");
        final JsonObject regionLanguagesPairJson = new JsonObject();

        for (final LanguageGroupsPair languageGroupsPair : languageGroupsPairs) {
            Preconditions.checkArgument(StringUtils.isNotBlank(languageGroupsPair.getLanguage()), "Language can't be blank");
            regionLanguagesPairJson.add(languageGroupsPair.getLanguage(), serializeLocalizationsGroupsPairs(languageGroupsPair));
        }

        return regionLanguagesPairJson;
    }
}
