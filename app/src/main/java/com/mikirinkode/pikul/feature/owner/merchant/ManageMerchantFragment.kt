package com.mikirinkode.pikul.feature.owner.merchant

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.databinding.FragmentJobVacancyBinding
import com.mikirinkode.pikul.databinding.FragmentManageMerchantBinding
import dagger.hilt.android.AndroidEntryPoint

// TODO: UNIMPLEMENTED
@AndroidEntryPoint
class ManageMerchantFragment : Fragment() {
    private var _binding: FragmentManageMerchantBinding? = null
    private val binding get() = _binding!!

    private val viewModel : ManageMerchantViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentManageMerchantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onClickAction()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onClickAction() {
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                Navigation.findNavController(binding.root).navigateUp()
            }

            fabAddMerchant.setOnClickListener {
                val action = ManageMerchantFragmentDirections.actionAddMerchant()
                Navigation.findNavController(binding.root).navigate(action)
            }
        }
    }
}