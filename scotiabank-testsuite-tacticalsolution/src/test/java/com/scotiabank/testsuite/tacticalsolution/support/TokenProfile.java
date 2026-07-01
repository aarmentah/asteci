package com.scotiabank.testsuite.tacticalsolution.support;

public enum TokenProfile {

    DEFAULT("default", "api.access.token", "passport.base.uri", "passport.token.path"),
    CUSTOMER_SUMMARY(
            "customer-summary",
            "customer.summary.api.access.token",
            "customer.summary.passport.base.uri",
            "customer.summary.passport.token.path"),
    CUSTOMER_LOOKUP(
            "customer-lookup",
            "customer.lookup.api.access.token",
            "customer.lookup.passport.base.uri",
            "customer.lookup.passport.token.path");

    private final String id;
    private final String manualTokenKey;
    private final String passportBaseUriKey;
    private final String passportTokenPathKey;

    TokenProfile(String id, String manualTokenKey, String passportBaseUriKey, String passportTokenPathKey) {
        this.id = id;
        this.manualTokenKey = manualTokenKey;
        this.passportBaseUriKey = passportBaseUriKey;
        this.passportTokenPathKey = passportTokenPathKey;
    }

    public String id() {
        return id;
    }

    public String manualTokenKey() {
        return manualTokenKey;
    }

    public String passportBaseUriKey() {
        return passportBaseUriKey;
    }

    public String passportTokenPathKey() {
        return passportTokenPathKey;
    }

    public static TokenProfile fromId(String profileId) {
        if (profileId == null || profileId.isBlank()) {
            return DEFAULT;
        }
        for (TokenProfile profile : values()) {
            if (profile.id.equalsIgnoreCase(profileId.trim())) {
                return profile;
            }
        }
        throw new IllegalArgumentException("Perfil de token desconocido: " + profileId);
    }
}
