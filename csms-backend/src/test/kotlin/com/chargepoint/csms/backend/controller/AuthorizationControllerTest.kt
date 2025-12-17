package com.chargepoint.csms.backend.controller

import com.chargepoint.csms.backend.service.AuthorizationService
import com.chargepoint.csms.common.model.AuthorizationRequest
import com.chargepoint.csms.common.model.AuthorizationResponse
import com.chargepoint.csms.common.model.AuthorizationStatus
import com.chargepoint.csms.common.model.DriverIdentifier
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class AuthorizationControllerTest {
    
    private val authorizationService: AuthorizationService = mock()
    private val controller = AuthorizationController(authorizationService)
    
    @Test
    fun `should return authorization response`() {
        val request = AuthorizationRequest(
            stationUuid = "25aac66b-6051-478a-95e2-6d3aa343b025",
            driverIdentifier = DriverIdentifier("id12345678901234567890")
        )
        val expectedResponse = AuthorizationResponse(AuthorizationStatus.Accepted)
        
        whenever(authorizationService.authorize(request)).thenReturn(expectedResponse)
        
        val result: ResponseEntity<AuthorizationResponse> = controller.authorize(request)
        
        assert(result.statusCode == HttpStatus.OK)
        assert(result.body?.authorizationStatus == AuthorizationStatus.Accepted)
        verify(authorizationService).authorize(request)
    }
}
