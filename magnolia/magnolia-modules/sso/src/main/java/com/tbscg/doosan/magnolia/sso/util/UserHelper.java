package com.tbscg.doosan.magnolia.sso.util;

import static com.tbscg.doosan.magnolia.sso.extractors.CustomAccessTokenExtractor.ACCESS_TOKEN;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tbscg.doosan.magnolia.sso.api.PassportApi;
import com.tbscg.doosan.magnolia.sso.model.OrgDivision;
import com.tbscg.doosan.magnolia.sso.model.PassportOrganization;
import com.tbscg.doosan.magnolia.sso.service.UserDBService;
import info.magnolia.connector.sso.util.JSONUtils;
import info.magnolia.context.MgnlContext;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import info.magnolia.objectfactory.Components;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument;

@Slf4j
public class UserHelper {

    private static final String USER_TYPE_SCOPE_DEALER = "dealer";
    private static final String USER_TYPE_SCOPE_OWNER = "owner";
    private static final String USER_TYPE_SCOPE_NATIONAL = "national";
    private static final String FULL_ACCESS_ROLE_DEALER = "INTERNAL_VIEW_ALL_ORGANIZATIONS";
    private static final String FULL_ACCESS_ROLE_NATIONAL= "INTERNAL_BOBCAT_DIRECT_ALL_ACCESS";
    private static final String OKTA_IDP = "kags-okta";
    private static final String OKTA_IDP_KEY_ACCOUNT = "kags-key-account-okta";
    private static final String TOKEN_SCOPE_FIELD = "scp";
    private static final String TOKEN_EMPLOYEE_NUMBER = "employeeNumber";
    private static final String DB_FIELD_PASSPORT_ID = "passportId";
    private static final String EXPERT_LEVEL_REGULAR = "regular";
    private static final String EXPERT_LEVEL_SUPERTECH = "supertech";

    private static UserDBService userDBService;
    private static PassportApi passportApi;

    private static UserDBService getUserDBService() {
        if (userDBService == null) {
            userDBService = Components.getComponent(UserDBService.class);
            Preconditions.checkArgument(userDBService != null, "UserDBService not initialized.");
        }
        return userDBService;
    }

    private static PassportApi getPassportApi() {
        if (passportApi == null) {
            passportApi = Components.getComponent(PassportApi.class);
            Preconditions.checkArgument(passportApi != null, "PassportApi not initialized.");
        }
        return passportApi;
    }

    public static boolean isInternal(String serviceName) throws Exception {
        if (!serviceName.equals(OKTA_IDP) && !serviceName.equals(OKTA_IDP_KEY_ACCOUNT)) {
            return false;
        }

        if (MgnlContext.hasAttribute(ACCESS_TOKEN)) {
            return hasFullAccessTags(getDecodedToken());
        } else {
            throw new RuntimeException("Access token is not provided. We can't verify if user is internal.");
        }
    }

    private static JsonObject getDecodedToken() {
        final String accessToken = MgnlContext.getAttribute(ACCESS_TOKEN);

        String[] split_string = accessToken.split("\\.");
        String base64EncodedBody = split_string[1]; // We take the body part only
        byte[] bytes = Base64.getUrlDecoder().decode(base64EncodedBody);
        String decodedString = new String(bytes, StandardCharsets.UTF_8);

        return JsonParser.parseString(decodedString).getAsJsonObject();
    }

    private static boolean hasFullAccessTags(JsonObject decodedToken) {
        String result = getUserType(decodedToken.getAsJsonArray(TOKEN_SCOPE_FIELD));
        if (result.equals(USER_TYPE_SCOPE_DEALER)) {
            return hasUserFullAccessTags(JSONUtils.getFieldValue(decodedToken, TOKEN_EMPLOYEE_NUMBER), FULL_ACCESS_ROLE_DEALER);
        } else if (result.equals(USER_TYPE_SCOPE_NATIONAL)) {
            return hasUserFullAccessTags(JSONUtils.getFieldValue(decodedToken, TOKEN_EMPLOYEE_NUMBER), FULL_ACCESS_ROLE_NATIONAL);
        } else {
            return false;
        }
    }

    private static String getUserType(JsonArray scopes) {
        String res = "";
        for (JsonElement scope : scopes) {
            if (scope.getAsString().equals(USER_TYPE_SCOPE_DEALER) ||
                    scope.getAsString().equals(USER_TYPE_SCOPE_OWNER) ||
                    scope.getAsString().equals(USER_TYPE_SCOPE_NATIONAL)) {
                res = scope.getAsString();
            }
        }
        return res;
    }

    private static boolean hasUserFullAccessTags(String employeeNumber, String fullAccessRole) {
        if (StringUtils.isBlank(employeeNumber)) {
            log.warn("Employee number is empty.");
            return false;
        }
        PassportApi passportApi = getPassportApi();
        List<String> webSecurityRoles = passportApi.getIdentity(employeeNumber).getWebSecurityRoles();

        if (Objects.isNull(webSecurityRoles)) {
            log.warn("No web security roles found for employee number: {}", employeeNumber);
            return false;
        }
        List<PassportOrganization> organizations = passportApi.getActiveOrganization(employeeNumber).getOrganizations();

        if (Objects.isNull(organizations)) {
            log.warn("No organizations found for employee number: {}", employeeNumber);
            return false;
        }
        return hasFullAccess(webSecurityRoles, fullAccessRole, organizations);
    }

    private static boolean hasFullAccess(List<String> webSecurityRoles, String fullAccessRole, List<PassportOrganization> organizations) {
        return hasFullAccessRole(webSecurityRoles, fullAccessRole) && containsEmeaOrNaoOrganization(organizations);
    }

    private static boolean hasFullAccessRole(List<String> webSecurityRoles, String fullAccessRole) {
        return webSecurityRoles.contains(fullAccessRole);
    }

    private static boolean containsEmeaOrNaoOrganization(List<PassportOrganization> organizations) {
        return organizations.stream().anyMatch(org -> isCorrectOrgDivision(org.getDivision()));
    }

    private static boolean isCorrectOrgDivision(OrgDivision division) {
        return division.equals(OrgDivision.EMEA) || division.equals(OrgDivision.BOBCAT) || division.equals(OrgDivision.DOOSAN);
    }

    public static String getCompanyIdByCompanyCode(String companyCode) {
        UserDBService service = getUserDBService();
        String companyId = "";
        EnhancedDocument item = service.getCompanyIdByCompanyCode(companyCode);
        if (item != null && StringUtils.isNotBlank(item.getString(DB_FIELD_PASSPORT_ID))) {
            companyId = item.getString(DB_FIELD_PASSPORT_ID);
        }
        return companyId;
    }

    public static String getExpertLevelByUsername(String username) {
        UserDBService service = getUserDBService();
        String level = EXPERT_LEVEL_REGULAR;
        if (StringUtils.isNotBlank(username)) {
            EnhancedDocument item = service.getExpertLevelByUsername(username);
            if (item != null) {
                level = EXPERT_LEVEL_SUPERTECH;
            }
        }
        return level;
    }

    public static String getEmployeeNumber() {
        if (MgnlContext.hasAttribute(ACCESS_TOKEN)) {
            JsonObject decodedToken = getDecodedToken();
            return JSONUtils.getFieldValue(decodedToken, TOKEN_EMPLOYEE_NUMBER);
        } else {
            throw new RuntimeException("Access token is not provided. We can't get employee number.");
        }
    }
}
