package com.example.passwordlessauth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Login screen composable that handles email input and OTP generation.
 * Follows Compose best practices with proper state hoisting and no business logic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    email: String,
    isEmailValid: Boolean,
    isGeneratingOtp: Boolean,
    errorMessage: String?,
    onEmailChanged: (String) -> Unit,
    onGenerateOtp: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Clear error when email changes
    LaunchedEffect(email) {
        if (errorMessage != null) {
            onClearError()
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title
        Text(
            text = "Welcome",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Enter your email to get started",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Email input field
        OutlinedTextField(
            value = email,
            onValueChange = { newValue ->
                onEmailChanged(newValue)
            },
            label = { Text("Email Address") },
            placeholder = { Text("Enter your email") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    if (isEmailValid && !isGeneratingOtp) {
                        onGenerateOtp()
                    }
                }
            ),
            singleLine = true,
            isError = errorMessage != null,
            supportingText = if (errorMessage != null) {
                { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Generate OTP button
        Button(
            onClick = onGenerateOtp,
            enabled = isEmailValid && !isGeneratingOtp,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (isGeneratingOtp) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generating OTP...")
            } else {
                Text(
                    text = "Get OTP",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Info text
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Text(
                text = "We'll send you a 6-digit code that expires in 60 seconds. You have 3 attempts to enter it correctly.",
                modifier = Modifier.padding(16.dp),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}