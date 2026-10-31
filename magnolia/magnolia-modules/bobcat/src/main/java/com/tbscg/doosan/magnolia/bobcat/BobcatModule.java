package com.tbscg.doosan.magnolia.bobcat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tbscg.doosan.magnolia.bobcat.commands.BackupCommandConfig;
import com.tbscg.doosan.magnolia.bobcat.util.ApiKey;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.ssm.SsmClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;

import static com.tbscg.doosan.magnolia.dxp.core.util.LoggingUtil.sanitizeMessage;

/**
 * This class is optional and represents the configuration for the magnolia-module-bobcat module.
 * By exposing simple getter/setter/adder methods, this bean can be configured via content2bean
 * using the properties and node from <tt>config:/modules/magnolia-module-bobcat</tt>.
 * If you don't need this, simply remove the reference to this class in the module descriptor xml.
 * See https://documentation.magnolia-cms.com/display/DOCS/Module+configuration for information about module configuration.
 */
@Slf4j
@Getter
@Setter
public class BobcatModule implements ModuleLifecycle, BobcatModuleConfiguration {

    public static final String MODULE_NAME = "bobcat";

    private static final String COUNTRY_HEADER = "CloudFront-Viewer-Country";
    @Getter
    private String ssrApiKeySecretName;
    @Getter
    private String ssrApiKey;
    @Getter
    private String bobcatApiKeySecretName;
    @Getter
    private String bobcatApiKey;

    private final Provider<SsmClient> ssmClientProvider;
    private Map<String, String> countryRegionMappings = Collections.emptyMap();
    private boolean customPathResolutionEnabled = false;
    private BackupCommandConfig backupCommandConfig;
    private SecretsManagerClient secretsClient;

    @Inject
    public BobcatModule(final Provider<SsmClient> ssmClientProvider) {
        this.ssmClientProvider = ssmClientProvider;
        this.secretsClient = SecretsManagerClient.builder()
                .build();
    }

    public String getDetectedRegionName(HttpServletRequest request) {
        String detectedCountry = request.getHeader(COUNTRY_HEADER);
        if (StringUtils.isNotBlank(detectedCountry)) {
            log.debug("Detected country: {}", sanitizeMessage(detectedCountry));
            return getCountryRegionMappings().get(detectedCountry);
        }
        return null;
    }

    @Override
    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        log.info("Module {} starting.", BobcatModule.MODULE_NAME);
    }

    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
        log.debug("Module {} stopping.", BobcatModule.MODULE_NAME);
    }

    public void setSsrApiKeySecretName(final String ssrApiKeySecretName) {
        this.ssrApiKeySecretName = ssrApiKeySecretName;
        resolveSsrApiKeySecret();
    }

    private void resolveSsrApiKeySecret() {
        String secret = resolveStringSecret(this.ssrApiKeySecretName);
        log.info("SSR Api Key resolved succesfully");
        this.ssrApiKey = getApiKeyFromSecret(secret);
    }

    public void setBobcatApiKeySecretName(final String bobcatApiKeySecretName) {
        this.bobcatApiKeySecretName = bobcatApiKeySecretName;
        resolveBobcatApiKeySecret();
    }

    private void resolveBobcatApiKeySecret() {
        String secret = resolveStringSecret(this.bobcatApiKeySecretName);
        log.info("Bobcat Api Key resolved succesfully");
        this.bobcatApiKey = getApiKeyFromSecret(secret);
    }

    private String getApiKeyFromSecret(String secret) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(secret, ApiKey.class).getApiKey();
        } catch (JsonProcessingException e) {
            log.error("Failed to parse secret {}", this.ssrApiKeySecretName);
            return "";
        }
    }

    private String resolveStringSecret(String secretName) {
        try {
            GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();
            GetSecretValueResponse valueResponse = this.secretsClient.getSecretValue(valueRequest);
            return valueResponse.secretString();
        } catch (Exception e) {
            log.error("Failed to get secret {} - {}:{}", secretName, ClassUtils.getShortClassName(e.getClass()), e.getMessage());
            return "";
        }
    }
}
