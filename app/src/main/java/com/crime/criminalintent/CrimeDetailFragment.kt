package com.crime.criminalintent

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.doOnLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.crime.criminalintent.databinding.FragmentCrimeDetailBinding
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

private const val DATE_FORMAT = "EEE, MM, dd"

class CrimeDetailFragment : Fragment() {

    private var _binding: FragmentCrimeDetailBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. IS the view visible?"
        }

    private val args: CrimeDetailFragmentArgs by navArgs()

    private var photoName: String? = null

    private val crimeDetailViewModel: CrimeDetailViewModel by viewModels {
        CrimeDetailViewModelFactory(args.crimeId)
    }

    private val selectSuspect = registerForActivityResult(ActivityResultContracts.PickContact()) { uri: Uri? ->
        uri?.let { parseContactSelection(it) }
    }

    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) { didTakePhoto: Boolean ->
        if(didTakePhoto && photoName != null) {
            crimeDetailViewModel.updateCrime { oldCrime ->
                oldCrime.copy(photoFileName = photoName)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCrimeDetailBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            crimeTitle.doOnTextChanged { text, _, _, _ ->
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(title = text.toString())
                }
            }

            crimeSolved.setOnCheckedChangeListener { _, isChecked ->
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(isSolved = isChecked)
                }
            }
            crimeSuspect.setOnClickListener {
                selectSuspect.launch(null)
            }

            crimeSuspect.isEnabled = canResolveIntent(selectSuspect.contract.createIntent(requireContext(), null))

            crimeCamera.setOnClickListener {
                photoName = "IMG_${Date()}.JPG"
                val photoFile = File(requireContext().applicationContext.filesDir, photoName)
                val photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.crime.criminalintent.fileprovider",
                    photoFile
                )

                takePhoto.launch(photoUri)
            }

            crimeCamera.isEnabled = canResolveIntent(takePhoto.contract.createIntent(requireContext(), null))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                crimeDetailViewModel.crime.collect { crime ->
                    crime?.let { updateUi(it) }
                }
            }
        }

        setFragmentResultListener(
            DatePickerFragment.REQUEST_KEY_DATE
        ) { _, bundle ->
            val year = bundle.getInt(DatePickerFragment.BUNDLE_KEY_YEAR)
            val month = bundle.getInt(DatePickerFragment.BUNDLE_KEY_MONTH)
            val day = bundle.getInt(DatePickerFragment.BUNDLE_KEY_DAY)

            crimeDetailViewModel.updateCrime {  oldCrime ->
                oldCrime.let {
                    dateVars(oldCrime.date).let {  (_, _, _, hr, min) ->
                        it.copy(date = GregorianCalendar(year, month, day, hr, min).time)
                    }
                }
            }
        }

        setFragmentResultListener(
            TimePickerFragment.REQUEST_KEY_TIME
        ) { _, bundle ->
            val hourOfDay = bundle.getInt(TimePickerFragment.BUNDLE_KEY_HOUR)
            val minutes = bundle.getInt(TimePickerFragment.BUNDLE_KEY_MINUTE)

            crimeDetailViewModel.updateCrime {  oldCrime ->
                oldCrime.let {
                    dateVars(oldCrime.date).let {  (yr, mn, dy, _, _) ->
                        it.copy(date = GregorianCalendar(yr, mn, dy, hourOfDay, minutes).time)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_detail, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete_crime -> {
                deleteCrime()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deleteCrime() {
        viewLifecycleOwner.lifecycleScope.launch {
            crimeDetailViewModel.deleteCrime()

            findNavController().navigate(CrimeDetailFragmentDirections.backToCrimeList())
        }
    }

    private fun getCrimeReport(crime: Crime) : String {
        val solvedString = if(crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }

        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()

        val suspectText = if(crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }

        return getString(
            R.string.crime_report,
            crime.title, dateString, solvedString, suspectText
        )
    }

    private fun parseContactSelection(contactUri: Uri) {
        val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)

        val queryCursor = requireActivity().contentResolver.query(contactUri, queryFields, null, null, null)

        queryCursor?.use { cursor ->
            if(cursor.moveToFirst()) {
                val suspect = cursor.getString(0)
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(suspect = suspect)
                }
            }
        }
    }

    private fun updateUi(crime: Crime) {
        binding.apply {
            if(crimeTitle.text.toString() != crime.title) {
                crimeTitle.setText(crime.title)
            }

            crimeDate.apply {
                val dateOfCrime = DateFormat.format("EEEE, MMMM dd, yyyy.", crime.date)
                text = dateOfCrime

            }

            crimeTime.apply {
                val timeOfCrime = DateFormat.format("HHmm", crime.date)
                text = "$timeOfCrime Hrs"

            }

            crimeSolved.isChecked = crime.isSolved

            crimeTime.setOnClickListener {
                val (_, _, _, hour, minute) = dateVars(crime.date)
                findNavController().navigate(CrimeDetailFragmentDirections.selectTime(hour, minute))
            }

            crimeDate.setOnClickListener {
                val (year, month, day, _, _) = dateVars(crime.date)
                findNavController().navigate(CrimeDetailFragmentDirections.selectDate(year, month, day))
            }

            crimeReport.setOnClickListener {
                val reportIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, getCrimeReport(crime))
                    putExtra(
                        Intent.EXTRA_SUBJECT,
                        getString(R.string.crime_report_subject)
                    )
                }

                val chooserIntent = Intent.createChooser(
                    reportIntent,
                    getString(R.string.send_report)
                )
                startActivity(chooserIntent)
            }

            crimeReport.text = getString(R.string.crime_report_text)

            crimeSuspect.text = crime.suspect.ifEmpty {
                getString(R.string.crime_suspect_text)
            }

            updatePhoto(crime.photoFileName)

            crime.photoFileName?.let { fileName ->
                crimePhoto.setOnClickListener {
                    findNavController().navigate(CrimeDetailFragmentDirections.showThumbnail(fileName))
                }
            }
        }
    }

    private fun dateVars(date: Date) : List<Int> {
        val cal: Calendar = Calendar.getInstance()
        cal.time = date
        return listOf(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH),
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE)
        )
    }

    private fun canResolveIntent(intent: Intent) : Boolean {
        val packageManager: PackageManager = requireActivity().packageManager
        return packageManager.resolveActivity(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        ) != null
    }

    private fun updatePhoto(photoFileName: String?) {
        if (binding.crimePhoto.tag != photoFileName) {
            val photoFile = photoFileName?.let {
                File(requireContext().applicationContext.filesDir, it)
            }

            if(photoFile?.exists() == true) {
                binding.crimePhoto.doOnLayout { measuredView ->
                    val scaledBitmap = getScaledBitmap(
                        photoFile.path,
                        measuredView.width,
                        measuredView.height
                    )

                    binding.crimePhoto.setImageBitmap(scaledBitmap)
                    binding.crimePhoto.tag = photoFileName
                }
            } else {
                binding.crimePhoto.setImageBitmap(null)
                binding.crimePhoto.tag = null
            }
        }
    }
}