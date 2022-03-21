package com.rh.util;



import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.ws.rs.core.HttpHeaders;

import com.google.gson.Gson;
import com.rh.model.RedHatSSOJWT;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedHatSSOUtils {
    
	private static final Logger LOGGER = LoggerFactory.getLogger(RedHatSSOUtils.class);
	
    private static final String RH_SSO_SERVER = System.getProperty("com.redhat.internal.rhsso.server",
            "http://localhost:8080");

    private static final String RH_SSO_REALM = System.getProperty("com.redhat.internal.rhsso.realm",
            "rhpam-treaty-dxc");

    private static final String RH_SSO_CLIENTID = System.getProperty("com.redhat.internal.rhsso.clientId",
            "springboot-user-app");

    private static final String RH_SSO_CLIENT_SECRET = System.getProperty("com.redhat.internal.rhsso.clientSecret",
            "secret");
    
    private String userName;
    
    private String password;

    private static final String URL_TEMPLATE = "%s/auth/realms/%s/protocol/openid-connect/token";

    private static final String BODY_RAW_DATA_REQUEST = "grant_type=%s&client_id=%s&client_secret=%s&username=%s&password=%s";

    private static final String BODY_RAW_DATA_REFRESH = "grant_type=%s&client_id=%s&refresh_token=%s&client_secret=%s&username=%s&password=%s";

    private static RedHatSSOUtils instance;
    
    private static final long DEFAULT_MIN_VALIDITY = 1; //= 30;
    
    private long expirationTime;
    
    private long minTokenValidity = DEFAULT_MIN_VALIDITY;
    
    private RedHatSSOJWT currentToken;

    public static RedHatSSOUtils getInstance(String userName, String password) {
        //if (instance == null) {
            instance = new RedHatSSOUtils(userName, password);
        //}
        return instance;
    }

    private RedHatSSOUtils(String userName, String password) {
    	this.userName = userName;
    	this.password = password;		
    }

    public synchronized String getAccessToken() {
    	LOGGER.debug("RH_SSO_SERVER: {}, RH_SSO_REALM: {}, RH_SSO_CLIENTID: {}, RH_SSO_CLIENT_SECRET: {}", RH_SSO_SERVER, RH_SSO_REALM, RH_SSO_CLIENTID, RH_SSO_CLIENT_SECRET);
        if (currentToken == null) {
            final int requestTime = TimeUtils.currentTime();
            synchronized (this) {
                this.currentToken = grantToken.get();
                expirationTime = requestTime + currentToken.getExpiresIn();
            }
        } else if (tokenExpired()) {
            this.currentToken = refreshToken.get();
        }
        return currentToken.getAccessToken();
    }

    private synchronized boolean tokenExpired() {
        return (TimeUtils.currentTime() + minTokenValidity) >= expirationTime;
    }

    private Function<String, RedHatSSOJWT> toRedHatSSOJWT = (
            responseBody) -> new Gson() //
            .fromJson(responseBody, RedHatSSOJWT.class);

    private Supplier<byte[]> retriveAuthorizationTokenBody = () -> String
            .format(BODY_RAW_DATA_REQUEST, "password", RH_SSO_CLIENTID, RH_SSO_CLIENT_SECRET, userName, password) //
            .getBytes(StandardCharsets.UTF_8);

    private Supplier<byte[]> refreshAuthorizationTokenBody = () -> String.format(BODY_RAW_DATA_REFRESH, "refresh_token",
            RH_SSO_CLIENTID, currentToken.getRefreshToken(), RH_SSO_CLIENT_SECRET).getBytes(StandardCharsets.UTF_8);

    private Supplier<String> buildRwdHatSsoRealmUrlFromTemplate = () -> String.format(URL_TEMPLATE, RH_SSO_SERVER,
            RH_SSO_REALM);

    private Function<Supplier<byte[]>, HttpUriRequest> createHttpUriRequest = (requestBodySupplier) -> RequestBuilder //
            .create("POST") //
            .addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded") //
            .setUri(buildRwdHatSsoRealmUrlFromTemplate.get()) //
            .setEntity(new ByteArrayEntity(requestBodySupplier.get())) //
            .build();

    private Function<HttpUriRequest, Optional<RedHatSSOJWT>> execute = (request) -> {
        try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {
            final HttpResponse response = httpClient.execute(request);
            final HttpEntity entity = response.getEntity();
            final String responseString = EntityUtils.toString(entity, "UTF-8");
            return Optional.of(toRedHatSSOJWT.apply(responseString));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    };

    private Supplier<RedHatSSOJWT> grantToken = () -> createHttpUriRequest //
            .andThen(execute) //
            .apply(retriveAuthorizationTokenBody) //
            .orElseThrow(() -> new RuntimeException("Unable to generate token"));

    private Supplier<RedHatSSOJWT> refreshToken = () -> createHttpUriRequest //
            .andThen(execute) //
            .apply(refreshAuthorizationTokenBody) //
            .orElseThrow(() -> new RuntimeException("Unable to refresh token"));

}