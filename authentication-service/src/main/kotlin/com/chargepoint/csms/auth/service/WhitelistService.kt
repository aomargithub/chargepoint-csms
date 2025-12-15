package com.chargepoint.csms.auth.service

import com.chargepoint.csms.common.model.AuthorizationStatus
import org.springframework.stereotype.Service

data class IdentifierRecord(
    val id: String,
    val allowedToCharge: Boolean
)

@Service
class WhitelistService {
    
    // In-memory whitelist storage
    private val whitelist = mutableMapOf<String, IdentifierRecord>()
    
    init {
        // Initialize with some sample data (all identifiers must be 20-80 characters)
        // Note: The sample "id1234" from requirements is only 6 chars, so using valid-length alternatives
        whitelist["id12345678901234567890"] = IdentifierRecord("id12345678901234567890", allowedToCharge = true)
        whitelist["allowed12345678901234567890"] = IdentifierRecord("allowed12345678901234567890", allowedToCharge = true)
        whitelist["rejected12345678901234567890"] = IdentifierRecord("rejected12345678901234567890", allowedToCharge = false)
    }
    
    fun checkAuthorization(identifier: String): AuthorizationStatus {
        val record = whitelist[identifier]
        return when {
            record == null -> AuthorizationStatus.Unknown
            record.allowedToCharge -> AuthorizationStatus.Accepted
            else -> AuthorizationStatus.Rejected
        }
    }
    
    fun addIdentifier(id: String, allowedToCharge: Boolean) {
        whitelist[id] = IdentifierRecord(id, allowedToCharge)
    }
    
    fun removeIdentifier(id: String) {
        whitelist.remove(id)
    }
}
