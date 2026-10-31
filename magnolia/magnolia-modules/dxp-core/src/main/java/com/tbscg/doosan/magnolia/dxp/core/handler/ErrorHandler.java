package com.tbscg.doosan.magnolia.dxp.core.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ErrorHandler {
    void handle(HttpServletRequest request, HttpServletResponse response, Exception exception);
}
