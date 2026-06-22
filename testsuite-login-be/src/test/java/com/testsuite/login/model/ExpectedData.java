package com.testsuite.login.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExpectedData {

    @JsonProperty("type_case")
    private String typeCase;

    /** Si es true, valida que data.subcase sea null. */
    @JsonProperty("subcase_null")
    private boolean subcaseNull;

    /** Si es true, valida que data.user_id sea string no vacío. */
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
