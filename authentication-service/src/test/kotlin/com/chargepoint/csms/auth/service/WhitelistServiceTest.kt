package com.chargepoint.csms.auth.service

import com.chargepoint.csms.common.model.AuthorizationStatus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class WhitelistServiceTest {
    
    private lateinit var whitelistService: WhitelistService
    
    @BeforeEach
    fun setUp() {
        whitelistService = WhitelistService()
    }
    
    @Test
    fun `should return Accepted for allowed identifier`() {
        val status = whitelistService.checkAuthorization("id12345678901234567890")
        assertEquals(AuthorizationStatus.Accepted, status)
    }
    
    @Test
    fun `should return Rejected for known but not allowed identifier`() {
        val status = whitelistService.checkAuthorization("rejected12345678901234567890")
        assertEquals(AuthorizationStatus.Rejected, status)
    }
    
    @Test
    fun `should return Unknown for unknown identifier`() {
        val status = whitelistService.checkAuthorization("unknown12345678901234567890")
        assertEquals(AuthorizationStatus.Unknown, status)
    }
    
    @Test
    fun `should add new identifier`() {
        val newId = "newid12345678901234567890"
        whitelistService.addIdentifier(newId, allowedToCharge = true)
        
        val status = whitelistService.checkAuthorization(newId)
        assertEquals(AuthorizationStatus.Accepted, status)
    }
    
    @Test
    fun `should remove identifier`() {
        val id = "id12345678901234567890"
        whitelistService.removeIdentifier(id)
        
        val status = whitelistService.checkAuthorization(id)
        assertEquals(AuthorizationStatus.Unknown, status)
    }
}
