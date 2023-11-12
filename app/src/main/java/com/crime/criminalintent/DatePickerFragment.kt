package com.crime.criminalintent

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs

class DatePickerFragment : DialogFragment() {

    private val args: DatePickerFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dateListener = DatePickerDialog.OnDateSetListener {
                _: DatePicker, year: Int, month: Int, day: Int ->

            setFragmentResult(REQUEST_KEY_DATE, bundleOf(
                BUNDLE_KEY_YEAR to year,
                BUNDLE_KEY_MONTH to month,
                BUNDLE_KEY_DAY to day
            ))
        }

        return DatePickerDialog(
            requireContext(),
            dateListener,
            args.year,
            args.month,
            args.day
        )
    }

    companion object {
        const val REQUEST_KEY_DATE = "REQUEST_KEY_DATE"
        const val BUNDLE_KEY_YEAR = "BUNDLE_KEY_YEAR"
        const val BUNDLE_KEY_MONTH = "BUNDLE_KEY_MONTH"
        const val BUNDLE_KEY_DAY = "BUNDLE_KEY_DAY"
    }
}