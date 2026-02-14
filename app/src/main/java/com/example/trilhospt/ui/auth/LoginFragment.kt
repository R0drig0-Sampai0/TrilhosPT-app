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
import com.example.trilhospt.databinding.FragmentLoginBinding
import com.example.trilhospt.ui.ViewModelFactory
import com.example.trilhospt.utils.Resource
import com.example.trilhospt.data.remote.api.AuthInterceptor

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = ApiRepository()
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            
            if (username.isNotEmpty() && password.isNotEmpty()) {
                viewModel.login(username, password)
            } else {
                Toast.makeText(requireContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.tvGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        viewModel.loginState.observe(viewLifecycleOwner) { resource ->
            when(resource) {
                is Resource.Loading -> {
                    // Show loading
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnLogin.isEnabled = false
                    binding.btnLogin.text = "A entrar..."
                    binding.etUsername.isEnabled = false
                    binding.etPassword.isEnabled = false
                }
                is Resource.Success -> {
                    // Hide loading
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    
                    // Save token using AuthInterceptor
                    val authInterceptor = AuthInterceptor(requireContext())
                    resource.data.token?.let { authInterceptor.saveToken(it) }
                    
                    // Update menu visibility in MainActivity
                    activity?.invalidateOptionsMenu()
                    
                    Toast.makeText(requireContext(), "Bem-vindo!", Toast.LENGTH_SHORT).show()
                    
                    // Navigate and clear login from backstack
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                }
                is Resource.Error -> {
                    // Hide loading and show error
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = getString(R.string.login)
                    binding.etUsername.isEnabled = true
                    binding.etPassword.isEnabled = true
                    
                    Toast.makeText(requireContext(), "Erro: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
