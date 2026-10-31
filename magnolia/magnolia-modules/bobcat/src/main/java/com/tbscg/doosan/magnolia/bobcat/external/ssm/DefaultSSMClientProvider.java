package com.tbscg.doosan.magnolia.bobcat.external.ssm;

import software.amazon.awssdk.services.ssm.SsmAsyncClient;
import software.amazon.awssdk.services.ssm.SsmClient;

import javax.inject.Provider;

/**
 * Factory class for {@link SsmClient}. Creates default client.
 */
public class DefaultSSMClientProvider implements Provider<SsmClient> {
    private final SsmClient ssmClient = SsmClient.create();

    @Override
    public SsmClient get() {
        return ssmClient;
    }

}
