# Passwordless Authentication Android App

A production-quality Android application implementing passwordless Email + OTP authentication using Kotlin and Jetpack Compose, following clean architecture principles and Android best practices.

## Features

- **Email-based Authentication**: Enter email to receive OTP
- **Local OTP Generation**: 6-digit OTP generated locally with 60-second expiry
- **Attempt Management**: Maximum 3 validation attempts per OTP
- **Session Tracking**: Live session timer showing duration in MM:SS format
- **State Persistence**: Survives screen rotations and configuration changes
- **Clean Architecture**: Proper separation of concerns with ViewModel pattern

## Architecture

The app follows clean architecture principles with clear separation of layers:

```
app/
├── data/                    # Data layer
│   ├── OtpData.kt          # Data model for OTP information
│   └── OtpManager.kt       # Business logic for OTP operations
├── viewmodel/              # Presentation layer
│   ├── AuthState.kt        # Immutable UI state
│   └── AuthViewModel.kt    # State management and business logic
├── ui/                     # UI layer
│   ├── LoginScreen.kt      # Email input screen
│   ├── OtpScreen.kt        # OTP verification screen
│   ├── SessionScreen.kt    # Session tracking screen
│   └── theme/              # Material Design 3 theming
├── analytics/              # Analytics layer
│   └── AnalyticsLogger.kt  # Logging with Timber integration
└── MainActivity.kt         # Main activity with navigation
```

## OTP Logic Implementation

### Data Structure
The app uses `Map<String, OtpData>` to store OTP information per email address:

```kotlin
data class OtpData(
    val otp: String,                    // 6-digit OTP code
    val expiryTime: Long,              // Expiry timestamp
    val attemptsRemaining: Int = 3,    // Validation attempts left
    val generatedTime: Long            // Generation timestamp
)
```

**Why this structure?**
- **Map<String, OtpData>**: Allows multiple users to have active OTPs simultaneously
- **Immutable data class**: Ensures thread safety and predictable state changes
- **Timestamp-based expiry**: More reliable than countdown timers
- **Attempt tracking**: Prevents brute force attacks

### OTP Generation & Expiry Logic

1. **Generation**: 
   - Creates random 6-digit number (100000-999999)
   - Sets expiry time to current time + 60 seconds
   - Resets attempts to 3
   - **Invalidates any existing OTP** for the same email

2. **Expiry Handling**:
   - Checks `System.currentTimeMillis() > expiryTime`
   - Timer updates UI every second
   - Expired OTPs cannot be validated

3. **Attempt Logic**:
   - Each wrong OTP decrements attempts
   - Zero attempts = OTP becomes invalid
   - Successful validation removes OTP from storage

### Edge Cases Handled

- **Screen Rotation**: State preserved using `rememberSaveable` and `Parcelable`
- **Expired OTP**: Clear error message with resend option
- **Wrong OTP**: Shows remaining attempts, clears input field
- **No Attempts Left**: Forces OTP regeneration
- **Multiple Resends**: Each new OTP invalidates the previous one
- **App Backgrounding**: Timers continue running via coroutines

## Technical Implementation

### State Management
- **Immutable State**: `AuthState` data class with all UI state
- **One-way Data Flow**: UI events → ViewModel → State updates → UI recomposition
- **No Global State**: All state contained in ViewModel
- **StateFlow**: Reactive state updates with proper lifecycle handling

### Coroutines Usage
- **OTP Timer**: `viewModelScope.launch` with 1-second intervals
- **Session Timer**: Continuous coroutine for live duration updates
- **Proper Cleanup**: Jobs cancelled in `onCleared()`
- **No Blocking Calls**: All operations use `delay()` instead of `Thread.sleep()`

### Compose Best Practices
- **State Hoisting**: All state managed in ViewModel
- **remember/rememberSaveable**: Proper state preservation
- **LaunchedEffect**: Side effects for timer management
- **No Business Logic in Composables**: Pure UI functions only

## Timber Integration

### Why Timber?
Timber was chosen for logging because:
- **Zero-cost in production**: Automatically removes debug logs in release builds
- **Structured logging**: Consistent log format across the app
- **Easy integration**: Single line initialization
- **Extensible**: Can easily add crash reporting or analytics

### Logged Events
The app logs these critical authentication events:

1. **OTP Generated**: `AnalyticsLogger.logOtpGenerated(email)`
2. **OTP Validation Success**: `AnalyticsLogger.logOtpValidationSuccess(email)`
3. **OTP Validation Failure**: `AnalyticsLogger.logOtpValidationFailure(email, reason, attempts)`
4. **User Logout**: `AnalyticsLogger.logLogout(email, sessionDuration)`

### Privacy Considerations
- Email addresses are masked in logs (only first 3 characters shown)
- Actual OTP codes are never logged in production
- Session durations tracked for analytics

## Dependencies

```kotlin
// Core Android & Compose
implementation("androidx.core:core-ktx:1.12.0")
implementation("androidx.activity:activity-compose:1.8.2")
implementation("androidx.compose.material3:material3")

// ViewModel & State Management
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

// Logging
implementation("com.jakewharton.timber:timber:5.0.1")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

## Setup & Running

1. **Clone the repository**
2. **Open in Android Studio**
3. **Sync Gradle files**
4. **Run the app** - No additional setup required

The project is configured to build and run immediately without any external dependencies or API keys.

## Testing the App

1. **Enter a valid email** on the login screen
2. **Generate OTP** - Check logcat for the generated OTP (debug builds only)
3. **Enter the OTP** within 60 seconds
4. **View session screen** with live timer
5. **Test edge cases**:
   - Let OTP expire
   - Enter wrong OTP multiple times
   - Rotate screen during different states
   - Resend OTP functionality

## AI Assistance vs Manual Implementation

### What was implemented manually:
- **Architecture design**: Clean separation of concerns
- **State management**: Immutable state with proper flow
- **OTP logic**: Expiry, attempts, and validation rules
- **UI/UX design**: Material Design 3 implementation
- **Error handling**: Comprehensive edge case management
- **Coroutines**: Timer implementation and lifecycle management

### AI assistance was used for:
- **Boilerplate code generation**: Reducing repetitive code writing
- **Dependency configuration**: Gradle setup and version management
- **Code structure**: Ensuring consistent formatting and organization
- **Documentation**: Comprehensive README and code comments

The core logic, architecture decisions, and implementation approach were designed and understood manually, with AI helping to accelerate the development process and ensure code quality.

## Production Readiness

This app demonstrates production-ready practices:
- ✅ Clean Architecture
- ✅ Proper error handling
- ✅ State preservation
- ✅ Memory leak prevention
- ✅ Accessibility support
- ✅ Material Design 3
- ✅ Comprehensive logging
- ✅ Thread safety
- ✅ Performance optimization