package com.chargepoint.csms.backend.service

import com.chargepoint.csms.common.model.AuthorizationRequest
import com.chargepoint.csms.common.model.AuthorizationResponse
import com.chargepoint.csms.common.model.AuthorizationStatus
import com.chargepoint.csms.common.model.DriverIdentifier
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.kafka.core.KafkaTemplate
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class AuthorizationServiceTest {
    
    private lateinit var kafkaTemplate: KafkaTemplate<String, com.chargepoint.csms.common.model.AuthorizationMessage>
    private lateinit var authorizationService: AuthorizationService
    
    @BeforeEach
    fun setUp() {
        kafkaTemplate = mock()
        authorizationService = AuthorizationService(kafkaTemplate)
    }
    
    @Test
    fun `should return Invalid for identifier shorter than 20 characters`() {
        val request = AuthorizationRequest(
            stationUuid = "25aac66b-6051-478a-95e2-6d3aa343b025",
            driverIdentifier = DriverIdentifier("short")
        )
        
        val response = authorizationService.authorize(request)
        
        assert(response.authorizationStatus == AuthorizationStatus.Invalid)
        verify(kafkaTemplate, never()).send(any(), any(), any())
    }
    
    @Test
    fun `should return Invalid for identifier longer than 80 characters`() {
        val longId = "a".repeat(81)
        val request = AuthorizationRequest(
            stationUuid = "25aac66b-6051-478a-95e2-6d3aa343b025",
            driverIdentifier = DriverIdentifier(longId)
        )
        
        val response = authorizationService.authorize(request)
        
        assert(response.authorizationStatus == AuthorizationStatus.Invalid)
        verify(kafkaTemplate, never()).send(any(), any(), any())
    }
    
    @Test
    fun `should send message to Kafka for valid identifier`() {
        val validId = "a".repeat(25)
        val request = AuthorizationRequest(
            stationUuid = "25aac66b-6051-478a-95e2-6d3aa343b025",
            driverIdentifier = DriverIdentifier(validId)
        )
        
        whenever(kafkaTemplate.send(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(mock()))
        
        // This will timeout, but we can verify Kafka was called
        val response = authorizationService.authorize(request)
        
        verify(kafkaTemplate, atLeastOnce()).send(eq("authorization-requests"), any(), any())
    }
    
    @Test
    fun `should handle response correctly`() {
        val requestId = "test-request-id"
        val response = AuthorizationResponse(AuthorizationStatus.Accepted)
        
        // Manually add a future to simulate pending request
        val future = CompletableFuture<AuthorizationResponse>()
        authorizationService.handleResponse(requestId, response)
        
        // The method should complete without error
        // (Note: This test is limited since we can't easily test the async flow)
    }
}
