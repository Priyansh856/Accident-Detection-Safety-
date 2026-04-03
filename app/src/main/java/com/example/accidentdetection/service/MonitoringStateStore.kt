package com.example.accidentdetection.service

import android.content.Context
import androidx.core.content.edit

class MonitoringStateStore(context: Context) {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isMonitoringActive(): Boolean = preferences.getBoolean(KEY_MONITORING_ACTIVE, false)

    fun setMonitoringActive(active: Boolean) {
        preferences.edit { putBoolean(KEY_MONITORING_ACTIVE, active) }
    }

    fun isAutoCallEnabled(): Boolean = preferences.getBoolean(KEY_AUTO_CALL, false)

    fun setAutoCallEnabled(enabled: Boolean) {
        preferences.edit { putBoolean(KEY_AUTO_CALL, enabled) }
    }

    fun getImpactThreshold(): Float = preferences.getFloat(KEY_IMPACT_THRESHOLD, 25f)

    fun setImpactThreshold(threshold: Float) {
        preferences.edit { putFloat(KEY_IMPACT_THRESHOLD, threshold) }
    }

    fun setLastDetection(reason: String) {
        preferences.edit { putString(KEY_LAST_DETECTION, reason) }
    }

    fun getLastDetection(): String = preferences.getString(KEY_LAST_DETECTION, null) ?: "None recorded"

    companion object {
        private const val PREFS_NAME = "monitoring_prefs"
        private const val KEY_MONITORING_ACTIVE = "monitoring_active"
        private const val KEY_AUTO_CALL = "auto_call"
        private const val KEY_IMPACT_THRESHOLD = "impact_threshold"
        private const val KEY_LAST_DETECTION = "last_detection"
    }
}
