package com.tbscg.doosan.magnolia.sso.util;

import com.github.scribejava.core.model.*;
import com.google.gson.JsonObject;
import com.tbscg.doosan.magnolia.sso.service.KeyAccountService;
import info.magnolia.connector.sso.oic.settings.OICSettings;
import info.magnolia.connector.sso.util.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import java.io.IOException;
import java.util.*;

/**
 * Customization of {@link info.magnolia.connector.sso.util.UserAccountUtils} that uses "en" as default locale.
 */
@Slf4j
public class UserAccountUtils {

    public static final String COMPANY_CODE_FIELD = "company_code";
    public static final String COMPANY_ID_FIELD = "companyId";
    private static final String ID_FIELD = "id";
    private static final String EMAIL_FIELD = "email";
    private static final String NAME_FIELD = "name";
    private static final String USER_NAME_FIELD = "user_name";
    private static final String FULL_NAME_FIELD = "fullName";
    private static final String GIVEN_NAME_FIELD = "given_name";
    private static final String FAMILY_NAME_FIELD = "family_name";
    private static final String LANGUAGE_FIELD = "language";
    private static final String USER_ATTRIBUTES_FIELD = "user_attributes";
    private static final String INTERNAL_FIELD = "internal";
    private static final String EXTERNAL_GROUPS_PREFIX = "EXT_GROUPS_";
    private static final String EXPERT_LEVEL_FIELD = "expertLevel";
    private static final String EMPLOYEE_NUMBER_FIELD = "employeeNumber";
    private static final String DEFAULT_LOCALE = Locale.ENGLISH.toLanguageTag();

    /**
     * Fill a list with user properties retrieved as JSON.
     *
     * @param oauthResponse                 The response from the server containing the requested information in JSON format.
     * @param serviceName                   The name of the service used.
     * @param token                         Authorization token to access Key Account data endpoint
     * @param keyAccountDataEndpointURL     Key Account data endpoint URL
     * @return List with retrieved user properties to be used when creating the external user object.
     */
    public static Map<String, String> createUserProperties(Response oauthResponse, OICSettings requestSettings, String serviceName, Token token, String keyAccountDataEndpointURL) {
        String array = StringUtils.defaultString(requestSettings.getArrayName(), "");

        try {
            JsonObject jsonObject = JSONUtils.getJsonObject(oauthResponse.getBody(), oauthResponse.getHeader("Content-Type"), array);
            return fillUserProperties(serviceName, jsonObject, requestSettings.getFieldMappings(),
                    requestSettings.isExternalGroupsManagement(), requestSettings.getExternalGroupsPropertyName(),
                    token, keyAccountDataEndpointURL);

        } catch (IOException e) {
            log.error("Could not create a JSON object from the response body.", e);
        }
        return null;
    }

    /**
     * Create a list with user properties provided by a JSON response to enable creation of an external user object.
     *
     * @param serviceName   Name of the OAuth authentication service used.
     * @param jsonObject    JSON result containing user properties retrieved by an OAuth protected resource.
     * @param fieldMappings Mapping of field names used in Magnolia to the ones used by a specific service.
     * @param token Authorization token to access Key Account data endpoint
     * @param keyAccountDataEndpointURL Key Account data endpoint URL
     * @return List with user properties.
     */
    private static Map<String, String> fillUserProperties(String serviceName,
                                                          JsonObject jsonObject,
                                                          Map<String, String> fieldMappings,
                                                          boolean externalGroups,
                                                          String externalGroupsName,
                                                          Token token,
                                                          String keyAccountDataEndpointURL) {

        Map<String, String> userProperties = new HashMap<>();
        // default field names
        String idField = StringUtils.defaultString(fieldMappings.get(ID_FIELD), ID_FIELD);
        String emailField = StringUtils.defaultString(fieldMappings.get(EMAIL_FIELD), EMAIL_FIELD);
        String nameField = StringUtils.defaultString(fieldMappings.get(NAME_FIELD), NAME_FIELD);
        String usernameField = StringUtils.defaultString(fieldMappings.get(USER_NAME_FIELD), USER_NAME_FIELD);
        String givenNameField = StringUtils.defaultString(fieldMappings.get(GIVEN_NAME_FIELD), GIVEN_NAME_FIELD);
        String familyNameField = StringUtils.defaultString(fieldMappings.get(FAMILY_NAME_FIELD), FAMILY_NAME_FIELD);
        String userAttributesField = StringUtils.defaultString(fieldMappings.get(USER_ATTRIBUTES_FIELD), USER_ATTRIBUTES_FIELD);
        String employeeNumberField = StringUtils.defaultString(fieldMappings.get(EMPLOYEE_NUMBER_FIELD), EMPLOYEE_NUMBER_FIELD);

        // -- EXPERT LEVEL
        String username = JSONUtils.getFieldValue(jsonObject, usernameField);
        String level = UserHelper.getExpertLevelByUsername(username);
        userProperties.put(EXPERT_LEVEL_FIELD, level);

        // -- ID
        String id = JSONUtils.getFieldValue(jsonObject, idField);
        userProperties.put(ID_FIELD, createUserId(id, serviceName));

        // -- USERNAME
        String email = JSONUtils.getFieldValue(jsonObject, emailField);
        String name = JSONUtils.getFieldValue(jsonObject, nameField);
        if (StringUtils.isNotBlank(name)) {
            userProperties.put(NAME_FIELD, name);
        } else if (StringUtils.isNotBlank(email)) {
            userProperties.put(NAME_FIELD, StringUtils.substringBefore(email, "@"));
        } else {
            // uh oh, no nickname, no email and no fullname... we can only have the ugly identifier
            userProperties.put(NAME_FIELD, id);
        }
        // -- FULL NAME
        String givenName = JSONUtils.getFieldValue(jsonObject, givenNameField);
        String familyName = JSONUtils.getFieldValue(jsonObject, familyNameField);
        if (StringUtils.isNotBlank(name)) {
            userProperties.put(FULL_NAME_FIELD, name);
        } else if (StringUtils.isNotBlank(givenName) && StringUtils.isNotBlank(familyName)) {
            userProperties.put(FULL_NAME_FIELD, givenName + " " + familyName);
        }
        // -- EMAIL
        if (StringUtils.isNotBlank(email)) {
            userProperties.put(EMAIL_FIELD, email);
        }

        // -- EMPLOYEE NUMBER
        if (StringUtils.isNotBlank(employeeNumberField)) {
            userProperties.put(EMPLOYEE_NUMBER_FIELD, employeeNumberField);
        }

        // -- SmartEquip company id
        JsonObject userAttributes = jsonObject.getAsJsonObject(userAttributesField);
        if (userAttributes != null && userAttributes.has(COMPANY_CODE_FIELD)) {
            String companyCode = JSONUtils.getFieldValue(userAttributes, COMPANY_CODE_FIELD);
            String companyId = UserHelper.getCompanyIdByCompanyCode(companyCode);
            userProperties.put(COMPANY_ID_FIELD, companyId);
        } else if (StringUtils.isNotBlank(email)) {
            if(StringUtils.isNotBlank(keyAccountDataEndpointURL)) {
                Map<String, String> data = KeyAccountService.fetchKeyAccountAndSabaOrgData(keyAccountDataEndpointURL, token, email);
                if(data != null) {
                    if(data.containsKey(COMPANY_ID_FIELD)) {
                        userProperties.put(COMPANY_ID_FIELD, data.get(COMPANY_ID_FIELD));
                    }
                    if(data.containsKey(COMPANY_CODE_FIELD)) {
                        userProperties.put(COMPANY_CODE_FIELD, data.get(COMPANY_CODE_FIELD));
                    }
                } else {
                    log.error("Error while setting up company data for user.");
                }
            } else if (log.isDebugEnabled()) {
                log.debug("Not kags-key-account-okta service so key account endpoint will not be called");
            }
        }

        try {
            userProperties.put(EMPLOYEE_NUMBER_FIELD, UserHelper.getEmployeeNumber());
        } catch (Exception e) {
            log.warn(e.getMessage());
        }

        try {
            userProperties.put(INTERNAL_FIELD, String.valueOf(UserHelper.isInternal(serviceName)));
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        userProperties.put(LANGUAGE_FIELD, DEFAULT_LOCALE);
        // if external group management should be used
        if (externalGroups) {
            if (log.isDebugEnabled()) {
                log.debug("Using external groups with group name property: {}", externalGroupsName);
            }
            // inject the property name where external groups are stored in the list of field names
            fieldMappings.put(externalGroupsName, externalGroupsName);
        }

        // add additional optional fields defined in service configuration
        for (String field : fieldMappings.keySet()) {
            String fieldValue = JSONUtils.getFieldValue(jsonObject, StringUtils.defaultString(fieldMappings.get(field), field));
            if (StringUtils.isNotBlank(fieldValue) && !userProperties.containsKey(field) && !userProperties.containsKey(EXTERNAL_GROUPS_PREFIX + field)) {

                if (externalGroups && StringUtils.equals(field, externalGroupsName)) {
                    field = EXTERNAL_GROUPS_PREFIX + field;
                }
                userProperties.put(field, fieldValue);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Created user properties: {}", userProperties);
        }

        return userProperties;
    }

    private static String createUserId(String idValue, String serviceName) {
        if (StringUtils.isNotBlank(idValue)) {
            return serviceName + ":" + "oauth:" + idValue;
        } else {
            return serviceName + ":" + ":oauth:generated:" + new Random().nextInt(999_999);
        }
    }

}
