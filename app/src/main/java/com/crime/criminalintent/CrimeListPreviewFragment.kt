package com.crime.criminalintent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.crime.criminalintent.databinding.FragmentCrimeListPreviewBinding

class CrimeListPreviewFragment :Fragment() {

    private var _binding: FragmentCrimeListPreviewBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. IS the view visible?"
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentCrimeListPreviewBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}