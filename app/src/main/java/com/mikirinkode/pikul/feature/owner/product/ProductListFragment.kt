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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.Product
import com.mikirinkode.pikul.databinding.FragmentProductListBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ProductListFragment : Fragment(), ProductListAdapter.ClickListener {
    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductViewModel by viewModels()

    private lateinit var adapter: ProductListAdapter

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
        initAdapter()
        initView()
        observeProductList()
        onClickAction()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initAdapter(){
        adapter = ProductListAdapter(this)
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
                            binding.layoutOnEmptyData.visibility = View.GONE
                            binding.rvProducts.visibility = View.VISIBLE
                        } else {
                            binding.rvProducts.visibility = View.GONE
                            binding.layoutOnEmptyData.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    private fun showDeleteConfirmationDialog(productId: String){
        val title = "Hapus Produk?"
        val message = "Apakah anda yakin ingin menghapus Produk ini?"
        val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setCancelable(true)
            .setPositiveButton(getString(R.string.txt_dialog_action_delete)) { _, _ ->
                viewModel.deleteProduct(productId).observe(viewLifecycleOwner){ result ->
                    when (result) {
                        is PikulResult.Loading -> {
                            binding.layoutLoading.visibility = View.VISIBLE
                        }
                        is PikulResult.LoadingWithProgress -> {}
                        is PikulResult.Error -> {
                            binding.layoutLoading.visibility = View.GONE
                            Toast.makeText(requireContext(), result.errorMessage, Toast.LENGTH_SHORT).show()
                        }
                        is PikulResult.Success -> {
                            binding.layoutLoading.visibility = View.GONE
                            Toast.makeText(requireContext(), "Berhasil menghapus data", Toast.LENGTH_SHORT).show()
                            observeProductList()
                        }
                    }
                }
            }
            .setNegativeButton(getString(R.string.cancel)){ dialogInterface, _ ->
                dialogInterface.cancel()
            }
        val confirmationDialog = dialogBuilder.create()
        confirmationDialog.show()
    }
    override fun onDeleteClick(product: Product) {
        if (product.productId != null){
            showDeleteConfirmationDialog(product.productId!!)
        }
    }

    override fun onEditClick(product: Product) {
        val action = ProductListFragmentDirections.actionCreateProduct(
            AddProductFragment.EDIT_MODE,
            product
        )
        Navigation.findNavController(binding.root).navigate(action)
    }
    
    
    private fun onClickAction(){
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                Navigation.findNavController(binding.root).navigateUp()
            }

            fabCreateProduct.setOnClickListener {
                val action = ProductListFragmentDirections.actionCreateProduct(
                    AddProductFragment.ADD_MODE,
                    null
                )
                Navigation.findNavController(binding.root).navigate(action)
            }
        }
    }
}