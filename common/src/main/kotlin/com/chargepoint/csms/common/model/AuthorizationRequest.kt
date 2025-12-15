package com.chargepoint.csms.common.model

import com.fasterxml.jackson.annotation.JsonProperty

data class DriverIdentifier(
    val id: String
)

data class AuthorizationRequest(
    @JsonProperty("stationUuid")
    val stationUuid: String,
    @JsonProperty("driverIdentifier")
    val driverIdentifier: DriverIdentifier
)
