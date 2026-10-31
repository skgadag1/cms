package com.tbscg.doosan.magnolia.localizations.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Set;

/**
 * @author apuigescobar
 */
@ToString
@EqualsAndHashCode(callSuper = true)
public class RegionalizedLocalizationsPackage extends LocalizationsPackage {
    @Getter
    private final Set<RegionLanguagesPair> regionLanguagesPairs;

    public RegionalizedLocalizationsPackage(String appName, Set<RegionLanguagesPair> regionLanguagesPairs) {
        super(appName);
        this.regionLanguagesPairs = regionLanguagesPairs;
    }
}
