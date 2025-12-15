package com.chargepoint.csms.auth.listener

import com.chargepoint.csms.auth.service.AuthorizationProcessor
import com.chargepoint.csms.common.model.AuthorizationMessage
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class AuthorizationRequestListener(
    private val authorizationProcessor: AuthorizationProcessor
) {
    
    @KafkaListener(
        topics = ["authorization-requests"],
        groupId = "authentication-service-group"
    )
    fun handleAuthorizationRequest(@Payload message: AuthorizationMessage) {
        authorizationProcessor.processAuthorization(message)
    }
}
