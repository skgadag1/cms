package com.tbscg.doosan.magnolia.localizations.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * @author apuigescobar
 */
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public abstract class LocalizationsPackage {
    @Getter
    private final String appName;
}
