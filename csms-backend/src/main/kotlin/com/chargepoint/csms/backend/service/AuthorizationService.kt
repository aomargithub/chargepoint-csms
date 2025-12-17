package com.chargepoint.csms.backend.service

import com.chargepoint.csms.common.model.AuthorizationMessage
import com.chargepoint.csms.common.model.AuthorizationRequest
import com.chargepoint.csms.common.model.AuthorizationResponse
import com.chargepoint.csms.common.model.AuthorizationStatus
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Service
class AuthorizationService(
    private val kafkaTemplate: KafkaTemplate<String, AuthorizationMessage>
) {
    companion object {
        const val AUTHORIZATION_REQUEST_TOPIC = "authorization-requests"
        const val AUTHORIZATION_RESPONSE_TOPIC = "authorization-responses"
        const val RESPONSE_TIMEOUT_SECONDS = 5L
    }
    
    private val pendingRequests = ConcurrentHashMap<String, CompletableFuture<AuthorizationResponse>>()
    
    fun authorize(request: AuthorizationRequest): AuthorizationResponse {
        // Validate identifier length
        val identifier = request.driverIdentifier.id
        if (identifier.length < 20 || identifier.length > 80) {
            return AuthorizationResponse(AuthorizationStatus.Invalid)
        }
        
        // Create message and send to Kafka
        val message = AuthorizationMessage(
            stationUuid = request.stationUuid,
            driverIdentifier = identifier,
            responseTopic = AUTHORIZATION_RESPONSE_TOPIC
        )
        
        // Create future for async response
        val future = CompletableFuture<AuthorizationResponse>()
        pendingRequests[message.requestId] = future
        
        // Send to Kafka
        kafkaTemplate.send(AUTHORIZATION_REQUEST_TOPIC, message.requestId, message)
        
        // Wait for response (with timeout)
        return try {
            future.get(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        } catch (e: Exception) {
            pendingRequests.remove(message.requestId)
            AuthorizationResponse(AuthorizationStatus.Unknown)
        }
    }
    
    fun handleResponse(requestId: String, response: AuthorizationResponse) {
        pendingRequests[requestId]?.complete(response)
        pendingRequests.remove(requestId)
    }
}
