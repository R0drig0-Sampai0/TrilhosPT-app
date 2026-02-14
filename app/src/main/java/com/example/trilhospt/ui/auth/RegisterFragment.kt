package com.example.trilhospt.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.trilhospt.R
import com.example.trilhospt.data.repository.ApiRepository
import com.example.trilhospt.databinding.FragmentRegisterBinding
import com.example.trilhospt.ui.ViewModelFactory
import com.example.trilhospt.utils.Resource
import com.example.trilhospt.data.remote.api.AuthInterceptor

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = ApiRepository()
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            
            if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.register(username, email, password)
            } else {
                Toast.makeText(requireContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.registerState.observe(viewLifecycleOwner) { resource ->
            when(resource) {
                is Resource.Loading -> {
                    // Show loading
                }
                is Resource.Success -> {
                    // Save token using AuthInterceptor
                    val authInterceptor = AuthInterceptor(requireContext())
                    resource.data.token?.let { authInterceptor.saveToken(it) }
                    
                    // Update menu visibility
                    activity?.invalidateOptionsMenu()
                    
                    Toast.makeText(requireContext(), "Conta criada com sucesso!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
