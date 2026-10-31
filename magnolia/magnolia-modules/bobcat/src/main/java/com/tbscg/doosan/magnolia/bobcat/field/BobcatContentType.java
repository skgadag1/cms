package com.tbscg.doosan.magnolia.bobcat.field;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Enum representing types available.
 */
public enum BobcatContentType {

    REGION("mgnl:folder", "Region"),
    GROUP("dxp:facetGroup", "Facet Group"),
    CATEGORY("dxp:facetCategory", "Facet Category"),
    PRODUCT("dxp:facet", "Facet"),
    TAG_GROUP("dxp:tagGroup", "Tag Group"),
    TAG("dxp:tag", "Tag"),
    FORM_FIELD_GROUP("dxp:fieldGroup", "Field group"),
    FORM_CONFIGURATION("dxp:formConfiguration", "Form configuration"),
    FORM_FIELD_TYPE("dxp:fieldType", "Field type"),
    FORM_EMAIL_CONFIG("dxp:formEmailConfiguration", "Email configuration"),
    MARKETING_CAMPAIGN("dxp:marketingCampaign", "Marketing campaign"),
    NAVIGATION_MEGA_MENU("dxp:navigationMegaMenu", "Mega Menu"),
    NAVIGATION_MEGA_LIST("dxp:navigationMegaList", "Mega List"),
    NAVIGATION_LIST_MENU("dxp:navigationList", "List Menu"),
    NAVIGATION_LINK_MENU("dxp:navigationLink", "Link Menu"),
    NAVIGATION_ROW("dxp:navigationRow", "Mega Menu Row"),
    NAVIGATION_COLUMN("dxp:navigationColumn", "Mega Menu Column"),
    NAVIGATION_LIST_COLUMN("dxp:navigationMegaListColumn", "Mega List Column"),
    NAVIGATION_ITEM("dxp:navigationItem", "Menu Item");

    private final String type;
    private final String name;
    private static final Map<String, String> typesMap;

    static {
        typesMap = new HashMap<>();
        for (BobcatContentType bobcatContentType : values()) {
            typesMap.put(bobcatContentType.type, bobcatContentType.name);
        }
    }

    BobcatContentType(final String type, final String name) {
        this.type = type;
        this.name = name;
    }

    /**
     * Returns custom Bobcat Content Type name from JCR type.
     *
     * @param type JCR Node Type
     * @return Bobcat Content name
     */
    public static String getNameByType(final String type) {
        return typesMap.get(type);
    }

    /**
     * Returns Bobcat Content from JCR type
     *
     * @param type JCR Node Type
     * @return Bobcat Content
     */
    public static BobcatContentType getBobcatContentByType(final String type) {
        return Arrays.stream(BobcatContentType.values())
                .filter(bobcatContent -> bobcatContent.type.equals(type))
                .findFirst().orElse(null);
    }
}
