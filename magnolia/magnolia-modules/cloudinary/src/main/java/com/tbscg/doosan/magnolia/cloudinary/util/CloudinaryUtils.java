package com.tbscg.doosan.magnolia.cloudinary.util;

import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import lombok.extern.slf4j.Slf4j;
import org.cloudinary.json.JSONArray;
import org.cloudinary.json.JSONObject;


import java.util.*;

import static com.tbscg.doosan.magnolia.cloudinary.util.CloudinaryConstants.CLOUDINARY_ERROR_METHOD_HANDLER;

@Slf4j
public class CloudinaryUtils {

    public static void handleCloudinaryError() {
        JavaScript.getCurrent().addFunction(CLOUDINARY_ERROR_METHOD_HANDLER, arguments -> {
            UI.getCurrent().access(() -> {
                Notification.show("Unable to access cloudinary", Notification.Type.ERROR_MESSAGE);
            });
        });
    }

    public static Map<String, Object> convertJsonObjectToMap(JSONObject jsonObject) {
        Map<String, Object> map = new HashMap<>();
        for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
            String key = it.next();
            Object value = jsonObject.get(key);
            if (value instanceof JSONObject) {
                map.put(key, convertJsonObjectToMap((JSONObject) value));
            } else if (value instanceof JSONArray) {
                map.put(key, convertJsonArrayToList((JSONArray) value));
            } else {
                map.put(key, value);
            }
        }
        return map;
    }

    public static List<Object> convertJsonArrayToList(JSONArray jsonArray) {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            Object value = jsonArray.get(i);
            if (value instanceof JSONObject) {
                list.add(convertJsonObjectToMap((JSONObject) value));
            } else if (value instanceof JSONArray) {
                list.add(convertJsonArrayToList((JSONArray) value));
            } else {
                list.add(value);
            }
        }
        return list;
    }
}
