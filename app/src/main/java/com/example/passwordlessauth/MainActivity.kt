package com.example.passwordlessauth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passwordlessauth.ui.LoginScreen
import com.example.passwordlessauth.ui.OtpScreen
import com.example.passwordlessauth.ui.SessionScreen
import com.example.passwordlessauth.ui.theme.PasswordlessAuthTheme
import com.example.passwordlessauth.viewmodel.AuthScreen
import com.example.passwordlessauth.viewmodel.AuthViewModel

/**
 * Main activity that hosts the authentication flow.
 * Uses Jetpack Compose with proper state management and navigation.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            PasswordlessAuthTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AuthApp()
                }
            }
        }
    }
}

/**
 * Main app composable that manages navigation between screens
 */
@Composable
fun AuthApp(
    viewModel: AuthViewModel = viewModel()
) {
    // Collect state from ViewModel directly
    val state by viewModel.state.collectAsState()
    
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        when (state.currentScreen) {
            AuthScreen.Login -> {
                LoginScreen(
                    email = state.email,
                    isEmailValid = state.isEmailValid,
                    isGeneratingOtp = state.isGeneratingOtp,
                    errorMessage = state.errorMessage,
                    onEmailChanged = viewModel::onEmailChanged,
                    onGenerateOtp = viewModel::generateOtp,
                    onClearError = viewModel::clearError,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            
            AuthScreen.Otp -> {
                OtpScreen(
                    email = state.email,
                    otpInput = state.otpInput,
                    otpTimeRemaining = state.otpTimeRemaining,
                    otpAttemptsRemaining = state.otpAttemptsRemaining,
                    isOtpExpired = state.isOtpExpired,
                    otpError = state.otpError,
                    isValidatingOtp = state.isValidatingOtp,
                    generatedOtp = state.generatedOtp, // Pass the generated OTP
                    onOtpChanged = viewModel::onOtpChanged,
                    onValidateOtp = viewModel::validateOtp,
                    onResendOtp = viewModel::resendOtp,
                    onNavigateBack = viewModel::navigateToLogin,
                    onClearError = viewModel::clearError,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            
            AuthScreen.Session -> {
                SessionScreen(
                    email = state.email,
                    sessionStartTime = state.sessionStartTime,
                    sessionDurationSeconds = state.sessionDurationSeconds,
                    onLogout = viewModel::logout,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}