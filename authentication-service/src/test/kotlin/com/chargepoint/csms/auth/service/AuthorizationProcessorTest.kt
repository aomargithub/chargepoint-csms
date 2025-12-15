package com.chargepoint.csms.auth.service

import com.chargepoint.csms.common.model.AuthorizationMessage
import com.chargepoint.csms.common.model.AuthorizationResponse
import com.chargepoint.csms.common.model.AuthorizationStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.kafka.core.KafkaTemplate

class AuthorizationProcessorTest {
    
    private lateinit var whitelistService: WhitelistService
    private lateinit var kafkaTemplate: KafkaTemplate<String, AuthorizationResponse>
    private lateinit var authorizationProcessor: AuthorizationProcessor
    
    @BeforeEach
    fun setUp() {
        whitelistService = WhitelistService()
        kafkaTemplate = mock()
        authorizationProcessor = AuthorizationProcessor(whitelistService, kafkaTemplate)
        
        whenever(kafkaTemplate.send(any(), any(), any())).thenReturn(mock())
    }
    
    @Test
    fun `should process authorization and return Accepted`() {
        val message = AuthorizationMessage(
            requestId = "test-request-1",
            stationUuid = "25aac66b-6051-478a-95e2-6d3aa343b025",
            driverIdentifier = "id12345678901234567890"
        )
        
        authorizationProcessor.processAuthorization(message)
        
        verify(kafkaTemplate).send(
            eq("authorization-responses"),
            eq("test-request-1"),
            argThat { it.authorizationStatus == AuthorizationStatus.Accepted }
        )
    }
    
    @Test
    fun `should return Invalid for identifier shorter than 20 characters`() {
        val message = AuthorizationMessage(
            requestId = "test-request-2",
            stationUuid = "25aac66b-6051-478a-95e2-6d3aa343b025",
            driverIdentifier = "short"
        )
        
        authorizationProcessor.processAuthorization(message)
        
        verify(kafkaTemplate).send(
            eq("authorization-responses"),
            eq("test-request-2"),
            argThat { it.authorizationStatus == AuthorizationStatus.Invalid }
        )
    }
    
    @Test
    fun `should return Invalid for identifier longer than 80 characters`() {
        val longId = "a".repeat(81)
        val message = AuthorizationMessage(
            requestId = "test-request-3",
            stationUuid = "25aac66b-6051-478a-95e2-6d3aa343b025",
            driverIdentifier = longId
        )
        
        authorizationProcessor.processAuthorization(message)
        
        verify(kafkaTemplate).send(
            eq("authorization-responses"),
            eq("test-request-3"),
            argThat { it.authorizationStatus == AuthorizationStatus.Invalid }
        )
    }
    
    @Test
    fun `should return Rejected for known but not allowed identifier`() {
        val message = AuthorizationMessage(
            requestId = "test-request-4",
            stationUuid = "25aac66b-6051-478a-95e2-6d3aa343b025",
            driverIdentifier = "rejected12345678901234567890"
        )
        
        authorizationProcessor.processAuthorization(message)
        
        verify(kafkaTemplate).send(
            eq("authorization-responses"),
            eq("test-request-4"),
            argThat { it.authorizationStatus == AuthorizationStatus.Rejected }
        )
    }
    
    @Test
    fun `should return Unknown for unknown identifier`() {
        val message = AuthorizationMessage(
            requestId = "test-request-5",
            stationUuid = "25aac66b-6051-478a-95e2-6d3aa343b025",
            driverIdentifier = "unknown12345678901234567890"
        )
        
        authorizationProcessor.processAuthorization(message)
        
        verify(kafkaTemplate).send(
            eq("authorization-responses"),
            eq("test-request-5"),
            argThat { it.authorizationStatus == AuthorizationStatus.Unknown }
        )
    }
}
