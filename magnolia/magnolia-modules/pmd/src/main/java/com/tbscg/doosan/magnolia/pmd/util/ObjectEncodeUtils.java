package com.tbscg.doosan.magnolia.pmd.util;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.defaultString;

public class ObjectEncodeUtils {

    private static final BeanUtilsBean BEAN_UTILS = new BeanUtilsBean(new ConvertUtilsBean() {
        @Override
        public Object convert(String value, Class clazz) {
            if (clazz.isEnum()){
                return Enum.valueOf(clazz, value);
            }else{
                return super.convert(value, clazz);
            }
        }
    });

    public static <C> C decode(String urlEncoded, Class<C> objClass) {
        try {
            C obj = objClass.newInstance();
            Map<String, String> map = new LinkedHashMap<>();
            stream(defaultString(urlEncoded).split("&"))
                    .map(s -> s.split("=", 2))
                    .filter(s -> s.length == 2)
                    .filter(s -> StringUtils.isNotEmpty(s[1]))
                    .forEachOrdered(s -> {
                        try {
                            String name = s[0];
                            String value = URLDecoder.decode(s[1], StandardCharsets.UTF_8.name());
                            System.out.println("decode: " + name + "=" + value);
                            map.put(name, value);
                        } catch (UnsupportedEncodingException ignored) {
                        }
                    });
            BEAN_UTILS.populate(obj, map);
            return obj;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("bean population error", e);
        }
    }

    public static String encode(Object obj) {
        try {
            return BEAN_UTILS.describe(obj).entrySet().stream().map(entry -> {
                String value = entry.getValue();
                if (value != null) {
                    try {
                        return entry.getKey() + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8.name());
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException();
                    }
                }
                return null;
            }).filter(StringUtils::isNotEmpty).collect(joining("&"));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("bean access error", e);
        }
    }
}
