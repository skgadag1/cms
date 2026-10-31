package com.tbscg.doosan.magnolia.bobcat.util;

import info.magnolia.cms.util.RequestDispatchUtil;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.util.regex.Pattern;

public class DxpRequestDispatchUtil {

    public static final String PROXY_WITH_KEY_PREFIX = "key-proxy:";
    private static final Logger log = LoggerFactory.getLogger(RequestDispatchUtil.class);
    private static final Pattern escapePattern = Pattern.compile("%[0-9]{1,}");


    public static boolean dispatch(String location, HttpServletRequest request, HttpServletResponse response, String apiKey) {
        if (location == null) {
            return false;
        } else {
            if (location.startsWith(PROXY_WITH_KEY_PREFIX)) {
                String forwardUrl = StringUtils.substringAfter(location, PROXY_WITH_KEY_PREFIX);

                forwardToExternalURL(response, forwardUrl, apiKey);
                return true;
            } else {
                return RequestDispatchUtil.dispatch(location, request, response);
            }
        }
    }

    private static void forwardToExternalURL(HttpServletResponse response, String forwardUrl, String apiKey) {

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String decodedUrl = URLDecoder.decode(forwardUrl, "UTF-8");
            if(escapePattern.matcher(decodedUrl).find()){
                forwardUrl = decodedUrl.replaceAll(" ", "+");
            }

            HttpGet req = new HttpGet(forwardUrl);
            if (apiKey != null && !apiKey.isEmpty()) {
                log.debug("Forwarding to external URL {} with API key", forwardUrl);
                req.addHeader("x-api-key", apiKey);
            }
            try (CloseableHttpResponse resp = httpClient.execute(req)) {
                HttpEntity entity = resp.getEntity();
                response.setStatus(resp.getStatusLine().getStatusCode());
                if (entity != null) {
                    response.setContentType(entity.getContentType().getValue());
                    entity.writeTo(response.getOutputStream());
                }
            }
        } catch (Exception e) {
            log.error("Failed to forward to  external URL {} - {}:{}", forwardUrl, ClassUtils.getShortClassName(e.getClass()), e.getMessage());
        }
    }
}
