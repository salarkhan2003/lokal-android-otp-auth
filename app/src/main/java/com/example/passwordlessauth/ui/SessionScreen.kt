package com.example.passwordlessauth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

/**
 * Session screen composable that displays session information and live timer.
 * Shows session start time and continuously updating session duration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(
    email: String,
    sessionStartTime: Long,
    sessionDurationSeconds: Long,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top app bar with logout
        TopAppBar(
            title = { Text("Active Session") },
            actions = {
                IconButton(onClick = onLogout) {
                    Icon(
                        Icons.Default.ExitToApp,
                        contentDescription = "Logout",
                        tint = MaterialTheme.colorScheme.error
                    )
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
            // Welcome message
            Text(
                text = "Welcome!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "You are successfully logged in",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Session information card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Session Details",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Email
                    SessionInfoRow(
                        label = "Email",
                        value = email
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Session start time
                    SessionInfoRow(
                        label = "Session Started",
                        value = formatDateTime(sessionStartTime)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Live session duration
                    SessionInfoRow(
                        label = "Session Duration",
                        value = formatDuration(sessionDurationSeconds),
                        isHighlighted = true
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Logout button
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(
                    Icons.Default.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Logout",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Info text
            Text(
                text = "Your session is active and secure. The timer above shows how long you've been logged in.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

/**
 * Composable for displaying session information rows
 */
@Composable
private fun SessionInfoRow(
    label: String,
    value: String,
    isHighlighted: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = if (isHighlighted) 24.sp else 16.sp,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

/**
 * Format timestamp to readable date and time
 */
private fun formatDateTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm:ss", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

/**
 * Format duration in seconds to MM:SS format
 */
private fun formatDuration(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}