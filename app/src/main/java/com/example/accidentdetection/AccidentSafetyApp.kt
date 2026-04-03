package com.example.accidentdetection

import android.app.Application
import com.example.accidentdetection.data.local.AppDatabase
import com.example.accidentdetection.data.repository.ContactRepository
import com.example.accidentdetection.service.MonitoringStateStore

class AccidentSafetyApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val repository: ContactRepository by lazy { ContactRepository(database) }
    val monitoringStateStore: MonitoringStateStore by lazy { MonitoringStateStore(this) }
}
