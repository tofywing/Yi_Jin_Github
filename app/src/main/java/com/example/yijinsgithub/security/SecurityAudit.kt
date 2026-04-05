package com.example.yijinsgithub.security

import android.util.Log

/**
 * Centralized security auditing utility for tracking sensitive events.
 * 
 * In a production environment, these logs would typically be sent to a secure 
 * remote server or a specialized crash reporting tool (like Firebase Crashlytics 
 * custom keys) for security analysis.
 */
object SecurityAudit {
    private const val TAG = "SecurityAudit"

    /**
     * Logs a security integrity failure.
     */
    fun logIntegrityViolation(reason: String) {
        Log.e(TAG, "[INTEGRITY_VIOLATION] Security risk detected: $reason")
    }

    /**
     * Logs an authentication-related security event.
     */
    fun logAuthEvent(event: String, isSuccess: Boolean) {
        val status = if (isSuccess) "SUCCESS" else "FAILURE"
        Log.i(TAG, "[AUTH_EVENT] $event: $status")
    }

    /**
     * Logs potentially malicious activity or suspicious network errors.
     */
    fun logSuspiciousActivity(activity: String) {
        Log.w(TAG, "[SUSPICIOUS_ACTIVITY] $activity")
    }
}
