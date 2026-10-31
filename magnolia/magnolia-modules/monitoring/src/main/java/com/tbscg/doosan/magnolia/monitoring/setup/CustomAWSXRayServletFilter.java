package com.tbscg.doosan.magnolia.monitoring.setup;

import com.amazonaws.xray.javax.servlet.AWSXRayServletFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

public class CustomAWSXRayServletFilter extends AWSXRayServletFilter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String path = ((HttpServletRequest) request).getRequestURI();
        if (shouldBeExcludedForXRay(path)) {
            chain.doFilter(request, response); 
        } else {
            // execute X-Ray logic
            super.doFilter(request, response, chain);
        }
    }

    private boolean shouldBeExcludedForXRay(String path) {
        return Stream.of("/.magnolia/admincentral", "/VAADIN", "/.resources", "/.rest/monitoring", "/bobcat")
                .anyMatch(k -> path.toLowerCase().startsWith(k.toLowerCase()));
    }
}
