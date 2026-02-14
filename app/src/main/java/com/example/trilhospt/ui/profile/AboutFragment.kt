package com.example.trilhospt.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.trilhospt.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Configura o título da Toolbar
        (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title = "Sobre a Aplicação"
        
        // Aqui podes atualizar os dados dos autores manualmente se necessário
        // binding.tvAuthorName.text = "Teu Nome"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
