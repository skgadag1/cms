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
public class LocalizationsGroupPair {
    @Getter
    private final String groupName;
    @Getter
    private final Set<Localization> localizations;
}
