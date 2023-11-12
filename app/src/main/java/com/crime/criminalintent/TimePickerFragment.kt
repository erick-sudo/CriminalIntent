package com.crime.criminalintent

import android.app.Dialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.TimePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs

class TimePickerFragment : DialogFragment() {

    private val args: TimePickerFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val timeChangedListener = TimePickerDialog.OnTimeSetListener {
            _: TimePicker, hourOfDay, minutes ->

            setFragmentResult(REQUEST_KEY_TIME, bundleOf(
                    BUNDLE_KEY_HOUR to hourOfDay,
                    BUNDLE_KEY_MINUTE to minutes
                )
            )
        }

        return TimePickerDialog(
            requireContext(),
            timeChangedListener,
            args.hourOfDay,
            args.minutes,
            true
            )
    }

    companion object {
        const val REQUEST_KEY_TIME = "REQUEST_KEY_TIME"
        const val BUNDLE_KEY_HOUR = "BUNDLE_KEY_HOUR"
        const val BUNDLE_KEY_MINUTE = "BUNDLE_KEY_MINUTE"
    }
}