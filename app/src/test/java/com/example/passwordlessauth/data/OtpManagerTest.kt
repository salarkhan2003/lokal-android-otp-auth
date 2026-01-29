package com.example.passwordlessauth.data

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for OtpManager to verify OTP generation, validation, and expiry logic.
 * Demonstrates testing practices for the core business logic.
 */
class OtpManagerTest {
    
    private lateinit var otpManager: OtpManager
    
    @Before
    fun setup() {
        otpManager = OtpManager()
    }
    
    @Test
    fun `generateOtp creates valid 6-digit OTP`() {
        val email = "test@example.com"
        val otp = otpManager.generateOtp(email)
        
        // Verify OTP is 6 digits
        assertEquals(6, otp.length)
        assertTrue("OTP should be numeric", otp.all { it.isDigit() })
        assertTrue("OTP should be in valid range", otp.toInt() in 100000..999999)
    }
    
    @Test
    fun `generateOtp stores OTP data correctly`() {
        val email = "test@example.com"
        val otp = otpManager.generateOtp(email)
        
        val otpData = otpManager.getOtpData(email)
        assertNotNull("OTP data should be stored", otpData)
        assertEquals("Stored OTP should match generated", otp, otpData?.otp)
        assertEquals("Should have 3 attempts initially", 3, otpData?.attemptsRemaining)
        assertFalse("OTP should not be expired initially", otpData?.isExpired() ?: true)
    }
    
    @Test
    fun `validateOtp returns success for correct OTP`() {
        val email = "test@example.com"
        val otp = otpManager.generateOtp(email)
        
        val result = otpManager.validateOtp(email, otp)
        assertTrue("Validation should succeed", result is OtpValidationResult.Success)
        
        // OTP should be removed after successful validation
        assertNull("OTP should be removed after success", otpManager.getOtpData(email))
    }
    
    @Test
    fun `validateOtp decrements attempts for wrong OTP`() {
        val email = "test@example.com"
        otpManager.generateOtp(email)
        
        val result = otpManager.validateOtp(email, "000000")
        assertTrue("Should return WrongOtp result", result is OtpValidationResult.WrongOtp)
        
        val remainingAttempts = (result as OtpValidationResult.WrongOtp).attemptsRemaining
        assertEquals("Should have 2 attempts remaining", 2, remainingAttempts)
        
        val otpData = otpManager.getOtpData(email)
        assertEquals("Stored attempts should match", 2, otpData?.attemptsRemaining)
    }
    
    @Test
    fun `validateOtp returns AttemptsExhausted after 3 wrong attempts`() {
        val email = "test@example.com"
        otpManager.generateOtp(email)
        
        // Make 3 wrong attempts
        otpManager.validateOtp(email, "000000") // 2 attempts left
        otpManager.validateOtp(email, "000000") // 1 attempt left
        otpManager.validateOtp(email, "000000") // 0 attempts left
        
        val result = otpManager.validateOtp(email, "000000")
        assertTrue("Should return AttemptsExhausted", result is OtpValidationResult.AttemptsExhausted)
    }
    
    @Test
    fun `validateOtp returns NoOtpFound for unknown email`() {
        val result = otpManager.validateOtp("unknown@example.com", "123456")
        assertTrue("Should return NoOtpFound", result is OtpValidationResult.NoOtpFound)
    }
    
    @Test
    fun `generateOtp invalidates previous OTP for same email`() {
        val email = "test@example.com"
        val firstOtp = otpManager.generateOtp(email)
        val secondOtp = otpManager.generateOtp(email)
        
        assertNotEquals("New OTP should be different", firstOtp, secondOtp)
        
        // First OTP should no longer work
        val result = otpManager.validateOtp(email, firstOtp)
        assertTrue("First OTP should be invalid", result is OtpValidationResult.WrongOtp)
        
        // Second OTP should work
        val result2 = otpManager.validateOtp(email, secondOtp)
        assertTrue("Second OTP should be valid", result2 is OtpValidationResult.Success)
    }
    
    @Test
    fun `clearAllOtpData removes all stored OTPs`() {
        otpManager.generateOtp("user1@example.com")
        otpManager.generateOtp("user2@example.com")
        
        assertNotNull("User1 OTP should exist", otpManager.getOtpData("user1@example.com"))
        assertNotNull("User2 OTP should exist", otpManager.getOtpData("user2@example.com"))
        
        otpManager.clearAllOtpData()
        
        assertNull("User1 OTP should be cleared", otpManager.getOtpData("user1@example.com"))
        assertNull("User2 OTP should be cleared", otpManager.getOtpData("user2@example.com"))
    }
}