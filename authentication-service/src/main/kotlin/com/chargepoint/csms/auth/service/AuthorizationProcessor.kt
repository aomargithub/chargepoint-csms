package com.chargepoint.csms.auth.service

import com.chargepoint.csms.common.model.AuthorizationMessage
import com.chargepoint.csms.common.model.AuthorizationResponse
import com.chargepoint.csms.common.model.AuthorizationStatus
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class AuthorizationProcessor(
    private val whitelistService: WhitelistService,
    private val kafkaTemplate: KafkaTemplate<String, AuthorizationResponse>
) {
    companion object {
        const val AUTHORIZATION_RESPONSE_TOPIC = "authorization-responses"
    }
    
    fun processAuthorization(message: AuthorizationMessage) {
        // Validate identifier length
        val identifier = message.driverIdentifier
        val status = if (identifier.length < 20 || identifier.length > 80) {
            AuthorizationStatus.Invalid
        } else {
            whitelistService.checkAuthorization(identifier)
        }
        
        val response = AuthorizationResponse(status)
        
        // Send response back to Kafka
        val responseTopic = message.responseTopic ?: AUTHORIZATION_RESPONSE_TOPIC
        kafkaTemplate.send(responseTopic, message.requestId, response)
    }
}
