package com.tbscg.doosan.magnolia.cloudinary.util;

import org.apache.http.HttpStatus;

import javax.ws.rs.HttpMethod;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Helper class to support external resources handling.
 */
public class ExternalResourceHelper {

    /**
     * Checks whether a resource URL is valid or not by opening a connection and checking its response code.
     *
     * @param urlString string url to validate.
     * @return whether the external resource is valid or not.
     */
    public static boolean isValidResource(String urlString) {
        HttpURLConnection connection = null;

        try {
            URL resourceUrl = new URL(urlString);
            connection = (HttpURLConnection) resourceUrl.openConnection();
            connection.setRequestMethod(HttpMethod.HEAD);

            return connection.getResponseCode() == HttpStatus.SC_OK;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception ignored) {}
        }
    }

}
