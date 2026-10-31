package com.tbscg.doosan.magnolia.cloudinary.field.definition;

import com.tbscg.doosan.magnolia.cloudinary.CloudinaryConfig;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.javascript.field.JavascriptFieldDefinition;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Javascript field definition for the bento layout editor that exposes the environment-specific
 * Cloudinary cloud name and API key (read from {@code config:/modules/cloudinary}) to the script
 * via the field parameters. The bento in-tile image picker uses them to open the Cloudinary media
 * library against the correct account instead of a hard-coded one.
 */
public class CloudinaryBentoLayoutFieldDefinition extends JavascriptFieldDefinition {

	@Override
	public Map<String, Object> getParameters() {
		Map<String, Object> parameters = new HashMap<>();
		Map<String, Object> configured = super.getParameters();
		if (configured != null) {
			parameters.putAll(configured);
		}

		CloudinaryConfig config = Components.getComponent(CloudinaryConfig.class);
		if (config != null && config.getApi() != null) {
			String cloudName = config.getApi().getCloudName();
			String apiKey = config.getApi().getApiKey();
			if (StringUtils.isNotBlank(cloudName)) {
				parameters.put("cloudName", cloudName);
			}
			if (StringUtils.isNotBlank(apiKey)) {
				parameters.put("apiKey", apiKey);
			}
		}
		return parameters;
	}
}
