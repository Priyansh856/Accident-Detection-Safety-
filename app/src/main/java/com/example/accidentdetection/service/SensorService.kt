package com.example.accidentdetection.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.accidentdetection.AccidentSafetyApp
import com.example.accidentdetection.MainActivity
import com.example.accidentdetection.R
import com.example.accidentdetection.data.local.CrashEvent
import com.example.accidentdetection.domain.AccidentDetector
import com.example.accidentdetection.ui.alert.AlertActivity
import com.example.accidentdetection.utils.LocationHelper
import com.example.accidentdetection.utils.PhoneCallHelper
import com.example.accidentdetection.utils.SMSHelper
import kotlinx.coroutines.launch

class SensorService : LifecycleService() {

    private lateinit var sensorHandler: SensorHandler
    private lateinit var accidentDetector: AccidentDetector
    private lateinit var monitoringStateStore: MonitoringStateStore
    private lateinit var locationHelper: LocationHelper
    private lateinit var smsHelper: SMSHelper
    private lateinit var phoneCallHelper: PhoneCallHelper

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val app = application as AccidentSafetyApp
        monitoringStateStore = app.monitoringStateStore
        locationHelper = LocationHelper(this)
        smsHelper = SMSHelper(this)
        phoneCallHelper = PhoneCallHelper(this)
        accidentDetector = AccidentDetector(impactThreshold = monitoringStateStore.getImpactThreshold())
        sensorHandler = SensorHandler(this) { sample ->
            val result = accidentDetector.process(sample)
            if (result.detected) {
                monitoringStateStore.setLastDetection(result.reason ?: "Possible crash")
                launchAlert(result.reason ?: "Possible crash")
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> stopMonitoring()
            ACTION_ALERT_CONFIRMED_SAFE -> cancelEmergency()
            ACTION_TRIGGER_EMERGENCY -> triggerEmergencyActions()
            else -> startMonitoring()
        }
        return Service.START_STICKY
    }

    override fun onDestroy() {
        sensorHandler.stop()
        monitoringStateStore.setMonitoringActive(false)
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    private fun startMonitoring() {
        startForeground(NOTIFICATION_ID, buildNotification())
        sensorHandler.start()
        monitoringStateStore.setMonitoringActive(true)
    }

    private fun stopMonitoring() {
        sensorHandler.stop()
        monitoringStateStore.setMonitoringActive(false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun cancelEmergency() {
        monitoringStateStore.setLastDetection("User marked safe")
    }

    private fun launchAlert(reason: String) {
        lifecycleScope.launch {
            val location = if (locationHelper.hasLocationPermission(this@SensorService)) {
                locationHelper.getCurrentLocationOrNull()
            } else {
                null
            }
            val app = application as AccidentSafetyApp
            app.repository.logCrash(
                CrashEvent(
                    timestamp = System.currentTimeMillis(),
                    reason = reason,
                    latitude = location?.latitude,
                    longitude = location?.longitude,
                    emergencyTriggered = false,
                ),
            )

            val intent = Intent(this@SensorService, AlertActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra(AlertActivity.EXTRA_REASON, reason)
            }
            startActivity(intent)
        }
    }

    private fun triggerEmergencyActions() {
        lifecycleScope.launch {
            val app = application as AccidentSafetyApp
            val contacts = app.repository.getContacts()
            if (contacts.isEmpty()) return@launch

            val location = if (locationHelper.hasLocationPermission(this@SensorService)) {
                locationHelper.getCurrentLocationOrNull()
            } else {
                null
            }
            val locationUrl = location?.let {
                "https://maps.google.com/?q=${it.latitude},${it.longitude}"
            } ?: "Location unavailable"

            smsHelper.sendEmergencyMessages(contacts, locationUrl)

            if (monitoringStateStore.isAutoCallEnabled()) {
                contacts.firstOrNull()?.let { phoneCallHelper.call(it.phoneNumber) }
            }

            app.repository.logCrash(
                CrashEvent(
                    timestamp = System.currentTimeMillis(),
                    reason = "Emergency alert sent",
                    latitude = location?.latitude,
                    longitude = location?.longitude,
                    emergencyTriggered = true,
                ),
            )
        }
    }

    private fun buildNotification(): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, SensorService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.service_notification_title))
            .setContentText(getString(R.string.service_notification_text))
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .addAction(0, getString(R.string.stop_monitoring), stopIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.service_channel_name),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = getString(R.string.service_channel_description)
                setShowBadge(false)
            }
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "accident_monitoring"
        private const val NOTIFICATION_ID = 1001

        const val ACTION_STOP = "com.example.accidentdetection.action.STOP"
        const val ACTION_ALERT_CONFIRMED_SAFE = "com.example.accidentdetection.action.ALERT_CONFIRMED_SAFE"
        const val ACTION_TRIGGER_EMERGENCY = "com.example.accidentdetection.action.TRIGGER_EMERGENCY"

        fun start(context: Context) {
            val intent = Intent(context, SensorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.startService(Intent(context, SensorService::class.java).setAction(ACTION_STOP))
        }

        fun markSafe(context: Context) {
            context.startService(
                Intent(context, SensorService::class.java).setAction(ACTION_ALERT_CONFIRMED_SAFE),
            )
        }

        fun triggerEmergency(context: Context) {
            context.startService(
                Intent(context, SensorService::class.java).setAction(ACTION_TRIGGER_EMERGENCY),
            )
        }

        fun openBatteryOptimizationSettings(context: Context) {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}
