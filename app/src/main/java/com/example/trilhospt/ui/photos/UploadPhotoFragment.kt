package com.example.trilhospt.ui.photos

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.trilhospt.R
import com.example.trilhospt.data.remote.api.AuthInterceptor
import com.example.trilhospt.data.remote.api.RetrofitClient
import com.example.trilhospt.data.remote.dto.TrailDto
import com.example.trilhospt.data.repository.ApiRepository
import com.example.trilhospt.databinding.FragmentUploadPhotoBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class UploadPhotoFragment : Fragment() {

    private var _binding: FragmentUploadPhotoBinding? = null
    private val binding get() = _binding!!
    
    private var imageUri: Uri? = null
    private var imageFile: File? = null
    private val trails = mutableListOf<TrailDto>()
    private val repository = ApiRepository()
    private lateinit var authInterceptor: AuthInterceptor
    
    // Result launcher for camera
    private val cameraLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val thumbnail = result.data?.extras?.get("data") as? Bitmap
            if (thumbnail != null) {
                // Save bitmap to file
                val file = File(requireContext().cacheDir, "upload_photo.jpg")
                val fos = FileOutputStream(file)
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                fos.close()
                
                this.imageFile = file
                binding.ivPreview.setImageBitmap(thumbnail)
                binding.btnUpload.isEnabled = true
                binding.btnTakePhoto.text = "Tirar Outra"
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadPhotoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        authInterceptor = AuthInterceptor(requireContext())
        
        setupListeners()
        loadTrails()
    }
    
    private fun setupListeners() {
        binding.btnTakePhoto.setOnClickListener {
            checkPermissionsAndLaunchCamera()
        }
        
        binding.btnUpload.setOnClickListener {
            uploadPhoto()
        }
    }
    
    private fun checkPermissionsAndLaunchCamera() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 100)
        } else {
            launchCamera()
        }
    }
    
    private fun launchCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }
    
    // API requires latitude and longitude for trail photos
    private fun uploadPhoto() {
        val file = imageFile ?: return
        val selectedTrailPosition = binding.spinnerTrails.selectedItemPosition
        
        if (selectedTrailPosition == -1 || trails.isEmpty()) {
            Toast.makeText(context, "Por favor selecione um trilho", Toast.LENGTH_SHORT).show()
            return
        }
        
        val selectedTrail = trails[selectedTrailPosition]
        val trailId = selectedTrail.id ?: return
        
        binding.progressBar.visibility = View.VISIBLE
        binding.btnUpload.isEnabled = false
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Prepare params
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("image", file.name, requestFile)
                
                val trailIdBody = RequestBody.create("text/plain".toMediaTypeOrNull(), trailId.toString())
                // Default coordinates if location is not available (for now using trail start or simple placeholder)
                // In a real app we should get user location
                val latBody = RequestBody.create("text/plain".toMediaTypeOrNull(), "0.0")
                val lngBody = RequestBody.create("text/plain".toMediaTypeOrNull(), "0.0")
                
                val token = "Token ${authInterceptor.getToken()}"
                
                val response = RetrofitClient.getApiService().uploadPhoto(
                    token, body, trailIdBody, latBody, lngBody, null
                )
                
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Foto guardada na galeria!", Toast.LENGTH_SHORT).show()
                        // Navigate back or to gallery
                        findNavController().navigateUp()
                    } else {
                        Toast.makeText(context, "Erro ao enviar: ${response.message()}", Toast.LENGTH_SHORT).show()
                        binding.btnUpload.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.btnUpload.isEnabled = true
                }
            }
        }
    }
    
    private fun loadTrails() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Fetch all trails to populate spinner
                val response = repository.getTrails()
                
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.let { trailList ->
                            trails.clear()
                            trails.addAll(trailList)
                            setupSpinner()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun setupSpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            trails.map { it.name ?: "Sem Nome" }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTrails.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
