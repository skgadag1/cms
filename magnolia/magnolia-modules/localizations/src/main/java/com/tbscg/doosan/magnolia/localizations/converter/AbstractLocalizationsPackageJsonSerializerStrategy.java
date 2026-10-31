package com.tbscg.doosan.magnolia.localizations.converter;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.tbscg.doosan.magnolia.localizations.model.LanguageGroupsPair;
import com.tbscg.doosan.magnolia.localizations.model.Localization;
import com.tbscg.doosan.magnolia.localizations.model.LocalizationsGroupPair;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

/**
 * Abstract {@link com.tbscg.doosan.magnolia.localizations.model.LocalizationsPackage} JSON serializer strategy.
 *
 * @author apuigescobar
 */
public abstract class AbstractLocalizationsPackageJsonSerializerStrategy {

    protected static final String PROPERTY_APP_NAME = "appName";
    protected static final String PROPERTY_LOCALIZATIONS = "localizations";

    protected static JsonObject serializeLocalizationsGroupsPairs(LanguageGroupsPair languageGroupsPair) {
        final Set<LocalizationsGroupPair> localizationsGroupPairs = languageGroupsPair.getLocalizationsGroupPairs();
        Preconditions.checkNotNull(localizationsGroupPairs, "Localizations groups can't be null");
        Preconditions.checkArgument(!localizationsGroupPairs.isEmpty(), "Localizations groups can't be empty");
        final JsonObject languageGroupsPairJson = new JsonObject();

        for (final LocalizationsGroupPair localizationsGroupPair : localizationsGroupPairs) {
            languageGroupsPairJson.add(localizationsGroupPair.getGroupName(), serializeLocalizationsGroup(localizationsGroupPair));
        }
        return languageGroupsPairJson;
    }

    protected static JsonObject serializeLocalizationsGroup(LocalizationsGroupPair localizationsGroupPair) {
        final String groupName = localizationsGroupPair.getGroupName();
        Preconditions.checkArgument(StringUtils.isNotBlank(groupName), "Localizations group name can't be blank");

        final Set<Localization> localizations = localizationsGroupPair.getLocalizations();
        Preconditions.checkNotNull(localizations, "Localizations can't be null");

        final JsonObject localizationsGroupJson = new JsonObject();

        for (final Localization localization : localizations) {
            final String key = localization.getKey();
            final String value = localization.getValue();
            Preconditions.checkArgument(StringUtils.isNotBlank(key), "Localization key can't be blank");
            Preconditions.checkNotNull(value, "Localization value can't be null");
            localizationsGroupJson.addProperty(key, value);
        }
        return localizationsGroupJson;
    }
}
