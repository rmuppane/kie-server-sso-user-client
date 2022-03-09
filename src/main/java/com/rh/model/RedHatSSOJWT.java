package com.rh.model;

import com.google.gson.annotations.SerializedName;

public class RedHatSSOJWT {
    
    @SerializedName("access_token")
    private String accessToken;
    
    @SerializedName("expires_in")
    private long expiresIn;
    
    @SerializedName("refresh_expires_in")
    private String refreshExpiresIn;
    
    @SerializedName("refresh_token")
    private String refreshToken;
    
    @SerializedName("token_type")
    private String tokenType;
    
    @SerializedName("not-before-policy")
    private String notBeforePolicy;
    
    @SerializedName("session_state")
    private String sessionState;
    
    @SerializedName("scope")
    private String scope;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getRefreshExpiresIn() {
        return refreshExpiresIn;
    }

    public void setRefreshExpiresIn(String refreshExpiresIn) {
        this.refreshExpiresIn = refreshExpiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getNotBeforePolicy() {
        return notBeforePolicy;
    }

    public void setNotBeforePolicy(String notBeforePolicy) {
        this.notBeforePolicy = notBeforePolicy;
    }

    public String getSessionState() {
        return sessionState;
    }

    public void setSessionState(String sessionState) {
        this.sessionState = sessionState;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    @Override
    public String toString() {
        return "RedHatSSOJWT [accessToken=" + accessToken + ", expiresIn=" + expiresIn + ", notBeforePolicy="
                + notBeforePolicy + ", refreshExpiresIn=" + refreshExpiresIn + ", refreshToken=" + refreshToken
                + ", scope=" + scope + ", sessionState=" + sessionState + ", tokenType=" + tokenType + "]";
    }
}