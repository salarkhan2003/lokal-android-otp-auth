package com.example.passwordlessauth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passwordlessauth.analytics.AnalyticsLogger
import com.example.passwordlessauth.data.OtpManager
import com.example.passwordlessauth.data.OtpValidationResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.regex.Pattern

/**
 * ViewModel for managing authentication state and business logic.
 * Implements clean architecture with immutable state and one-way data flow.
 * All business logic is contained here, not in Composables.
 */
class AuthViewModel : ViewModel() {
    
    private val otpManager = OtpManager()
    
    // Immutable state flow
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()
    
    // Coroutine jobs for managing timers
    private var otpTimerJob: Job? = null
    private var sessionTimerJob: Job? = null
    
    companion object {
        private const val OTP_DURATION_SECONDS = 60
        private val EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )
    }
    
    /**
     * Handle email input changes
     */
    fun onEmailChanged(email: String) {
        val isValid = isValidEmail(email)
        _state.value = _state.value.copy(
            email = email,
            isEmailValid = isValid,
            errorMessage = null
        )
    }
    
    /**
     * Generate OTP for the entered email
     */
    fun generateOtp() {
        val currentState = _state.value
        if (!currentState.isEmailValid) {
            _state.value = currentState.copy(errorMessage = "Please enter a valid email address")
            return
        }
        
        _state.value = currentState.copy(isGeneratingOtp = true, errorMessage = null)
        
        viewModelScope.launch {
            try {
                // Simulate network delay for OTP generation
                delay(500)
                
                val otp = otpManager.generateOtp(currentState.email)
                AnalyticsLogger.logOtpGenerated(currentState.email)
                
                // Navigate to OTP screen and start timer
                _state.value = _state.value.copy(
                    currentScreen = AuthScreen.Otp,
                    isGeneratingOtp = false,
                    otpInput = "",
                    otpTimeRemaining = OTP_DURATION_SECONDS,
                    otpAttemptsRemaining = 3,
                    isOtpExpired = false,
                    otpError = null,
                    generatedOtp = otp // Store the OTP for display
                )
                
                startOtpTimer()
                
                // In a real app, you wouldn't log the actual OTP
                Timber.d("Generated OTP for testing: $otp")
                
            } catch (e: Exception) {
                Timber.e(e, "Error generating OTP")
                _state.value = _state.value.copy(
                    isGeneratingOtp = false,
                    errorMessage = "Failed to generate OTP. Please try again."
                )
            }
        }
    }
    
    /**
     * Handle OTP input changes
     */
    fun onOtpChanged(otp: String) {
        // Only allow numeric input and max 6 digits
        val filteredOtp = otp.filter { it.isDigit() }.take(6)
        _state.value = _state.value.copy(
            otpInput = filteredOtp,
            otpError = null
        )
    }
    
    /**
     * Validate the entered OTP
     */
    fun validateOtp() {
        val currentState = _state.value
        if (currentState.otpInput.length != 6) {
            _state.value = currentState.copy(otpError = "Please enter a 6-digit OTP")
            return
        }
        
        _state.value = currentState.copy(isValidatingOtp = true, otpError = null)
        
        viewModelScope.launch {
            try {
                // Simulate network delay for OTP validation
                delay(300)
                
                val result = otpManager.validateOtp(currentState.email, currentState.otpInput)
                
                when (result) {
                    is OtpValidationResult.Success -> {
                        AnalyticsLogger.logOtpValidationSuccess(currentState.email)
                        AnalyticsLogger.logSessionStart(currentState.email)
                        
                        // Stop OTP timer and navigate to session screen
                        stopOtpTimer()
                        val sessionStartTime = System.currentTimeMillis()
                        
                        _state.value = _state.value.copy(
                            currentScreen = AuthScreen.Session,
                            isValidatingOtp = false,
                            sessionStartTime = sessionStartTime,
                            sessionDurationSeconds = 0L
                        )
                        
                        startSessionTimer()
                    }
                    
                    is OtpValidationResult.WrongOtp -> {
                        AnalyticsLogger.logOtpValidationFailure(
                            currentState.email, 
                            "Wrong OTP", 
                            result.attemptsRemaining
                        )
                        
                        if (result.attemptsRemaining == 0) {
                            // No attempts remaining - timeout
                            _state.value = _state.value.copy(
                                isValidatingOtp = false,
                                otpAttemptsRemaining = 0,
                                otpError = "Maximum attempts exceeded. Please generate a new OTP.",
                                otpInput = ""
                            )
                            stopOtpTimer()
                        } else {
                            _state.value = _state.value.copy(
                                isValidatingOtp = false,
                                otpAttemptsRemaining = result.attemptsRemaining,
                                otpError = "Incorrect OTP. ${result.attemptsRemaining} attempts remaining.",
                                otpInput = ""
                            )
                        }
                    }
                    
                    is OtpValidationResult.Expired -> {
                        AnalyticsLogger.logOtpValidationFailure(currentState.email, "OTP Expired")
                        _state.value = _state.value.copy(
                            isValidatingOtp = false,
                            isOtpExpired = true,
                            otpError = "OTP has expired. Please generate a new one."
                        )
                        stopOtpTimer()
                    }
                    
                    is OtpValidationResult.AttemptsExhausted -> {
                        AnalyticsLogger.logOtpValidationFailure(currentState.email, "Attempts Exhausted")
                        _state.value = _state.value.copy(
                            isValidatingOtp = false,
                            otpError = "Maximum attempts exceeded. Please generate a new OTP."
                        )
                        stopOtpTimer()
                    }
                    
                    is OtpValidationResult.NoOtpFound -> {
                        AnalyticsLogger.logOtpValidationFailure(currentState.email, "No OTP Found")
                        _state.value = _state.value.copy(
                            isValidatingOtp = false,
                            otpError = "No OTP found. Please generate a new one."
                        )
                    }
                }
                
            } catch (e: Exception) {
                Timber.e(e, "Error validating OTP")
                _state.value = _state.value.copy(
                    isValidatingOtp = false,
                    otpError = "Failed to validate OTP. Please try again."
                )
            }
        }
    }
    
    /**
     * Resend OTP (generate new one)
     */
    fun resendOtp() {
        stopOtpTimer()
        generateOtp()
    }
    
    /**
     * Logout and reset to initial state
     */
    fun logout() {
        val currentState = _state.value
        val sessionDuration = if (currentState.sessionStartTime > 0) {
            (System.currentTimeMillis() - currentState.sessionStartTime) / 1000
        } else 0L
        
        AnalyticsLogger.logLogout(currentState.email, sessionDuration)
        
        // Stop all timers
        stopOtpTimer()
        stopSessionTimer()
        
        // Clear OTP data
        otpManager.clearAllOtpData()
        
        // Reset to initial state
        _state.value = AuthState()
    }
    
    /**
     * Navigate back to login screen
     */
    fun navigateToLogin() {
        stopOtpTimer()
        _state.value = _state.value.copy(
            currentScreen = AuthScreen.Login,
            otpInput = "",
            otpError = null,
            errorMessage = null,
            generatedOtp = "" // Clear the generated OTP
        )
    }
    
    /**
     * Clear error messages
     */
    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null, otpError = null)
    }
    
    /**
     * Start the OTP countdown timer
     */
    private fun startOtpTimer() {
        otpTimerJob?.cancel()
        otpTimerJob = viewModelScope.launch {
            repeat(OTP_DURATION_SECONDS) { second ->
                val remaining = OTP_DURATION_SECONDS - second
                _state.value = _state.value.copy(otpTimeRemaining = remaining)
                delay(1000)
            }
            
            // Timer expired
            _state.value = _state.value.copy(
                otpTimeRemaining = 0,
                isOtpExpired = true,
                otpError = "OTP has expired. Please generate a new one."
            )
        }
    }
    
    /**
     * Start the session duration timer
     */
    private fun startSessionTimer() {
        sessionTimerJob?.cancel()
        sessionTimerJob = viewModelScope.launch {
            while (true) {
                val currentState = _state.value
                if (currentState.sessionStartTime > 0) {
                    val duration = (System.currentTimeMillis() - currentState.sessionStartTime) / 1000
                    _state.value = currentState.copy(sessionDurationSeconds = duration)
                }
                delay(1000)
            }
        }
    }
    
    /**
     * Stop the OTP timer
     */
    private fun stopOtpTimer() {
        otpTimerJob?.cancel()
        otpTimerJob = null
    }
    
    /**
     * Stop the session timer
     */
    private fun stopSessionTimer() {
        sessionTimerJob?.cancel()
        sessionTimerJob = null
    }
    
    /**
     * Validate email format
     */
    private fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && EMAIL_PATTERN.matcher(email).matches()
    }
    
    override fun onCleared() {
        super.onCleared()
        stopOtpTimer()
        stopSessionTimer()
    }
}