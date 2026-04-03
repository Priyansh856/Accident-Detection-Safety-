package com.example.accidentdetection.data.repository

import com.example.accidentdetection.data.local.AppDatabase
import com.example.accidentdetection.data.local.CrashEvent
import com.example.accidentdetection.data.local.EmergencyContact
import kotlinx.coroutines.flow.Flow

class ContactRepository(
    private val database: AppDatabase,
) {
    fun observeContacts(): Flow<List<EmergencyContact>> = database.emergencyContactDao().observeAll()

    suspend fun getContacts(): List<EmergencyContact> = database.emergencyContactDao().getAll()

    suspend fun saveContact(contact: EmergencyContact) {
        if (contact.id == 0L) {
            database.emergencyContactDao().insert(contact)
        } else {
            database.emergencyContactDao().update(contact)
        }
    }

    suspend fun deleteContact(contact: EmergencyContact) {
        database.emergencyContactDao().delete(contact)
    }

    suspend fun logCrash(event: CrashEvent) {
        database.crashEventDao().insert(event)
    }

    fun observeCrashHistory(): Flow<List<CrashEvent>> = database.crashEventDao().observeRecent()

    suspend fun latestCrash(): CrashEvent? = database.crashEventDao().getLatest()
}
