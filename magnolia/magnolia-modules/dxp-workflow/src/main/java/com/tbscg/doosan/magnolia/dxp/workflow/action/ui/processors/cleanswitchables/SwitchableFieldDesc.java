package com.tbscg.doosan.magnolia.dxp.workflow.action.ui.processors.cleanswitchables;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Describes switchable field in form definition.
 * Maps "field" value to list of property names that are relevant to this field value.
 */
@Data
@Builder(setterPrefix = "with")
@ToString
public
class SwitchableFieldDesc {
    /**
     * Pattern to match field path.
     */
    private Pattern fieldPathPattern;
    /**
     * Map of "field" value to list of property names that are relevant to this field value.
     */
    private Map<String, List<String>> valueToPropertyList;

    /**
     * Checks if path matches fieldPathPattern.
     *
     * @param path path to check
     * @return true if path matches fieldPathPattern
     */
    public boolean pathMatches(final CharSequence path) {
        return this.fieldPathPattern.matcher(path).matches();
    }

}
