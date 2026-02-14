package com.example.trilhospt.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import coil.load
import com.example.trilhospt.R
import com.example.trilhospt.databinding.DialogImageDetailBinding

class ImageDetailDialogFragment : DialogFragment() {

    private var _binding: DialogImageDetailBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_IMAGE_URL = "image_url"

        fun newInstance(imageUrl: String): ImageDetailDialogFragment {
            val args = Bundle()
            args.putString(ARG_IMAGE_URL, imageUrl)
            val fragment = ImageDetailDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogImageDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageUrl = arguments?.getString(ARG_IMAGE_URL) ?: return
        val fullUrl = if (imageUrl.startsWith("http")) imageUrl else "http://10.129.146.48:8000$imageUrl"

        binding.ivFullImage.load(fullUrl) {
            crossfade(true)
            placeholder(R.drawable.bg_gradient_overlay) // Should fix if bg_gradient_overlay does not exist
            error(android.R.drawable.ic_menu_report_image)
        }

        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
