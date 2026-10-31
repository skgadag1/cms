package com.tbscg.doosan.magnolia.dxp.core.util;

import org.apache.jackrabbit.core.id.NodeId;

/**
 * JCR utility class.
 *
 * @author Alejandro Puig Escobar
 */

public class JcrUtil {

    /**
     * Check if an id is also a valid UUID.
     *
     * @param id id to check.
     * @return whether the id is a valid UUID or not.
     */
    public static boolean isUUID(String id) {
        try {
            if (id != null) {
                new NodeId(id);
                return true;
            }
        } catch (IllegalArgumentException e) {
            //malformed UUID
            return false;
        }

        return false;
    }
}
