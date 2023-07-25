package com.mikirinkode.pikul.feature.owner.product

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.databinding.FragmentCreateProductBinding
import com.mikirinkode.pikul.databinding.FragmentJobVacancyBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CreateProductFragment: Fragment() {
    private var _binding: FragmentCreateProductBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentCreateProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}