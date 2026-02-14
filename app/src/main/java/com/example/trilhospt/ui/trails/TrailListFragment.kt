package com.example.trilhospt.ui.trails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trilhospt.R
import com.example.trilhospt.data.repository.ApiRepository
import com.example.trilhospt.databinding.FragmentTrailListBinding
import com.example.trilhospt.ui.ViewModelFactory
import com.example.trilhospt.ui.adapters.TrailAdapter
import com.example.trilhospt.utils.Resource

class TrailListFragment : Fragment() {

    private var _binding: FragmentTrailListBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: TrailViewModel
    private lateinit var adapter: TrailAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrailListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = ApiRepository()
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[TrailViewModel::class.java]

        (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title = "Explorar Trilhos"

        setupRecyclerView()
        setupSearch()
        setupFilters()
        setupWelcome()
        setupSwipeRefresh()

        binding.fabAddTrail.setOnClickListener {
            findNavController().navigate(R.id.action_trailListFragment_to_createTrailFragment)
        }
        
        viewModel.getTrails()
        
        viewModel.trails.observe(viewLifecycleOwner) { resource ->
            when(resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.tvError.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    if (resource.data.isNullOrEmpty()) {
                        binding.tvError.text = "Nenhum trilho encontrado."
                        binding.tvError.visibility = View.VISIBLE
                        adapter.updateTrails(emptyList())
                    } else {
                        binding.tvError.visibility = View.GONE
                        adapter.updateTrails(resource.data)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    binding.tvError.text = resource.message
                    binding.tvError.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.getTrails()
        }
        // Set color scheme
        binding.swipeRefresh.setColorSchemeResources(
            R.color.primary,
            R.color.primary_light,
            R.color.accent
        )
    }

    private fun setupFilters() {
        binding.chipGroupFilters.setOnCheckedStateChangeListener { group, checkedIds ->
            val difficulty = when(checkedIds.firstOrNull()) {
                R.id.chipEasy -> "easy"
                R.id.chipMedium -> "moderate"
                R.id.chipHard -> "hard"
                else -> null
            }
            viewModel.getTrails(difficulty = difficulty)
        }
    }

    private fun setupWelcome() {
        val sharedPref = requireActivity().getSharedPreferences("TrilhosPT", android.content.Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", "")
        if (username?.isNotEmpty() == true) {
            (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title = "Olá, $username!"
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.getTrails(search = query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    viewModel.getTrails()
                }
                return true
            }
        })
    }

    private fun setupRecyclerView() {
        adapter = TrailAdapter(emptyList()) { trailId ->
            val bundle = bundleOf("trailId" to trailId)
            findNavController().navigate(R.id.action_trailListFragment_to_trailDetailFragment, bundle)
        }
        binding.rvTrails.adapter = adapter
        binding.rvTrails.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
