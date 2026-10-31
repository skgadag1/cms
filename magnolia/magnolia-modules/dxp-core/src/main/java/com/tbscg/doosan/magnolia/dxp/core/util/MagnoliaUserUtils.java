package com.tbscg.doosan.magnolia.dxp.core.util;

import info.magnolia.context.MgnlContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MagnoliaUserUtils {

    public static String getCurrentUserEmail() {
        try {
            return MgnlContext.getUser().getProperty("email");
        } catch (Exception e) {
            log.error("Could not get current username: {}", e.getMessage());
            return null;
        }
    }
}
