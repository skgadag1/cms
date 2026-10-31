package com.tbscg.doosan.magnolia.sso.service;

import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserDBService {

    private static final String COMPANY_CODE_DB_FIELD = "code";
    private static final String USERNAME_DB_FIELD = "username_uppercase";
    private static final String SMART_EQUIP_TABLE = "SmartEquipOrganization";
    private static final String SUPERTECH_USERS_TABLE = "SuperTechUsers";

    private final DynamoDBService dynamoDBService;

    @Inject
    public UserDBService(DynamoDBService dynamoDBService) {
        this.dynamoDBService = dynamoDBService;
    }

    public EnhancedDocument getCompanyIdByCompanyCode(String companyCode) {
        return dynamoDBService.getItemsByStringKey(COMPANY_CODE_DB_FIELD, SMART_EQUIP_TABLE, companyCode);
    }

    public EnhancedDocument getExpertLevelByUsername(String username) {
        return dynamoDBService.getItemsByStringKey(USERNAME_DB_FIELD, SUPERTECH_USERS_TABLE, username.toUpperCase());
    }
}
