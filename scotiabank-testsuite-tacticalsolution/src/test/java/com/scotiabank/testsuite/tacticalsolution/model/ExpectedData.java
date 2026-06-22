package com.scotiabank.testsuite.tacticalsolution.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExpectedData {

    @JsonProperty("type_case")
    private String typeCase;

    @JsonProperty("subcase_null")
    private boolean subcaseNull;

    @JsonProperty("user_id_not_empty")
    private boolean userIdNotEmpty;

    public String getTypeCase() {
        return typeCase;
    }

    public void setTypeCase(String typeCase) {
        this.typeCase = typeCase;
    }

    public boolean isSubcaseNull() {
        return subcaseNull;
    }

    public void setSubcaseNull(boolean subcaseNull) {
        this.subcaseNull = subcaseNull;
    }

    public boolean isUserIdNotEmpty() {
        return userIdNotEmpty;
    }

    public void setUserIdNotEmpty(boolean userIdNotEmpty) {
        this.userIdNotEmpty = userIdNotEmpty;
    }
}
