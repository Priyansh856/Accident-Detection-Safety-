package com.example.accidentdetection.utils

import android.content.Context
import android.telephony.SmsManager
import com.example.accidentdetection.data.local.EmergencyContact

class SMSHelper(private val context: Context) {

    fun sendEmergencyMessages(contacts: List<EmergencyContact>, locationUrl: String) {
        val smsManager = context.getSystemService(SmsManager::class.java)
        val message = "Emergency! I may have been in an accident. My location: $locationUrl"
        contacts.forEach { contact ->
            smsManager.sendTextMessage(contact.phoneNumber, null, message, null, null)
        }
    }
}
