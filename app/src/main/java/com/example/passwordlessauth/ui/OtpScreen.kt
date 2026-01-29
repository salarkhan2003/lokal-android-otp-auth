package com.example.passwordlessauth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * OTP verification screen composable.
 * Handles OTP input, validation, timer display, and resend functionality.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpScreen(
    email: String,
    otpInput: String,
    otpTimeRemaining: Int,
    otpAttemptsRemaining: Int,
    isOtpExpired: Boolean,
    otpError: String?,
    isValidatingOtp: Boolean,
    onOtpChanged: (String) -> Unit,
    onValidateOtp: () -> Unit,
    onResendOtp: () -> Unit,
    onNavigateBack: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Clear error when OTP input changes
    LaunchedEffect(otpInput) {
        if (otpError != null) {
            onClearError()
        }
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top app bar
        TopAppBar(
            title = { Text("Verify OTP") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title and email
            Text(
                text = "Enter Verification Code",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "We've sent a 6-digit code to",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = email,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Timer display
            if (!isOtpExpired && otpTimeRemaining > 0) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (otpTimeRemaining <= 10) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        }
                    )
                ) {
                    Text(
                        text = "Time remaining: ${formatTime(otpTimeRemaining)}",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (otpTimeRemaining <= 10) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // OTP input field
            OutlinedTextField(
                value = otpInput,
                onValueChange = onOtpChanged,
                label = { Text("6-Digit Code") },
                placeholder = { Text("000000") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done,
                    autoCorrect = false
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (otpInput.length == 6 && !isValidatingOtp && !isOtpExpired) {
                            onValidateOtp()
                        }
                    }
                ),
                singleLine = true,
                maxLines = 1,
                isError = otpError != null,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Error message or attempts remaining
            Spacer(modifier = Modifier.height(4.dp))
            if (otpError != null) {
                Text(
                    text = otpError,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = "$otpAttemptsRemaining attempts remaining",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Verify button
            Button(
                onClick = onValidateOtp,
                enabled = otpInput.length == 6 && !isValidatingOtp && !isOtpExpired && otpAttemptsRemaining > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (isValidatingOtp) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verifying...")
                } else {
                    Text(
                        text = "Verify Code",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Resend button
            if (isOtpExpired || otpTimeRemaining == 0 || otpAttemptsRemaining == 0) {
                OutlinedButton(
                    onClick = onResendOtp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Resend OTP")
                }
            } else {
                TextButton(
                    onClick = onResendOtp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Didn't receive the code? Resend")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Security Information",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Code expires in 60 seconds\n• Maximum 3 attempts allowed\n• Generating a new code invalidates the previous one",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Format seconds into MM:SS format
 */
private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}