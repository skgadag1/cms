package com.tbscg.doosan.magnolia.localizations.converter;


import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tbscg.doosan.magnolia.localizations.model.LocalizationsPackage;
import com.tbscg.doosan.magnolia.localizations.model.NonRegionalizedLocalizationsPackage;
import com.tbscg.doosan.magnolia.localizations.model.RegionalizedLocalizationsPackage;
import software.amazon.awssdk.services.cloudfront.model.InvalidArgumentException;

import javax.inject.Singleton;
import java.lang.reflect.Type;

/**
 * Specialized serializer for {@link LocalizationsPackage}.
 *
 * @author apuigescobar
 */
@Singleton
public class LocalizationsPackageJsonSerializer implements JsonSerializer<LocalizationsPackage> {

    @Override
    public JsonElement serialize(LocalizationsPackage localizationsPackage, Type typeOfSrc, JsonSerializationContext context) {
        Preconditions.checkNotNull(localizationsPackage, "Localizations package can't be null!");
        if (localizationsPackage instanceof NonRegionalizedLocalizationsPackage) {
            return NonRegionalizedLocalizationsPackageJsonSerializerStrategy.serializeLocalizationPackage((NonRegionalizedLocalizationsPackage) localizationsPackage);
        } else if (localizationsPackage instanceof RegionalizedLocalizationsPackage) {
            return RegionalizedLocalizationsPackageJsonSerializerStrategy.serializeLocalizationPackage((RegionalizedLocalizationsPackage) localizationsPackage);
        }
        throw InvalidArgumentException.builder().message(String.format("There is no applicable serializer strategy for %s's localization package", localizationsPackage.getAppName())).build();
    }
}
