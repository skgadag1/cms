package com.tbscg.doosan.magnolia.localizations.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Set;

/**
 * @author apuigescobar
 */
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RegionLanguagesPair {
    @Getter
    private final String regionName;
    @Getter
    private final Set<LanguageGroupsPair> languageGroupsPairs;
}
