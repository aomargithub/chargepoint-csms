package com.chargepoint.csms.backend.controller

import com.chargepoint.csms.backend.service.AuthorizationService
import com.chargepoint.csms.common.model.AuthorizationRequest
import com.chargepoint.csms.common.model.AuthorizationResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/authorization")
class AuthorizationController(
    private val authorizationService: AuthorizationService
) {
    
    @PostMapping
    fun authorize(@RequestBody request: AuthorizationRequest): ResponseEntity<AuthorizationResponse> {
        val response = authorizationService.authorize(request)
        return ResponseEntity.ok(response)
    }
}
