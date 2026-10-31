package com.tbscg.doosan.magnolia.dxp.core.util;

import com.google.common.base.Preconditions;
import com.tbscg.doosan.magnolia.dxp.core.i18n.LocalizedProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import javax.jcr.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.jackrabbit.ocm.repository.NodeUtil.PATH_SEPARATOR;

/**
 * Utility class for facilitating operations with JCR properties.
 *
 * @author apuigescobar
 */
@Slf4j
public class PropertyUtil {
    /**
     * Get a list of properties from the node provided as parameter.
     *
     * @param node provided {@link Node}.
     * @return Collection of properties {@link Collection} {@link javax.jcr.Property}.
     * @throws RepositoryException in case of exception accessing the JCR repository.
     */
    public static Collection<Property> getProperties(Node node) throws RepositoryException {

        Collection<Property> propertyList = new ArrayList<>();

        PropertyIterator propertyIterator = node.getProperties();

        while (propertyIterator.hasNext()) {
            propertyList.add(propertyIterator.nextProperty());
        }

        return propertyList;
    }

    public static void setProperty(Node node, String name, String value) {
        try {
            node.setProperty(name, value);
        } catch (RepositoryException e) {
            log.warn("Cannot set property {} with value {} on node {}", name, value, NodeUtil.getPath(node));
        }
    }

    /**
     * Get a list of i18n node properties given an i18n property name and pattern.
     *
     * @param node provided {@link Node}.
     * @param i18nPattern i18n pattern to match {@link Pattern}.
     * @return Collection of properties {@link Collection} {@link javax.jcr.Property}.
     * @throws RepositoryException in case of exception accessing the JCR repository.
     */
    public static Collection<Property> getI18nProperties(Node node, String propertyName, Pattern i18nPattern)
            throws RepositoryException {

        Collection<Property> propertyList = new ArrayList<>();

        PropertyIterator propertyIterator = node.getProperties();

        while (propertyIterator.hasNext()) {
            Property itProperty = propertyIterator.nextProperty();
            String itPropertyName = itProperty.getName();

            if (itPropertyName.equals(propertyName) || i18nPattern.matcher(itPropertyName).matches()) {
                propertyList.add(itProperty);
            }
        }

        return propertyList;
    }

    /**
     * Exceptionless way of retrieving property name.
     *
     * @param property property to get name from
     * @return property name string
     */
    public static String getName(Property property) {
        try {
            return property.getName();
        } catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }

    /**
     * Exceptionless way of retrieving property path.
     *
     * @param property property to get path from
     * @return property path string
     */
    public static String getPath(Property property) {
        try {
            return property.getPath();
        } catch (Exception e) {
            return PATH_SEPARATOR;
        }
    }

    /**
     * Gets and validates a JCR string property.
     *
     * @param node node to get property from.
     * @param propertyName property name.
     * @param errorMessage error message.
     * @return checked string property.
     * @throws IllegalArgumentException if the property is blank.
     */
    public static String getCheckedString(Node node, String propertyName, String errorMessage) {
        String property = info.magnolia.jcr.util.PropertyUtil.getString(node, propertyName);
        DxpPreconditions.checkNotBlank(property, errorMessage);
        return property;
    }

    /**
     * Gets and validates a JCR date property.
     *
     * @param node node to get property from.
     * @param propertyName property name.
     * @param errorMessage error message.
     * @return checked date property.
     * @throws NullPointerException if the property is null.
     */
    public static Date getCheckedDate(Node node, String propertyName, String errorMessage) {
        Calendar property = info.magnolia.jcr.util.PropertyUtil.getDate(node, propertyName);
        Preconditions.checkNotNull(property, errorMessage);
        return property.getTime();
    }

    /**
     * Creates localized property from JCR String property based on provided locales.
     * @param node node to get property from.
     * @param propertyName property name without locale suffix.
     * @param locale locale for property.
     * @param defaultLocale default locale for property.
     * @param errorMessage error message
     * @return LocalizedProperty
     */
    public static LocalizedProperty getLocalizedProperty(Node node, String propertyName, Locale locale, Locale defaultLocale, String errorMessage) {
        boolean isDefaultLanguage = locale.getLanguage().equals(defaultLocale.getLanguage());
        String propName = isDefaultLanguage ? propertyName : propertyName + "_" + locale.getLanguage();
        String localizedValue;
        try {
            localizedValue = PropertyUtil.getCheckedString(node, propName, errorMessage);
        } catch(IllegalArgumentException ex) {
            if (!isDefaultLanguage) {
                localizedValue = PropertyUtil.getCheckedString(node, propertyName, errorMessage);
            } else {
                throw ex;
            }
        }
        return new LocalizedProperty(locale.getLanguage(), localizedValue);
    }

    /**
     * Extract locales and their corresponding values to Map<String,String>.
     * @param node node to get property from.
     * @param propertyName property name without locale suffix.
     * @param locales list of locales.
     * @param defaultLocale default locale for property.
     * @param errorMessage error message
     * @return Map<String, String>
     */
    public static Map<String, String> extractLocalizedProperties(Node node, String propertyName, List<Locale> locales, Locale defaultLocale, String errorMessage) {
        return locales.stream()
                .map(locale -> PropertyUtil.getLocalizedProperty(node, propertyName, locale, defaultLocale, errorMessage))
                .filter(localizedProperty -> StringUtils.isNotEmpty(localizedProperty.getValue()))
                .collect(Collectors.toMap(LocalizedProperty::getLocale, LocalizedProperty::getValue));
    }

    /**
     * Collects properties from property iterator into list. If the iterator is null the result is empty.
     *
     * @param propertyIterator input iterator
     * @return list of properties
     */
    public static List<Property> asList(final PropertyIterator propertyIterator) {
        if (propertyIterator == null) {
            return Collections.emptyList();
        }

        final List<Property> properties = new ArrayList<>();
        while (propertyIterator.hasNext()) {
            properties.add(propertyIterator.nextProperty());
        }
        return properties;
    }

    public static Optional<Boolean> getBooleanValue(Property property) {
        return Optional.ofNullable(property).map(prop -> {
            try {
                return prop.getBoolean();
            } catch (RepositoryException e) {
                log.error("Failed to get boolean value.", e);
                return null;
            }
        });
    }

    public static Optional<String> getStringValue(Property property) {
        return Optional.ofNullable(property).map(prop -> {
            try {
                return prop.getString();
            } catch (RepositoryException e) {
                log.error("Failed to get string value.", e);
                return null;
            }
        });
    }

    public static List<String> getStringValues(Property property) {
        List<String> stringValues = new ArrayList<>();
        Optional.ofNullable(property).ifPresent(prop -> {
            try {
                for (Value value : prop.getValues()) {
                    stringValues.add(value.getString());
                }
            } catch (RepositoryException e) {
                log.error("Failed to get string values.", e);
            }
        });
        return stringValues;
    }

    public static Optional<Property> tryGetProperty(Node node, final String path) {
        try {
            return Optional.of(node.getProperty(path));
        } catch (RepositoryException e) {
            return Optional.empty();
        }
    }

    public static Boolean tryGetBooleanValue(Node node, final String path) {
        return tryGetBooleanValue(node, path, false);
    }

    public static Boolean tryGetBooleanValue(Node node, final String path, final boolean defaultValue) {
        return tryGetProperty(node, path).flatMap(PropertyUtil::getBooleanValue).orElse(defaultValue);
    }

    public static String tryGetStringValue(Node node, final String path) {
        return tryGetStringValue(node, path, "");
    }

    public static String tryGetStringValue(Node node, final String path, final String defaultValue) {
        return tryGetProperty(node, path).flatMap(PropertyUtil::getStringValue).orElse(defaultValue);
    }
}
