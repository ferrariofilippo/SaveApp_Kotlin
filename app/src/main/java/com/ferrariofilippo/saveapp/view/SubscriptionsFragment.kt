package com.ferrariofilippo.saveapp.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ferrariofilippo.saveapp.databinding.FragmentSubscriptionsBinding
import com.ferrariofilippo.saveapp.view.viewmodels.NewMovementViewModel
import com.ferrariofilippo.saveapp.view.viewmodels.SubscriptionsViewModel

class SubscriptionsFragment : Fragment() {
    private lateinit var viewModel: SubscriptionsViewModel

    private var _binding: FragmentSubscriptionsBinding? = null
    private val binding get() = _binding!!

    // Overrides
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubscriptionsBinding
            .inflate(inflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                vm = viewModel
            }

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[SubscriptionsViewModel::class.java]
    }

}