package com.example.passwordlessauth.data

import timber.log.Timber
import kotlin.random.Random

/**
 * Manages OTP generation, validation, and storage per email address.
 * Uses a Map<String, OtpData> to store OTP data per email.
 * 
 * This class handles:
 * - OTP generation (6-digit random numbers)
 * - OTP validation with attempt tracking
 * - OTP expiry (60 seconds)
 * - Invalidating old OTPs when generating new ones
 */
class OtpManager {
    
    // Thread-safe storage of OTP data per email
    private val otpStorage = mutableMapOf<String, OtpData>()
    
    companion object {
        private const val OTP_EXPIRY_DURATION_MS = 60_000L // 60 seconds
        private const val MAX_ATTEMPTS = 3
        private const val OTP_LENGTH = 6
    }
    
    /**
     * Generates a new 6-digit OTP for the given email.
     * Invalidates any existing OTP for this email and resets attempts.
     * 
     * @param email The email address to generate OTP for
     * @return The generated OTP string
     */
    fun generateOtp(email: String): String {
        val otp = generateRandomOtp()
        val currentTime = System.currentTimeMillis()
        val expiryTime = currentTime + OTP_EXPIRY_DURATION_MS
        
        val otpData = OtpData(
            otp = otp,
            expiryTime = expiryTime,
            attemptsRemaining = MAX_ATTEMPTS,
            generatedTime = currentTime
        )
        
        // Store the new OTP data, replacing any existing one
        otpStorage[email] = otpData
        
        Timber.d("OTP generated for email: $email, expires in ${OTP_EXPIRY_DURATION_MS / 1000} seconds")
        
        return otp
    }
    
    /**
     * Validates the provided OTP against the stored OTP for the given email.
     * 
     * @param email The email address
     * @param inputOtp The OTP entered by the user
     * @return OtpValidationResult indicating success or failure reason
     */
    fun validateOtp(email: String, inputOtp: String): OtpValidationResult {
        val otpData = otpStorage[email]
            ?: return OtpValidationResult.NoOtpFound
        
        // Check if OTP has expired
        if (otpData.isExpired()) {
            Timber.d("OTP validation failed for $email: OTP expired")
            return OtpValidationResult.Expired
        }
        
        // Check if attempts are exhausted
        if (!otpData.hasAttemptsRemaining()) {
            Timber.d("OTP validation failed for $email: No attempts remaining")
            return OtpValidationResult.AttemptsExhausted
        }
        
        // Validate the OTP
        if (otpData.otp == inputOtp) {
            // Success - remove the OTP data as it's been used
            otpStorage.remove(email)
            Timber.d("OTP validation success for $email")
            return OtpValidationResult.Success
        } else {
            // Wrong OTP - decrement attempts
            val updatedOtpData = otpData.decrementAttempts()
            otpStorage[email] = updatedOtpData
            
            Timber.d("OTP validation failure for $email: Wrong OTP, ${updatedOtpData.attemptsRemaining} attempts remaining")
            return OtpValidationResult.WrongOtp(updatedOtpData.attemptsRemaining)
        }
    }
    
    /**
     * Gets the current OTP data for an email (for UI state purposes)
     */
    fun getOtpData(email: String): OtpData? = otpStorage[email]
    
    /**
     * Clears all OTP data (useful for logout/reset)
     */
    fun clearAllOtpData() {
        otpStorage.clear()
        Timber.d("All OTP data cleared")
    }
    
    /**
     * Generates a random 6-digit OTP
     */
    private fun generateRandomOtp(): String {
        return (100000..999999).random().toString()
    }
}

/**
 * Sealed class representing the result of OTP validation
 */
sealed class OtpValidationResult {
    object Success : OtpValidationResult()
    object NoOtpFound : OtpValidationResult()
    object Expired : OtpValidationResult()
    object AttemptsExhausted : OtpValidationResult()
    data class WrongOtp(val attemptsRemaining: Int) : OtpValidationResult()
}