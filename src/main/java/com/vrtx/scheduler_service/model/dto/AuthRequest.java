package com.vrtx.scheduler_service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NonNull;

@Data
public class AuthRequest {
    @NonNull
    @JsonProperty("Username")
    private String username;

    @NonNull
    @JsonProperty("Password")
    private String password;
}
