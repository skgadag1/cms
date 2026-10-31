package com.tbscg.doosan.magnolia.dxp.core.util;

import org.apache.commons.lang.StringUtils;

import static org.apache.jackrabbit.ocm.repository.NodeUtil.PATH_SEPARATOR;

public class PathUtil {

	public static final String ROOT_FOLDER_PATH = "";

	public static String getFileName(String path) {
		int lastSeparator = StringUtils.lastIndexOf(path, PATH_SEPARATOR);
		if (lastSeparator >= 0) {
			return StringUtils.substringAfterLast(path, PATH_SEPARATOR);
		} else {
			return path;
		}
	}

	public static String getPath(String parent, String name) {
		if (parent.isEmpty()) {
			return name;
		} else {
			return parent + PATH_SEPARATOR + name;
		}
	}

	public static String getParentPath(String path) {
		int lastSeparator = StringUtils.lastIndexOf(path, PATH_SEPARATOR);
		if (lastSeparator >= 0) {
			return StringUtils.substring(path, 0, lastSeparator);
		} else {
			return ROOT_FOLDER_PATH;
		}
	}
}
