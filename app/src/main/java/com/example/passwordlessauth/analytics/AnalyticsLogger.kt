package com.example.passwordlessauth.analytics

import timber.log.Timber

/**
 * Analytics logger that uses Timber for logging authentication events.
 * In a production app, this would integrate with analytics services like Firebase Analytics,
 * Mixpanel, or other tracking solutions.
 */
object AnalyticsLogger {
    
    /**
     * Log OTP generation event
     */
    fun logOtpGenerated(email: String) {
        Timber.i("Analytics: OTP generated for email: ${email.take(3)}***")
    }
    
    /**
     * Log successful OTP validation
     */
    fun logOtpValidationSuccess(email: String) {
        Timber.i("Analytics: OTP validation success for email: ${email.take(3)}***")
    }
    
    /**
     * Log failed OTP validation with reason
     */
    fun logOtpValidationFailure(email: String, reason: String, attemptsRemaining: Int? = null) {
        val message = if (attemptsRemaining != null) {
            "Analytics: OTP validation failure for email: ${email.take(3)}*** - Reason: $reason, Attempts remaining: $attemptsRemaining"
        } else {
            "Analytics: OTP validation failure for email: ${email.take(3)}*** - Reason: $reason"
        }
        Timber.w(message)
    }
    
    /**
     * Log user logout event
     */
    fun logLogout(email: String, sessionDurationSeconds: Long) {
        Timber.i("Analytics: User logout - Email: ${email.take(3)}***, Session duration: ${sessionDurationSeconds}s")
    }
    
    /**
     * Log session start
     */
    fun logSessionStart(email: String) {
        Timber.i("Analytics: Session started for email: ${email.take(3)}***")
    }
}