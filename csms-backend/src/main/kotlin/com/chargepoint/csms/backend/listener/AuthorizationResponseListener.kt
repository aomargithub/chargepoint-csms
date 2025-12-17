package com.chargepoint.csms.backend.listener

import com.chargepoint.csms.backend.service.AuthorizationService
import com.chargepoint.csms.common.model.AuthorizationResponse
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class AuthorizationResponseListener(
    private val authorizationService: AuthorizationService
) {
    
    @KafkaListener(
        topics = ["authorization-responses"],
        groupId = "csms-backend-group"
    )
    fun handleAuthorizationResponse(
        @Payload response: AuthorizationResponse,
        @Header(KafkaHeaders.RECEIVED_KEY) requestId: String
    ) {
        authorizationService.handleResponse(requestId, response)
    }
}
