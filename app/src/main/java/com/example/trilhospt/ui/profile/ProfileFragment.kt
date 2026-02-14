package com.example.trilhospt.ui.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.example.trilhospt.R
import com.example.trilhospt.data.repository.ApiRepository
import com.example.trilhospt.data.remote.api.RetrofitClient
import com.example.trilhospt.databinding.FragmentProfileBinding
import com.example.trilhospt.ui.ViewModelFactory
import com.example.trilhospt.ui.adapters.BadgeAdapter
import com.example.trilhospt.utils.Resource
import com.example.trilhospt.data.remote.api.AuthInterceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ProfileViewModel
    private lateinit var badgeAdapter: BadgeAdapter
    private lateinit var photoAdapter: com.example.trilhospt.ui.adapters.ProfilePhotoAdapter
    private lateinit var createdTrailsAdapter: com.example.trilhospt.ui.adapters.HorizontalTrailAdapter
    private lateinit var completedTrailsAdapter: com.example.trilhospt.ui.adapters.HorizontalTrailAdapter
    
    // Upload variables
    private var selectedImageUri: android.net.Uri? = null
    private var tempImageUri: android.net.Uri? = null

    // Launchers
    private val getContent = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri: android.net.Uri? ->
        uri?.let {
            selectedImageUri = it
            showUploadDialog(it)
        }
    }

    private val takePicture = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success && tempImageUri != null) {
            selectedImageUri = tempImageUri
            showUploadDialog(tempImageUri!!)
        }
    }

    private val requestPermission = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(context, "Permissão de câmara necessária", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root as View
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = arguments?.getInt("userId", -1)?.takeIf { it != -1 }
        
        val repository = ApiRepository()
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]

        badgeAdapter = com.example.trilhospt.ui.adapters.BadgeAdapter(emptyList())
        binding.rvBadges.adapter = badgeAdapter

        setupTrailsAdapters()
        setupGallery()
        setupObservers()
        viewModel.getProfile(userId)
        viewModel.getCreatedTrails(userId)
        viewModel.getCompletedTrails(userId)
        
        if (userId != null) {
            binding.btnLogout.visibility = View.GONE
        } else {
            binding.btnLogout.visibility = View.VISIBLE
            binding.btnLogout.setOnClickListener {
                logout()
            }
        }
        
        setupAddPhotoButton(userId)
        setupSwipeRefresh(userId)
    }

    private fun setupSwipeRefresh(userId: Int?) {
        binding.swipeRefresh.setOnRefreshListener {
            if (userId != null && userId != -1) {
                viewModel.getProfile(userId)
                viewModel.getCreatedTrails(userId)
                viewModel.getCompletedTrails(userId)
                viewModel.getBadges(userId)
                viewModel.getUserPhotos(userId)
            } else {
                viewModel.getProfile()
                viewModel.getCreatedTrails()
                viewModel.getCompletedTrails()
                viewModel.getBadges()
                // For photos, we need the user ID from profile
                (viewModel.profile.value as? Resource.Success)?.data?.id?.let { 
                    viewModel.getUserPhotos(it) 
                }
            }
        }
        // Set color scheme
        binding.swipeRefresh.setColorSchemeResources(
            R.color.primary,
            R.color.primary_light,
            R.color.accent
        )
    }

    private fun setupObservers() {
        viewModel.profile.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    val user = resource.data
                    
                    binding.tvUsername.text = user.username
                    if (user.email.isNullOrEmpty()) {
                        binding.tvEmail.visibility = View.GONE
                    } else {
                        binding.tvEmail.visibility = View.VISIBLE
                        binding.tvEmail.text = user.email
                    }
                    
                    val userIdArg = arguments?.getInt("userId", -1) ?: -1
                    if (userIdArg != -1) {
                        (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title = "Perfil de ${user.username}"
                    } else {
                        (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title = getString(R.string.my_profile)
                    }
                    binding.tvTotalDistance.text = String.format("%.1f", user.totalDistance)
                    binding.tvTrailsCount.text = user.totalTrails.toString()
                    binding.tvLevel.text = user.level.toString()

                    // Load Profile Photo
                    user.profilePhoto?.let { photoUrl ->
                        val baseUrl = RetrofitClient.BASE_URL.trimEnd('/')
                        val fullUrl = if (photoUrl.startsWith("http")) photoUrl else "$baseUrl/${photoUrl.trimStart('/')}"
                        binding.ivProfilePhoto.load(fullUrl) {
                            crossfade(true)
                            placeholder(android.R.drawable.ic_menu_gallery)
                            error(android.R.drawable.ic_menu_gallery)
                        }
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.badges.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Could show a small loader for badges specifically if needed
                }
                is Resource.Success -> {
                    resource.data?.let { badgeAdapter.updateBadges(it) }
                }
                is Resource.Error -> {
                    // Silently fail or show a small error for badges
                }
            }
        }

        viewModel.photos.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> { } 
                is Resource.Success -> {
                    val photos = resource.data
                    if (photos.isNullOrEmpty()) {
                        binding.rvGallery.visibility = View.GONE
                        binding.galleryHeader.visibility = View.GONE
                    } else {
                        binding.rvGallery.visibility = View.VISIBLE
                        binding.galleryHeader.visibility = View.VISIBLE
                        photoAdapter.updatePhotos(photos)
                    }
                }
                is Resource.Error -> {
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(context, "Erro ao carregar fotos", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.createdTrails.observe(viewLifecycleOwner) { resource ->
             when (resource) {
                is Resource.Loading -> { }
                is Resource.Success -> {
                    val trails = resource.data
                    if (trails.isNullOrEmpty()) {
                        binding.rvCreatedTrails.visibility = View.GONE
                        binding.tvCreatedTrailsTitle.visibility = View.VISIBLE
                        binding.tvCreatedTrailsEmpty.visibility = View.VISIBLE
                    } else {
                        binding.rvCreatedTrails.visibility = View.VISIBLE
                        binding.tvCreatedTrailsTitle.visibility = View.VISIBLE
                        binding.tvCreatedTrailsEmpty.visibility = View.GONE
                        createdTrailsAdapter.updateTrails(trails)
                    }
                }
                is Resource.Error -> {
                    binding.rvCreatedTrails.visibility = View.GONE
                    binding.tvCreatedTrailsTitle.visibility = View.GONE
                    Toast.makeText(requireContext(), "Erro ao carregar trilhos: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.completedTrails.observe(viewLifecycleOwner) { resource ->
             when (resource) {
                is Resource.Loading -> { }
                is Resource.Success -> {
                    val trails = resource.data
                    if (trails.isNullOrEmpty()) {
                        binding.rvCompletedTrails.visibility = View.GONE
                        binding.tvCompletedTrailsTitle.visibility = View.VISIBLE
                        binding.tvCompletedTrailsEmpty.visibility = View.VISIBLE
                    } else {
                        binding.rvCompletedTrails.visibility = View.VISIBLE
                        binding.tvCompletedTrailsTitle.visibility = View.VISIBLE
                        binding.tvCompletedTrailsEmpty.visibility = View.GONE
                        completedTrailsAdapter.updateTrails(trails)
                    }
                }
                is Resource.Error -> {
                    binding.rvCompletedTrails.visibility = View.GONE
                    binding.tvCompletedTrailsTitle.visibility = View.GONE
                }
            }
        }

        viewModel.uploadStatus.observe(viewLifecycleOwner) { resource ->
            if (resource == null) return@observe
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    Toast.makeText(context, "A enviar foto...", Toast.LENGTH_SHORT).show()
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Foto carregada com sucesso!", Toast.LENGTH_SHORT).show()
                    viewModel.clearUploadStatus()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, resource.message, Toast.LENGTH_LONG).show()
                    viewModel.clearUploadStatus()
                }
            }
        }
    }

    private fun logout() {
        // Clear saved token and user data using AuthInterceptor
        val authInterceptor = AuthInterceptor(requireContext())
        authInterceptor.clearToken()
        
        // Update menu visibility in MainActivity
        activity?.invalidateOptionsMenu()
        
        Toast.makeText(requireContext(), "Sessão terminada!", Toast.LENGTH_SHORT).show()
        
        // Navigate back to login
        findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
    }

    private fun setupTrailsAdapters() {
        createdTrailsAdapter = com.example.trilhospt.ui.adapters.HorizontalTrailAdapter(emptyList()) { trail ->
            val bundle = Bundle().apply {
                trail.id?.let { putInt("trailId", it) }
            }
            findNavController().navigate(R.id.action_profileFragment_to_trailDetailFragment, bundle)
        }
        binding.rvCreatedTrails.adapter = createdTrailsAdapter

        completedTrailsAdapter = com.example.trilhospt.ui.adapters.HorizontalTrailAdapter(emptyList()) { trail ->
            val bundle = Bundle().apply {
                trail.id?.let { putInt("trailId", it) }
            }
            findNavController().navigate(R.id.action_profileFragment_to_trailDetailFragment, bundle)
        }
        binding.rvCompletedTrails.adapter = completedTrailsAdapter
    }

    private fun setupGallery() {
        photoAdapter = com.example.trilhospt.ui.adapters.ProfilePhotoAdapter(emptyList()) { photo ->
            photo.image?.let { url ->
                val dialog = com.example.trilhospt.ui.ImageDetailDialogFragment.newInstance(url)
                dialog.show(parentFragmentManager, "image_detail")
            }
        }
        binding.rvGallery.adapter = photoAdapter
    }

    private fun setupAddPhotoButton(userIdArg: Int?) {
        // Only show Add Photo button if viewing own profile
        if (userIdArg == null) {
            binding.btnAddPhoto.visibility = View.VISIBLE
            binding.btnAddPhoto.setOnClickListener {
                showSourceSelectionDialog()
            }
            // Fetch trails for the spinner (using created trails for now)
            viewModel.getCreatedTrails()
        } else {
            binding.btnAddPhoto.visibility = View.GONE
        }
    }

    private fun showSourceSelectionDialog() {
        val options = arrayOf("Câmara", "Galeria")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Adicionar Foto")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openGallery() {
        getContent.launch("image/*")
    }

    private fun openCamera() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            requestPermission.launch(android.Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        try {
            val photoFile = java.io.File.createTempFile(
                "IMG_", 
                ".jpg", 
                requireContext().getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
            )
            tempImageUri = androidx.core.content.FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                photoFile
            )
            takePicture.launch(tempImageUri)
        } catch (e: Exception) {
            Toast.makeText(context, "Erro ao iniciar câmara: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun showUploadDialog(imageUri: android.net.Uri) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_upload_photo, null)
        val ivPreview = dialogView.findViewById<android.widget.ImageView>(R.id.ivPreview)
        val btnCamera = dialogView.findViewById<android.widget.Button>(R.id.btnCamera)
        val btnGallery = dialogView.findViewById<android.widget.Button>(R.id.btnGallery)
        val spinner = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerTrails)
        val etDesc = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etDescription)

        // Hide source buttons since we already have an image
        btnCamera.visibility = View.GONE
        btnGallery.visibility = View.GONE
        ivPreview.visibility = View.VISIBLE
        ivPreview.setImageURI(imageUri)

        // Setup Spinner
        val trailsList = mutableListOf<com.example.trilhospt.data.remote.dto.TrailDto>()
        
        // Populate spinner from viewModel logic - checking current trails
        val currentTrails = viewModel.createdTrails.value
        if (currentTrails is Resource.Success) {
            trailsList.addAll(currentTrails.data)
        }

        val trailNames = trailsList.map { it.name }
        val adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, trailNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Publicar") { _, _ ->
                val desc = etDesc.text.toString()
                val position = spinner.selectedItemPosition
                if (position >= 0 && position < trailsList.size) {
                    val trailId = trailsList[position].id ?: return@setPositiveButton
                    
                    prepareUpload(imageUri, trailId, desc)
                } else {
                    Toast.makeText(context, "Selecione um trilho válido", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun prepareUpload(uri: android.net.Uri, trailId: Int, description: String) {
        try {
            val contentResolver = requireContext().contentResolver
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            val extension = android.webkit.MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"
            
            val inputStream = contentResolver.openInputStream(uri)
            val file = java.io.File(requireContext().cacheDir, "upload_temp.$extension")
            val outputStream = java.io.FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            val requestFile = RequestBody.create(mimeType.toMediaTypeOrNull(), file)
            val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

            // Random coordinates for now if not extracted from EXIF (simplification)
            // or we could use the trail coordinates associated with it
            val lat = 38.7 
            val lng = -9.1

            viewModel.uploadPhoto(body, trailId, lat, lng, description)
            
        } catch (e: Exception) {
            Toast.makeText(context, "Erro ao preparar arquivo: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
