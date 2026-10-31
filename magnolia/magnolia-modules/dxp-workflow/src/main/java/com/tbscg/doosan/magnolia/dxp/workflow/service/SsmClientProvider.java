package com.tbscg.doosan.magnolia.dxp.workflow.service;

import software.amazon.awssdk.services.ssm.SsmClient;

import javax.inject.Provider;

public class SsmClientProvider implements Provider<SsmClient> {
    private final SsmClient ssmClient = SsmClient.create();

    @Override
    public SsmClient get() {
        return ssmClient;
    }

}
