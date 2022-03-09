package com.rh.client;

import org.kie.server.client.CredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static javax.ws.rs.core.HttpHeaders.*;

import java.util.function.Supplier;

public class RedHatSSOCredentialProvider implements CredentialsProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedHatSSOCredentialProvider.class);

    private final Supplier<String> bearerTokenProvider;

    public RedHatSSOCredentialProvider(final Supplier<String> bearerTokenProvider) {
        this.bearerTokenProvider = bearerTokenProvider;
    }

    @Override
    public String getHeaderName() {
        return AUTHORIZATION;
    }

    @Override
    public String getAuthorization() {
        String bearerToken = bearerTokenProvider.get();
        LOGGER.info("adding token: {}", bearerToken);
        return TOKEN_AUTH_PREFIX + bearerToken;
    }
}