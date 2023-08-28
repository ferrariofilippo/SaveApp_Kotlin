package com.ferrariofilippo.saveapp.view

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ferrariofilippo.saveapp.MainActivity
import com.ferrariofilippo.saveapp.databinding.FragmentHomeBinding
import com.ferrariofilippo.saveapp.view.viewmodels.HomeViewModel

class HomeFragment : Fragment() {
    private lateinit var viewModel: HomeViewModel

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Overrides
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding
            .inflate(inflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                vm = viewModel
            }

        setupUI()

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Methods
    private fun setupUI() {
        binding.settingsButton.setOnClickListener {
            (activity as MainActivity).goToSettings()
        }

        binding.subscriptionsButton.setOnClickListener {
            (activity as MainActivity).goToSubscriptions()
        }
    }
}
