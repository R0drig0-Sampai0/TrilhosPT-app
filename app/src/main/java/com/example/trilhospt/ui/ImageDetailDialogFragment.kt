package com.example.trilhospt.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import coil.load
import com.example.trilhospt.R
import com.example.trilhospt.databinding.DialogImageDetailBinding
import com.example.trilhospt.data.remote.api.RetrofitClient

class ImageDetailDialogFragment : DialogFragment() {

    private var _binding: DialogImageDetailBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_IMAGE_URL = "image_url"
        private const val ARG_PHOTO_ID = "photo_id"
        private const val ARG_is_OWNER = "is_owner"

        fun newInstance(imageUrl: String, photoId: Int? = null, isOwner: Boolean = false): ImageDetailDialogFragment {
            val args = Bundle()
            args.putString(ARG_IMAGE_URL, imageUrl)
            if (photoId != null) args.putInt(ARG_PHOTO_ID, photoId)
            args.putBoolean(ARG_is_OWNER, isOwner)
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
        val photoId = arguments?.getInt(ARG_PHOTO_ID, -1).takeIf { it != -1 }
        val isOwner = arguments?.getBoolean(ARG_is_OWNER, false) ?: false

        val baseUrl = RetrofitClient.BASE_URL.trimEnd('/')
        val fullUrl = if (imageUrl.startsWith("http")) imageUrl else "$baseUrl/${imageUrl.trimStart('/')}"

        binding.ivFullImage.load(fullUrl) {
            crossfade(true)
            placeholder(R.drawable.bg_gradient_overlay)
            error(android.R.drawable.ic_menu_report_image)
        }

        binding.btnClose.setOnClickListener {
            dismiss()
        }

        if (isOwner && photoId != null) {
            binding.btnDelete.visibility = View.VISIBLE
            binding.btnDelete.setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle("Apagar Foto")
                    .setMessage("Tens a certeza que queres apagar esta foto?")
                    .setPositiveButton("Apagar") { _, _ ->
                        val result = Bundle().apply {
                            putInt("photoId", photoId)
                            putString("action", "delete")
                        }
                        setFragmentResult("photo_action", result)
                        dismiss()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        } else {
            binding.btnDelete.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
