package com.example.trilhospt.ui.trails

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.example.trilhospt.R
import com.example.trilhospt.data.remote.dto.ReviewDto
import com.example.trilhospt.data.remote.dto.TrailDto
import com.example.trilhospt.data.repository.ApiRepository
import com.example.trilhospt.data.remote.api.RetrofitClient
import com.example.trilhospt.databinding.FragmentTrailDetailBinding
import com.example.trilhospt.ui.ViewModelFactory
import com.example.trilhospt.ui.adapters.ReviewAdapter
import com.example.trilhospt.utils.Resource
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import com.example.trilhospt.ui.adapters.PhotoAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.example.trilhospt.data.remote.dto.PointOfInterestDto

class TrailDetailFragment : Fragment() {

    private var _binding: FragmentTrailDetailBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: TrailViewModel
    private lateinit var mapView: MapView
    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var photoAdapter: PhotoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrailDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val trailId = arguments?.getInt("trailId") ?: return
        
        val repository = ApiRepository()
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[TrailViewModel::class.java]

        setupMap()
        setupReviews()
        
        photoAdapter = com.example.trilhospt.ui.adapters.PhotoAdapter(emptyList()) { url ->
            val dialog = com.example.trilhospt.ui.ImageDetailDialogFragment.newInstance(url)
            dialog.show(parentFragmentManager, "image_detail")
        }
        binding.vpPhotos.adapter = photoAdapter
        
        viewModel.getTrailDetail(trailId)
        viewModel.getCurrentUser()
        
        setupObservers(trailId)
    }

    private fun setupObservers(trailId: Int) {
        viewModel.trailDetail.observe(viewLifecycleOwner) { resource ->
            when(resource) {
                is Resource.Loading -> showLoading()
                is Resource.Success<*> -> {
                    hideLoading()
                    displayTrailDetails(resource.data as com.example.trilhospt.data.remote.dto.TrailDto)
                }
                is Resource.Error -> {
                    hideLoading()
                    showError(resource.message)
                }
            }
        }

        viewModel.currentUser.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                if (resource.data.isStaff) {
                    binding.adminActions.visibility = View.VISIBLE
                    setupAdminActions(trailId)
                }
            }
        }

        viewModel.deleteStatus.observe(viewLifecycleOwner) { resource ->
            when(resource) {
                is Resource.Loading -> { }
                is Resource.Success -> {
                    Toast.makeText(requireContext(), "Trilho eliminado!", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    showError(resource.message)
                }
            }
        }

        viewModel.reviewStatus.observe(viewLifecycleOwner) { resource ->
            when(resource) {
                is Resource.Loading -> { }
                is Resource.Success<*> -> {
                    Toast.makeText(requireContext(), "Avaliação publicada!", Toast.LENGTH_SHORT).show()
                    viewModel.getTrailDetail(trailId) // Refresh
                }
                is Resource.Error -> {
                    showError(resource.message)
                }
            }
        }


        binding.btnAddReview.setOnClickListener {
            showAddReviewDialog(trailId)
        }

        viewModel.completionStatus.observe(viewLifecycleOwner) { resource ->
            when(resource) {
                is Resource.Loading -> {
                     binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                     binding.progressBar.visibility = View.GONE
                     val isCompleted = resource.data
                     if (isCompleted) {
                         Toast.makeText(requireContext(), "Trilho marcado como concluído!", Toast.LENGTH_SHORT).show()
                     } else {
                         Toast.makeText(requireContext(), "Conclusão removida!", Toast.LENGTH_SHORT).show()
                     }
                }
                is Resource.Error -> {
                     binding.progressBar.visibility = View.GONE
                     Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun displayTrailDetails(trail: TrailDto) {
        binding.tvTrailName.text = trail.name
        binding.tvDistance.text = "${trail.distance ?: 0.0} km"
        binding.tvDuration.text = formatDuration(trail.duration ?: 0)
        binding.tvDifficulty.text = translateDifficulty(trail.difficulty)
        binding.tvDescription.text = trail.description
        
        // Rating
        val rating = trail.avgRating ?: 0.0
        binding.tvRating.text = String.format("%.1f", rating)
        binding.tvReviewCount.text = "(${trail.totalReviews ?: 0} avaliações)"

        // Photos Gallery
        val imageUrls = getTrailImageUrls(trail)
        photoAdapter.updatePhotos(imageUrls)
        
        // Setup TabLayout with ViewPager2
        if (imageUrls.size > 1) {
            binding.tabIndicator.visibility = View.VISIBLE
            TabLayoutMediator(binding.tabIndicator, binding.vpPhotos) { _, _ -> }.attach()
        } else {
            binding.tabIndicator.visibility = View.GONE
        }

        // Map
        drawTrailOnMap(trail)
        if (!trail.pois.isNullOrEmpty()) {
            drawPOIsOnMap(trail.pois)
        }


        // Set Toolbar Title
        (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title = trail.name

        // Reviews
        trail.reviews?.let {
            reviewAdapter.updateReviews(it)
        }
        
        // Completion Button
        if (trail.completedTrailId != null) {
            binding.btnMarkCompleted.text = "Concluído"
            binding.btnMarkCompleted.setIconResource(android.R.drawable.checkbox_on_background)
            binding.btnMarkCompleted.setOnClickListener {
                 AlertDialog.Builder(requireContext())
                    .setTitle("Remover conclusão?")
                    .setPositiveButton("Sim") { _, _ ->
                        trail.id?.let { id -> viewModel.unmarkAsCompleted(trail.completedTrailId, id) }
                    }
                    .setNegativeButton("Não", null)
                    .show()
            }
        } else {
            binding.btnMarkCompleted.text = "Marcar como Concluído"
            binding.btnMarkCompleted.icon = null
            binding.btnMarkCompleted.setOnClickListener {
                 trail.id?.let { id -> viewModel.markAsCompleted(id) }
            }
        }
    }

    private fun translateDifficulty(difficulty: String?): String {
        return when(difficulty?.lowercase()) {
            "easy" -> "Fácil"
            "moderate" -> "Moderado"
            "hard" -> "Difícil"
            "expert" -> "Expert"
            else -> difficulty ?: "N/A"
        }
    }

    private fun setupReviews() {
        reviewAdapter = ReviewAdapter(emptyList()) { userId ->
            val bundle = Bundle().apply {
                putInt("userId", userId)
            }
            findNavController().navigate(R.id.action_trailDetailFragment_to_profileFragment, bundle)
        }
        binding.rvReviews.adapter = reviewAdapter
    }

    private fun showAddReviewDialog(trailId: Int) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_review, null)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)
        val etComment = dialogView.findViewById<EditText>(R.id.etComment)

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Avaliar Trilho")
            .setPositiveButton("Publicar") { _, _ ->
                val rating = ratingBar.rating.toInt()
                val comment = etComment.text.toString()
                
                if (rating == 0) {
                    Toast.makeText(requireContext(), "Por favor selecione uma nota", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val review = ReviewDto(
                    trail = trailId,
                    rating = rating,
                    comment = comment
                )
                viewModel.createReview(review)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun setupMap() {
        // Essential OsmDroid configuration
        org.osmdroid.config.Configuration.getInstance().load(
            requireContext(),
            android.preference.PreferenceManager.getDefaultSharedPreferences(requireContext())
        )
        org.osmdroid.config.Configuration.getInstance().userAgentValue = requireContext().packageName

        mapView = binding.mapView
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)
        
        // Default center in Portugal
        mapView.controller.setCenter(GeoPoint(39.5, -8.0))
    }

    private fun drawTrailOnMap(trail: TrailDto) {
        val gpxData = trail.gpxData ?: return
        
        try {
            val gson = Gson()
            val jsonString = gson.toJson(gpxData)
            val listType = object : TypeToken<List<Map<String, Any>>>() {}.type
            val points: List<Map<String, Any>> = gson.fromJson(jsonString, listType)

            if (points.isEmpty()) return

            val polyline = Polyline()
            polyline.outlinePaint.color = android.graphics.Color.BLUE
            polyline.outlinePaint.strokeWidth = 10f
            
            val geoPoints = ArrayList<GeoPoint>()

            for (point in points) {
                val lat = (point["lat"] ?: point["latitude"])?.toString()?.toDoubleOrNull() ?: continue
                val lng = (point["lng"] ?: point["longitude"])?.toString()?.toDoubleOrNull() ?: continue
                geoPoints.add(GeoPoint(lat, lng))
            }

            if (geoPoints.isEmpty()) return

            polyline.setPoints(geoPoints)
            mapView.overlays.clear()
            mapView.overlays.add(polyline)

            // Add start marker
            val startMarker = org.osmdroid.views.overlay.Marker(mapView)
            startMarker.position = geoPoints[0]
            startMarker.title = "Início"
            mapView.overlays.add(startMarker)

            val boundingBox = BoundingBox.fromGeoPoints(geoPoints)
            
            mapView.post {
                try {
                    // Reduce padding to 50 to avoid zoom issues on small views
                    mapView.zoomToBoundingBox(boundingBox, true, 50)
                } catch (e: Exception) {
                    mapView.controller.setCenter(geoPoints[0])
                    mapView.controller.setZoom(15.0)
                }
            }
            mapView.invalidate()
        } catch (e: Exception) {
            android.util.Log.e("TrailDetailFragment", "Error parsing GPX data", e)
        }
    }

    private fun drawPOIsOnMap(pois: List<PointOfInterestDto>) {
        // Add markers for each POI with custom styling
        for (poi in pois) {
             val lat = poi.latitude ?: continue
             val lng = poi.longitude ?: continue
             
             val marker = org.osmdroid.views.overlay.Marker(mapView)
             marker.position = GeoPoint(lat, lng)
             marker.title = poi.typeDisplay ?: "Ponto de Interesse"
             marker.snippet = poi.description ?: ""
             
             // Custom emoji icon based on type
             val emoji = when (poi.type) {
                 "viewpoint" -> "🔭"
                 "waterfall" -> "💧"
                 "fountain" -> "⛲"
                 "parking" -> "🅿️"
                 else -> "📍"
             }
             
             // Create custom icon with larger emoji
             val icon = createEmojiIcon(emoji)
             marker.icon = icon
             marker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM)
             
             mapView.overlays.add(marker)
        }
        mapView.invalidate()
    }
    
    private fun createEmojiIcon(emoji: String): android.graphics.drawable.Drawable {
        // Create a bitmap with the emoji
        val size = 120 // Size in pixels
        val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        
        // Draw emoji text
        val paint = android.graphics.Paint().apply {
            textSize = 80f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
        
        val x = size / 2f
        val y = size / 2f - ((paint.descent() + paint.ascent()) / 2)
        canvas.drawText(emoji, x, y, paint)
        
        return android.graphics.drawable.BitmapDrawable(resources, bitmap)
    }
    
    private fun formatDuration(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
    }
    
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvError.visibility = View.GONE
        binding.galleryContainer.visibility = View.GONE
    }
    
    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.galleryContainer.visibility = View.VISIBLE
    }
    
    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
    
    private fun getTrailImageUrls(trail: TrailDto): List<String> {
        val urls = mutableListOf<String>()
        
        // Add main photo if exists
        trail.firstPhotoUrl?.let { urls.add(buildFullUrl(it)!!) }
        
        // Add all from photos array
        trail.photos?.forEach { photo ->
            val url = buildFullUrl(photo.image)
            if (url != null && !urls.contains(url)) {
                urls.add(url)
            }
        }
        
        return urls
    }
    
    private fun buildFullUrl(path: String?): String? {
        if (path == null) return null
        if (path.startsWith("http")) return path
        val baseUrl = RetrofitClient.BASE_URL.trimEnd('/')
        return "$baseUrl/${path.trimStart('/')}"
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
    private fun setupAdminActions(trailId: Int) {
        // Delete Trail with confirmation
        binding.btnDeleteTrail.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("⚠️ Eliminar Trilho")
                .setMessage("Tens a certeza que queres eliminar este trilho permanentemente?\n\nEsta ação não pode ser revertida e irá eliminar:\n• Todas as fotos\n• Todas as avaliações\n• Todos os dados do trilho")
                .setPositiveButton("Eliminar") { _, _ ->
                    viewModel.deleteTrail(trailId)
                }
                .setNegativeButton("Cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
        }

        // Edit Trail - Navigate to edit screen
        binding.btnEditTrail.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("trailId", trailId)
            }
            findNavController().navigate(R.id.action_trailDetailFragment_to_editTrailFragment, bundle)
        }
    }

}
