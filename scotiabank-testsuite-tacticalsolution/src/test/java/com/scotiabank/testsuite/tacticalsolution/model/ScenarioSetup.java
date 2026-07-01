package com.scotiabank.testsuite.tacticalsolution.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ScenarioSetup {

    @JsonProperty("token_profile")
    private String tokenProfile = "default";

    @JsonProperty("surrogate_keys")
    private Map<String, String> surrogateKeys = new LinkedHashMap<>();

    public String getTokenProfile() {
        return tokenProfile;
    }

    public void setTokenProfile(String tokenProfile) {
        this.tokenProfile = tokenProfile;
    }

    public Map<String, String> getSurrogateKeys() {
        return surrogateKeys;
    }

    public void setSurrogateKeys(Map<String, String> surrogateKeys) {
        this.surrogateKeys = surrogateKeys != null ? new LinkedHashMap<>(surrogateKeys) : new LinkedHashMap<>();
    }
}
