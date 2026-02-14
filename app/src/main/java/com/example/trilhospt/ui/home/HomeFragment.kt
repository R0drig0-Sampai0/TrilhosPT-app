package com.example.trilhospt.ui.home

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.trilhospt.R
import com.example.trilhospt.data.remote.dto.TrailDto
import com.example.trilhospt.data.repository.ApiRepository
import com.example.trilhospt.databinding.FragmentHomeBinding
import com.example.trilhospt.ui.ViewModelFactory
import com.example.trilhospt.ui.adapters.HorizontalTrailAdapter
import com.example.trilhospt.ui.trails.TrailViewModel
import com.example.trilhospt.utils.Resource

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: TrailViewModel
    private lateinit var adapter: HorizontalTrailAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = ApiRepository()
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[TrailViewModel::class.java]

        setupRecyclerView()
        setupObservers()

        viewModel.getTrails()
        viewModel.getCurrentUser()
    }

    private fun setupRecyclerView() {
        adapter = HorizontalTrailAdapter(emptyList()) { trail ->
            val bundle = Bundle().apply {
                putInt("trailId", trail.id ?: 0)
            }
            findNavController().navigate(R.id.action_homeFragment_to_trailDetailFragment, bundle)
        }
        binding.rvRecentTrails.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.trails.observe(viewLifecycleOwner) { resource ->
            when(resource) {
                is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val allTrails = resource.data
                    if (allTrails.isNotEmpty()) {
                        // Display the first trail as "Trail of the Day"
                        displayFeaturedTrail(allTrails.first())
                        
                        // Display the rest (or all) in the horizontal list
                        adapter.updateTrails(allTrails)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }

        viewModel.currentUser.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                binding.tvWelcome.text = "Olá, ${resource.data.username}!"
            }
        }
    }

    private fun displayFeaturedTrail(trail: TrailDto) {
        binding.tvFeaturedName.text = trail.name
        binding.tvFeaturedDifficulty.text = trail.difficulty?.uppercase() ?: "N/A"
        binding.tvFeaturedDistance.text = "${trail.distance ?: 0.0} km"
        
        val photoUrl = trail.firstPhotoUrl ?: trail.photos?.firstOrNull()?.image
        if (photoUrl != null) {
            val fullUrl = if (photoUrl.startsWith("http")) photoUrl else "http://10.129.146.48:8000$photoUrl"
            binding.ivFeaturedImage.load(fullUrl) {
                crossfade(true)
                placeholder(R.drawable.bg_gradient_overlay)
                error(R.drawable.bg_gradient_overlay)
            }
        }

        binding.cardFeatured.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("trailId", trail.id ?: 0)
            }
            findNavController().navigate(R.id.action_homeFragment_to_trailDetailFragment, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
