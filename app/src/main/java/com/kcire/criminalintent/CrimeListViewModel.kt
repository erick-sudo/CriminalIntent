package com.kcire.criminalintent

import android.util.Log
import androidx.lifecycle.ViewModel
import database.CrimeRepository

class CrimeListViewModel : ViewModel(){

    private val crimeRepository = CrimeRepository.get()

    suspend fun loadCrimes() : List<Crime> {
        return crimeRepository.getCrimes()
    }
}