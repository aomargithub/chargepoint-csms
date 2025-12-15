package com.chargepoint.csms.common.model

import com.fasterxml.jackson.annotation.JsonProperty

enum class AuthorizationStatus {
    Accepted,
    Unknown,
    Invalid,
    Rejected
}

data class AuthorizationResponse(
    @JsonProperty("authorizationStatus")
    val authorizationStatus: AuthorizationStatus
)
