package com.example.passwordlessauth.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data class representing OTP information for a specific email
 * 
 * @param otp The 6-digit OTP code
 * @param expiryTime The timestamp when this OTP expires (System.currentTimeMillis())
 * @param attemptsRemaining Number of validation attempts remaining (max 3)
 * @param generatedTime The timestamp when this OTP was generated
 */
@Parcelize
data class OtpData(
    val otp: String,
    val expiryTime: Long,
    val attemptsRemaining: Int = 3,
    val generatedTime: Long = System.currentTimeMillis()
) : Parcelable {
    
    /**
     * Check if this OTP has expired
     */
    fun isExpired(): Boolean = System.currentTimeMillis() > expiryTime
    
    /**
     * Check if this OTP has attempts remaining
     */
    fun hasAttemptsRemaining(): Boolean = attemptsRemaining > 0
    
    /**
     * Create a new OtpData with one less attempt
     */
    fun decrementAttempts(): OtpData = copy(attemptsRemaining = attemptsRemaining - 1)
}