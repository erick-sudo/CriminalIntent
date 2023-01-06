package com.kcire.criminalintent

import android.app.Application
import database.CrimeRepository

class CriminalIntentApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CrimeRepository.initialize(this)
    }
}