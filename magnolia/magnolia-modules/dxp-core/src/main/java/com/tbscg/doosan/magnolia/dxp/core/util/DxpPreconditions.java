package com.tbscg.doosan.magnolia.dxp.core.util;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

/**
 * Preconditions class extension {@link com.google.common.base.Preconditions}
 */
public class DxpPreconditions {

    /**
     * Checks whether the provided string argument is blank or not.
     *
     * @param strArgument argument to validate.
     * @param errorMessage error message shown in case that the precondition is not satisfied.
     * @throws IllegalArgumentException if the argument is blank.
     */
    public static void checkNotBlank(String strArgument, String errorMessage) {
        Preconditions.checkArgument(StringUtils.isNotBlank(strArgument), errorMessage);
    }

}
