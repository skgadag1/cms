package com.tbscg.doosan.magnolia.sso.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tbscg.doosan.magnolia.dxp.core.aws.AwsHttpClient;
import com.tbscg.doosan.magnolia.sso.SsoDoosanModule;
import com.tbscg.doosan.magnolia.sso.model.PassportIdentity;
import com.tbscg.doosan.magnolia.sso.model.PassportOrganizations;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.io.IOException;

@Slf4j
@Singleton
public class PassportApi {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AwsHttpClient client;

    @Inject
    public PassportApi(final Provider<SsoDoosanModule> ssoDoosanModuleProvider) {
        String passportAuroraApiUrl = ssoDoosanModuleProvider.get().getNgPassportUrl();
        this.client = new AwsHttpClient(passportAuroraApiUrl);
    }

    public PassportIdentity getIdentity(String passportID) {
        try {
            NameValuePair serialNumberParam = new BasicNameValuePair("serial_number", passportID);
            String identityResponse = client.getResource("/identity", serialNumberParam);
            return objectMapper.readValue(identityResponse, PassportIdentity.class);
        } catch (IOException e) {
            log.error("Failed to get identity for passport ID {}", passportID, e);
            return new PassportIdentity();
        }
    }

    public PassportOrganizations getActiveOrganization(String passportId) {
        try {
            NameValuePair serialNumberParam = new BasicNameValuePair("serial_number", passportId);
            NameValuePair statusParam = new BasicNameValuePair("status", "ACTIVE");
            String jsonResponse = client.getResource("/orgselector/search", serialNumberParam, statusParam);
            return objectMapper.readValue(jsonResponse, PassportOrganizations.class);
        } catch (IOException e) {
            log.error("Failed to get organization info for org ID {}", passportId, e);
            return new PassportOrganizations();
        }
    }
}
