package com.mikirinkode.pikul.feature.owner.product

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.databinding.FragmentProductListBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ProductListFragment : Fragment() {
    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductViewModel by viewModels()

    private val adapter: ProductListAdapter by lazy {
        ProductListAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeProductList()
        onClickAction()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initView() {
        binding.apply {
            rvProducts.layoutManager = LinearLayoutManager(requireContext())
            rvProducts.adapter = adapter
        }
    }

    private fun observeProductList() {
        binding.apply {
            viewModel.getProductList().observe(viewLifecycleOwner) { result ->
                when (result) {
                    is PikulResult.Loading -> {
                        layoutLoading.visibility = View.VISIBLE
                    }
                    is PikulResult.LoadingWithProgress -> {}
                    is PikulResult.Error -> {
                        layoutLoading.visibility = View.GONE
                        Toast.makeText(requireContext(), result.errorMessage, Toast.LENGTH_SHORT)
                            .show()
                    }
                    is PikulResult.Success -> {
                        layoutLoading.visibility = View.GONE
                        if (result.data.isNotEmpty()) {
                            adapter.setData(result.data)
                        } else {

                        }
                    }
                }
            }
        }
    }

    private fun onClickAction(){
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                Navigation.findNavController(binding.root).navigateUp()
            }

            fabCreateProduct.setOnClickListener {
                val action = ProductListFragmentDirections.actionCreateProduct()
                Navigation.findNavController(binding.root).navigate(action)
            }
        }
    }
}