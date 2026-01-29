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
    // Collect state from ViewModel
    val state by viewModel.state.collectAsState()
    
    // Use rememberSaveable to survive configuration changes
    var savedState by rememberSaveable { mutableStateOf(state) }
    
    // Update saved state when state changes
    LaunchedEffect(state) {
        savedState = state
    }
    
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        when (savedState.currentScreen) {
            AuthScreen.Login -> {
                LoginScreen(
                    email = savedState.email,
                    isEmailValid = savedState.isEmailValid,
                    isGeneratingOtp = savedState.isGeneratingOtp,
                    errorMessage = savedState.errorMessage,
                    onEmailChanged = viewModel::onEmailChanged,
                    onGenerateOtp = viewModel::generateOtp,
                    onClearError = viewModel::clearError,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            
            AuthScreen.Otp -> {
                OtpScreen(
                    email = savedState.email,
                    otpInput = savedState.otpInput,
                    otpTimeRemaining = savedState.otpTimeRemaining,
                    otpAttemptsRemaining = savedState.otpAttemptsRemaining,
                    isOtpExpired = savedState.isOtpExpired,
                    otpError = savedState.otpError,
                    isValidatingOtp = savedState.isValidatingOtp,
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
                    email = savedState.email,
                    sessionStartTime = savedState.sessionStartTime,
                    sessionDurationSeconds = savedState.sessionDurationSeconds,
                    onLogout = viewModel::logout,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}