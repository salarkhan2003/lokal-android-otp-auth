package com.example.passwordlessauth.viewmodel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Immutable UI state for the authentication flow.
 * Follows one-way data flow pattern with clear state representation.
 */
@Parcelize
data class AuthState(
    // Current screen state
    val currentScreen: AuthScreen = AuthScreen.Login,
    
    // Login screen state
    val email: String = "",
    val isEmailValid: Boolean = false,
    
    // OTP screen state
    val otpInput: String = "",
    val otpTimeRemaining: Int = 0, // seconds
    val otpAttemptsRemaining: Int = 3,
    val isOtpExpired: Boolean = false,
    val otpError: String? = null,
    val generatedOtp: String = "", // For displaying the OTP to user (testing purposes)
    
    // Session screen state
    val sessionStartTime: Long = 0L,
    val sessionDurationSeconds: Long = 0L,
    
    // Loading states
    val isGeneratingOtp: Boolean = false,
    val isValidatingOtp: Boolean = false,
    
    // Error states
    val errorMessage: String? = null
) : Parcelable

/**
 * Represents the different screens in the authentication flow
 */
@Parcelize
enum class AuthScreen : Parcelable {
    Login,
    Otp,
    Session
}