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
public class NonRegionalizedLocalizationsPackage extends LocalizationsPackage {
    @Getter
    private final Set<LanguageGroupsPair> languageGroupsPairs;

    public NonRegionalizedLocalizationsPackage(String appName, Set<LanguageGroupsPair> languageGroupsPairs) {
        super(appName);
        this.languageGroupsPairs = languageGroupsPairs;
    }
}
