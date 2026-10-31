package com.tbscg.doosan.magnolia.monitoring.rest;

import com.tbscg.doosan.magnolia.monitoring.model.Status;
import info.magnolia.rest.AbstractEndpoint;
import info.magnolia.rest.EndpointDefinition;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;

@Slf4j
@Path("/monitoring")
public class MonitoringEndpoint<D extends EndpointDefinition> extends AbstractEndpoint<D> {
	public static final String PATH_STATUS = "/status";

	@Inject
	@Named("magnolia.docker.version.file")
	private Provider<String> versionFilePath;

	private String dockerVersion = "";

	public MonitoringEndpoint(D definition) {
		super(definition);
	}

	private String getDockerVersion() {
		if (StringUtils.isEmpty(this.dockerVersion)) {
			String path = this.versionFilePath.get();
			this.dockerVersion = readDockerVersionFromFile(path);
		}
		return this.dockerVersion;
	}

	private String readDockerVersionFromFile(String filepath) {
		if (filepath != null) {
			File versionFile = new File(filepath);
			if (versionFile.exists() && versionFile.isFile()) {
				log.info("version file found: {}, reading contents...", filepath);
				try {
					byte[] bytes = Files.readAllBytes(versionFile.toPath());
					return new String(bytes);
				} catch (IOException e) {
					log.warn("error reading contents of version file", e);
				}
			}
		}
		return StringUtils.EMPTY;
	}

	private String getHostname() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			return StringUtils.EMPTY;
		}
	}

	@Path(PATH_STATUS)
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Status getStatus() {
		Status result = new Status();
		result.setStatus("OK");
		result.setVersion(getDockerVersion());
		result.setHostname(getHostname());
		return result;
	}
}
