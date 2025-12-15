package com.chargepoint.csms.common.model

import java.util.UUID

data class AuthorizationMessage(
    val requestId: String = UUID.randomUUID().toString(),
    val stationUuid: String,
    val driverIdentifier: String,
    val responseTopic: String? = null
)
