package com.tbscg.doosan.magnolia.bobcat.ui.contentapp.html.mapper;

import org.apache.commons.text.StringEscapeUtils;

import java.util.Collection;

public class MapToTokensElement {

    public static String create(Collection<String> tokens) {
        String s = tokens.stream()
                .map(MapToTokensElement::createTokenElement)
                .reduce((accumulator, token) -> accumulator + '\n' + token).orElse("");

        return wrapHTML(s);
    }


    private static String wrapHTML(String s) {
        return  "<div class=\"component-wrap\" style=\"\">\n" +
                "   <div class=\"v-customcomponent v-disabled v-widget v-has-width\" style=\"width: 100%;\">\n" +
                "      <div class=\"v-horizontallayout v-layout v-horizontal v-disabled v-widget token-field-horizontal-layout v-horizontallayout-token-field-horizontal-layout\">\n" +
                "         <div class=\"v-slot\">\n" +
                "            <div class=\"exttokenfield v-disabled v-widget\">\n" +
                s +
                "            </div>\n" +
                "         </div>\n" +
                "      </div>\n" +
                "   </div>\n" +
                "</div>";
    }

    private static String createTokenElement(String token) {
        return  "<div class=\"token\">\n" +
                "   <div class=\"token-content\">\n" +
                "      <div class=\"token-label\">" + escapeHtml(token) + "</div>\n" +
                "   </div>\n" +
                "</div>\n";
    }

    private static String escapeHtml(String value) {
        return StringEscapeUtils.escapeHtml4(value);
    }
}
