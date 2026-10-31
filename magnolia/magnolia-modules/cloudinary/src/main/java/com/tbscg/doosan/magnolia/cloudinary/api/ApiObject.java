package com.tbscg.doosan.magnolia.cloudinary.api;

import com.cloudinary.utils.ObjectUtils;
import com.tbscg.doosan.magnolia.dxp.core.util.DateUtil;
import org.cloudinary.json.JSONArray;
import org.cloudinary.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ApiObject {

	private final Map properties;

	public ApiObject(Map properties) {
		this.properties = properties;
	}

	public static ApiObject fromEncodedString(String encoded) {
		byte[] bytes = Base64.getDecoder().decode(encoded);
		String jsonString = new String(bytes, StandardCharsets.UTF_8);
		JSONObject json = new JSONObject(jsonString);
		Map<String, Object> map = ObjectUtils.toMap(json);
		return new ApiObject(map);
	}

	public static String toEncodedString(Map<String, Object> mapToEncode) {
		return Base64.getEncoder().encodeToString(toJsonString(mapToEncode).getBytes(StandardCharsets.UTF_8));
	}

	public static String toJsonString(Map<String, Object> mapToJson) {
		JSONObject json = new JSONObject(mapToJson);
		return json.toString();
	}

	public String getString(String key) {
		return (String) properties.get(key);
	}

	public Calendar getDate(String key) {
		String date = this.getString(key);
		if (date != null) {
			try {
				return DateUtil.parseUTCDate(date);
			} catch (ParseException pe) {/*silent exception*/}
		}
		return null;
	}

	public Double getDouble(String key) {
		return (Double) properties.get(key);
	}

	public Integer getInt(String key) {
		return (Integer) properties.get(key);
	}

	public Long getLong(String key) {
		final Object value = properties.get(key);
		if (value instanceof Long) {
			return (Long) value;
		}
		return this.getInt(key).longValue();
	}

	public Number getNumber(String key) {
		return (Number) properties.get(key);
	}

	public Object getRaw(String key) {
		return properties.get(key);
	}

	public ApiObject getObject(String key) {
		Map map = (Map) getRaw(key);
		if (map != null) {
			return new ApiObject(map);
		} else {
			return null;
		}
	}

	public List<ApiObject> getList(String key) {
		List<Object> list = Collections.emptyList();
		Object rawValue = getRaw(key);
		if (rawValue instanceof JSONArray) {
			list = ObjectUtils.toList((JSONArray) rawValue);
		} else if (rawValue instanceof List) {
			list = (List) rawValue;
		}
		return list.stream().map(o -> {
			if (o instanceof Map) {
				return new ApiObject((Map) o);
			} if (o instanceof JSONObject) {
				Map<String, Object> map = ObjectUtils.toMap((JSONObject) o);
				return new ApiObject(map);
			} else {
				throw new IllegalArgumentException("");
			}
		}).collect(Collectors.toList());
	}

	public Map<String, Object> getProperties() {
		return properties;
	}
}
