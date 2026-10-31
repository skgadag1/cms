/**
 * This file Copyright (c) 2019 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This program and the accompanying materials are made
 * available under the terms of the Magnolia Network Agreement
 * which accompanies this distribution, and is available at
 * http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package com.tbscg.doosan.magnolia.toc.io;

import info.magnolia.jcr.RuntimeRepositoryException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Copied - Needed due to protected access.
 *
 * Utility class.
 */
class MultiValueStringConverter {

    private MultiValueStringConverter() {
        // hide the implicit public one.
    }

    /**
     * Multi value utility that will return an array from a string in the form [x, x].
     * @param value
     */
    static String[] toArray(String value) {
        //TODO: Improvement check if the values contain commas
        if (StringUtils.isNotBlank(value)) {
            return value.replace("[", "").replace("]", "").split(",\\s*");
        }
        return null;
    }

    /**
     * Multi value utility that will get the values array and return a string in the form [x, x].
     * @param values
     */
    static String toString(Value[] values) {
        //TODO: Improvement check if the values contain commas
        if (ArrayUtils.isEmpty(values)) {
            return null;
        }
        return Arrays.stream(values)
                .map(value -> {
                    try {
                        return value.getString();
                    } catch (RepositoryException e) {
                        throw new RuntimeRepositoryException("Can not transform array value " + value, e);
                    }
                })
                .collect(Collectors.joining(","));
    }
}
