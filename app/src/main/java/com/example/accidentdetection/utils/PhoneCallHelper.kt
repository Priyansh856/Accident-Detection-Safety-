package com.example.accidentdetection.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

class PhoneCallHelper(private val context: Context) {

    fun call(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$phoneNumber")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
