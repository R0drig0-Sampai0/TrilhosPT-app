package com.example.trilhospt.ui.trails

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.trilhospt.data.remote.dto.TrailDto
import com.example.trilhospt.data.repository.ApiRepository
import com.example.trilhospt.databinding.FragmentCreateTrailBinding
import com.example.trilhospt.ui.ViewModelFactory
import com.example.trilhospt.utils.Resource
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class CreateTrailFragment : Fragment() {

    private var _binding: FragmentCreateTrailBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TrailViewModel
    private lateinit var mapView: MapView
    private val trailPoints = mutableListOf<GeoPoint>()
    private val markers = mutableListOf<Marker>()
    private var polyline = Polyline()
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    
    // Image Selection
    private var selectedImageUri: android.net.Uri? = null
    private var tempImageUri: android.net.Uri? = null

    private val getContent = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri: android.net.Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivTrailPhoto.setImageURI(it)
            binding.llAddPhotoPlaceholder.visibility = View.GONE
        }
    }

    private val takePicture = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success && tempImageUri != null) {
            selectedImageUri = tempImageUri
            binding.ivTrailPhoto.setImageURI(tempImageUri)
            binding.llAddPhotoPlaceholder.visibility = View.GONE
        }
    }

    private val requestCameraPermission = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
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
        _binding = FragmentCreateTrailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = ApiRepository()
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[TrailViewModel::class.java]

        (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title = "Criar Novo Trilho"

        setupMap()
        setupListeners()
        setupObservers()
        checkLocationPermissions()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            setupLocationOverlay()
        } else {
            Toast.makeText(requireContext(), "Permissão de localização negada. O GPS não funcionará.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) 
            == PackageManager.PERMISSION_GRANTED) {
            setupLocationOverlay()
        } else {
            requestPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    private fun setupMap() {
        // Essential OsmDroid configuration
        org.osmdroid.config.Configuration.getInstance().load(
            requireContext(),
            android.preference.PreferenceManager.getDefaultSharedPreferences(requireContext())
        )
        org.osmdroid.config.Configuration.getInstance().userAgentValue = requireContext().packageName

        mapView = binding.mapCreate
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)
        
        // Center in Portugal (as fallback)
        mapView.controller.setCenter(GeoPoint(39.5, -8.0))

        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                addPoint(p)
                return true
            }

            override fun longPressHelper(p: GeoPoint): Boolean = false
        }

        val overlay = MapEventsOverlay(mapEventsReceiver)
        mapView.overlays.add(overlay)

        polyline.outlinePaint.color = Color.BLUE
        polyline.outlinePaint.strokeWidth = 10f
        mapView.overlays.add(polyline)

        // Setup GPS Location Overlay (Hardware Usage)
        setupLocationOverlay()
    }

    private fun setupLocationOverlay() {
        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), mapView)
        myLocationOverlay.enableMyLocation()
        myLocationOverlay.enableFollowLocation() // Follow user by default on create
        myLocationOverlay.runOnFirstFix {
            activity?.runOnUiThread {
                if (trailPoints.isEmpty()) {
                    mapView.controller.animateTo(myLocationOverlay.myLocation)
                }
            }
        }
        mapView.overlays.add(myLocationOverlay)
    }

    private fun addPoint(point: GeoPoint) {
        trailPoints.add(point)
        
        // Update Polyline
        polyline.addPoint(point)
        
        // Add Marker for the first point (Start)
        if (trailPoints.size == 1) {
            val startMarker = Marker(mapView)
            startMarker.position = point
            startMarker.title = "Início"
            mapView.overlays.add(startMarker)
            markers.add(startMarker)
        }
        
        mapView.invalidate()
    }

    private fun setupListeners() {
        binding.btnClearMap.setOnClickListener {
            trailPoints.clear()
            polyline.setPoints(emptyList())
            markers.forEach { mapView.overlays.remove(it) }
            markers.clear()
            mapView.invalidate()
        }

        binding.btnSaveTrail.setOnClickListener {
            saveTrail()
        }
        
        binding.btnSelectPhoto.setOnClickListener {
            showImageSourceDialog()
        }
    }

    private fun saveTrail() {
        // Validate name
        if (!com.example.trilhospt.utils.ValidationUtils.validateNotEmpty(binding.etTrailName, "Nome")) return
        if (!com.example.trilhospt.utils.ValidationUtils.validateMinLength(binding.etTrailName, 3, "Nome")) return
        
        // Validate description
        if (!com.example.trilhospt.utils.ValidationUtils.validateNotEmpty(binding.etTrailDescription, "Descrição")) return
        if (!com.example.trilhospt.utils.ValidationUtils.validateMinLength(binding.etTrailDescription, 10, "Descrição")) return
        
        // Validate distance (0.1 to 1000 km)
        val distance = com.example.trilhospt.utils.ValidationUtils.validateNumberRange(
            binding.etDistance, 0.1, 1000.0, "Distância"
        ) ?: return
        
        // Validate duration (1 minute to 7 days)
        val duration = com.example.trilhospt.utils.ValidationUtils.validatePositiveInt(
            binding.etDuration, "Duração"
        ) ?: return
        
        if (duration > 10080) {
            binding.etDuration.error = "Duração muito longa (máximo 7 dias)"
            binding.etDuration.requestFocus()
            return
        }
        
        // Validate difficulty selection
        val difficulty = when(binding.chipGroupDifficulty.checkedChipId) {
            binding.chipEasy.id -> "easy"
            binding.chipHard.id -> "hard"
            else -> "moderate"
        }
        
        // Validate trail points
        if (trailPoints.size < 2) {
            Toast.makeText(
                requireContext(), 
                "⚠️ Por favor, marque pelo menos 2 pontos no mapa para criar o percurso",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        
        // All validations passed
        val name = binding.etTrailName.text.toString().trim()
        val description = binding.etTrailDescription.text.toString().trim()
        val gpxData = trailPoints.map { 
            mapOf("lat" to it.latitude, "lng" to it.longitude)
        }
        
        val trailDto = TrailDto(
            name = name,
            description = description,
            difficulty = difficulty,
            distance = distance,
            duration = duration,
            gpxData = gpxData
        )
        
        viewModel.createTrail(trailDto)
    }

    private fun setupObservers() {
        viewModel.createTrailStatus.observe(viewLifecycleOwner) { resource ->
            when(resource) {
                is Resource.Loading -> {
                    binding.btnSaveTrail.isEnabled = false
                    binding.btnSaveTrail.text = "A guardar..."
                }
                is Resource.Success -> {
                    // Trail created, now check if we need to upload photo
                    val trail = resource.data
                    if (trail?.id != null && selectedImageUri != null) {
                        binding.btnSaveTrail.text = "A carregar foto..."
                        prepareUpload(selectedImageUri!!, trail.id)
                    } else {
                        Toast.makeText(requireContext(), "Trilho criado com sucesso!", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                }
                is Resource.Error -> {
                    binding.btnSaveTrail.isEnabled = true
                    binding.btnSaveTrail.text = "Guardar Trilho"
                    Toast.makeText(requireContext(), "Erro ao criar trilho: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.uploadPhotoStatus.observe(viewLifecycleOwner) { resource ->
             when(resource) {
                is Resource.Loading -> {
                     binding.btnSaveTrail.text = "A enviar foto..."
                }
                is Resource.Success -> {
                    Toast.makeText(requireContext(), "Trilho e foto guardados!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                is Resource.Error -> {
                    // Even if photo fails, trail was created. Show warning and exit.
                    Toast.makeText(requireContext(), "Trilho criado, mas erro na foto: ${resource.message}", Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun showImageSourceDialog() {
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
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
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
        }
    }

    private fun prepareUpload(uri: android.net.Uri, trailId: Int) {
        try {
            val contentResolver = requireContext().contentResolver
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            val extension = android.webkit.MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"

            val inputStream = contentResolver.openInputStream(uri)
            val file = java.io.File(requireContext().cacheDir, "upload_trail_temp.$extension")
            val outputStream = java.io.FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            val requestFile = okhttp3.RequestBody.create(mimeType.toMediaTypeOrNull(), file)
            val body = okhttp3.MultipartBody.Part.createFormData("image", file.name, requestFile)
            
            // Get first point for location or default
            val lat = trailPoints.firstOrNull()?.latitude ?: 0.0
            val lng = trailPoints.firstOrNull()?.longitude ?: 0.0
            val desc = "Foto principal do trilho"

            viewModel.uploadPhoto(body, trailId, lat, lng, desc)

        } catch (e: Exception) {
            Toast.makeText(context, "Erro ao processar imagem: ${e.message}", Toast.LENGTH_SHORT).show()
            // Should probably finish anyway
            findNavController().popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
