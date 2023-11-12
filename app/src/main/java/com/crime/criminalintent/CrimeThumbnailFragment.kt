package com.crime.criminalintent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.crime.criminalintent.databinding.FragmentCrimeThumbnailBinding
import java.io.File

class CrimeThumbnailFragment : DialogFragment() {

    private var _binding: FragmentCrimeThumbnailBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. IS the view visible?"
        }

    private val args: CrimeThumbnailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentCrimeThumbnailBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            updatePhoto(args.photoFileName)
        }
    }

    private fun updatePhoto(photoFileName: String?) {
        if (binding.imageView.tag != photoFileName) {
            val photoFile = photoFileName?.let {
                File(requireContext().applicationContext.filesDir, it)
            }

            if(photoFile?.exists() == true) {
                binding.imageView.doOnLayout {
                    val scaledBitmap = getScaledBitmap(
                        photoFile.path,
                        400,
                        400
                    )

                    binding.imageView.setImageBitmap(scaledBitmap)
                    binding.imageView.tag = photoFileName
                }
            } else {
                binding.imageView.setImageBitmap(null)
                binding.imageView.tag = null
            }
        }
    }
}