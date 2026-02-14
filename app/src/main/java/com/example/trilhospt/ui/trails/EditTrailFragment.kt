package com.example.trilhospt.ui.trails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.trilhospt.data.remote.dto.TrailDto
import com.example.trilhospt.data.repository.ApiRepository
import com.example.trilhospt.databinding.FragmentEditTrailBinding
import com.example.trilhospt.ui.ViewModelFactory
import com.example.trilhospt.utils.Resource
import com.example.trilhospt.utils.ValidationUtils

class EditTrailFragment : Fragment() {

    private var _binding: FragmentEditTrailBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: TrailViewModel
    private var trailId: Int = -1
    private var currentTrail: TrailDto? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditTrailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        trailId = arguments?.getInt("trailId") ?: -1
        if (trailId == -1) {
            Toast.makeText(context, "Erro: ID do trilho inválido", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        val repository = ApiRepository()
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[TrailViewModel::class.java]

        (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title = "Editar Trilho"

        setupListeners()
        setupObservers()
        
        // Load trail data
        viewModel.getTrailDetail(trailId)
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            saveTrail()
        }
        
        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupObservers() {
        // Observe trail detail to pre-fill form
        viewModel.trailDetail.observe(viewLifecycleOwner) { resource ->
            when(resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    currentTrail = resource.data
                    prefillForm(resource.data)
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Erro ao carregar trilho", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
        }

        // Observe update status
        viewModel.updateTrailStatus.observe(viewLifecycleOwner) { resource ->
            when(resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSave.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "✅ Trilho atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    Toast.makeText(context, "Erro: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun prefillForm(trail: TrailDto) {
        binding.etTrailName.setText(trail.name ?: "")
        binding.etTrailDescription.setText(trail.description ?: "")
        binding.etDistance.setText(trail.distance?.toString() ?: "")
        binding.etDuration.setText(trail.duration?.toString() ?: "")
        
        // Set difficulty
        when(trail.difficulty?.lowercase()) {
            "easy" -> binding.chipEasy.isChecked = true
            "hard" -> binding.chipHard.isChecked = true
            else -> binding.chipModerate.isChecked = true
        }
    }

    private fun saveTrail() {
        // Validate name
        if (!ValidationUtils.validateNotEmpty(binding.etTrailName, "Nome")) return
        if (!ValidationUtils.validateMinLength(binding.etTrailName, 3, "Nome")) return
        
        // Validate description
        if (!ValidationUtils.validateNotEmpty(binding.etTrailDescription, "Descrição")) return
        if (!ValidationUtils.validateMinLength(binding.etTrailDescription, 10, "Descrição")) return
        
        // Validate distance (0.1 to 1000 km)
        val distance = ValidationUtils.validateNumberRange(
            binding.etDistance, 0.1, 1000.0, "Distância"
        ) ?: return
        
        // Validate duration (1 minute to 7 days)
        val duration = ValidationUtils.validatePositiveInt(
            binding.etDuration, "Duração"
        ) ?: return
        
        if (duration > 10080) {
            binding.etDuration.error = "Duração muito longa (máximo 7 dias)"
            binding.etDuration.requestFocus()
            return
        }
        
        // Get difficulty
        val difficulty = when(binding.chipGroupDifficulty.checkedChipId) {
            binding.chipEasy.id -> "easy"
            binding.chipHard.id -> "hard"
            else -> "moderate"
        }
        
        // All validations passed
        val name = binding.etTrailName.text.toString().trim()
        val description = binding.etTrailDescription.text.toString().trim()
        
        val updatedTrail = TrailDto(
            name = name,
            description = description,
            difficulty = difficulty,
            distance = distance,
            duration = duration,
            gpxData = currentTrail?.gpxData // Keep existing GPX data
        )
        
        viewModel.updateTrail(trailId, updatedTrail)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
