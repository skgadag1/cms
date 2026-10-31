package com.tbscg.doosan.magnolia.localizations.converter;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tbscg.doosan.magnolia.localizations.model.LanguageGroupsPair;
import com.tbscg.doosan.magnolia.localizations.model.NonRegionalizedLocalizationsPackage;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

/**
 * Specialized serializer strategy for {@link NonRegionalizedLocalizationsPackage}.
 *
 * @author apuigescobar
 */
public class NonRegionalizedLocalizationsPackageJsonSerializerStrategy extends AbstractLocalizationsPackageJsonSerializerStrategy {

    protected static JsonElement serializeLocalizationPackage(NonRegionalizedLocalizationsPackage localizationsPackage) {
        final String appName = localizationsPackage.getAppName();
        Preconditions.checkArgument(StringUtils.isNotBlank(appName), "App name can't be blank");

        final JsonObject localizationsPackageJson = new JsonObject();
        localizationsPackageJson.addProperty(PROPERTY_APP_NAME, appName);

        final JsonObject localizationsJson = new JsonObject();

        final Set<LanguageGroupsPair> languageGroupsPairs = localizationsPackage.getLanguageGroupsPairs();
        Preconditions.checkNotNull(languageGroupsPairs, "Language - groups pair can't be null");

        for (final LanguageGroupsPair languageGroupsPair : languageGroupsPairs) {
            localizationsJson.add(languageGroupsPair.getLanguage(), serializeLocalizationsGroupsPairs(languageGroupsPair));
        }
        localizationsPackageJson.add(PROPERTY_LOCALIZATIONS, localizationsJson);

        return localizationsPackageJson;
    }
}
