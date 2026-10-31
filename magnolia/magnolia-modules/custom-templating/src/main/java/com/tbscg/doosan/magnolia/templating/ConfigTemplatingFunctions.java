package com.tbscg.doosan.magnolia.templating;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;

import info.magnolia.init.MagnoliaConfigurationProperties;

public class ConfigTemplatingFunctions {

	public static final String NAME = "cfgfn";

	private MagnoliaConfigurationProperties properties;

	@Inject
	public ConfigTemplatingFunctions(MagnoliaConfigurationProperties properties) {
		this.properties = properties;
	}

	public String get(String key) {
		return getProperty(key);
	}

	public String getProperty(String key) {
		return this.properties.getProperty(key);
	}

	public PocProperties getPoc() {
		return new PocProperties();
	}

	public KagsProperties getKags() {
		return new KagsProperties();
	}

	public class PocProperties implements SiteProperties {

		public String getStaticServer() {
			return getProperty("magnolia.poc.static.server");
		}

		public String getApiServer() {
			return getProperty("magnolia.poc.api.server");
		}
	}

	public class KagsProperties implements SiteProperties {

		public String getStaticServer() {
			return getProperty("magnolia.kags.static.server");
		}

		public String getContentApiServer() {
			return getProperty("magnolia.kags.content.api.server");
		}

		public String getNotificationsApiServer() {
			return getProperty("magnolia.kags.notifications.api.server");
		}

		public String getBdApiServer() { return getProperty("magnolia.kags.bd.api.server"); }

		public String getServiceAiApiServer() { return getProperty("magnolia.kags.serviceai.api.server"); }

		public String getAgentAiApiServer() { return getProperty("magnolia.kags.agentai.api.server"); }

		public String getApiServersJSON() throws IOException {
			JsonObject json = Json.createObjectBuilder()
				.add("bd-api", getBdApiServer())
				.add("content-api", getContentApiServer())
				.add("notifications-api", getNotificationsApiServer())
				.add("service-api", getServiceAiApiServer())
				.add("agent-api", getAgentAiApiServer())
				.build();

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			JsonWriter writer = Json.createWriter(out);
			writer.writeObject(json);
			writer.close();
			return new String(out.toByteArray());
		}
	}
}
