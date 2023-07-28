package com.mikirinkode.pikul.feature.owner.stock

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.local.LocalPreferenceConstants
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.Product
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.databinding.DialogUpdateProductStockBinding
import com.mikirinkode.pikul.databinding.FragmentJobVacancyBinding
import com.mikirinkode.pikul.databinding.FragmentManageStockBinding
import com.mikirinkode.pikul.feature.owner.product.ProductListAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// TODO CHECK AGAIN
@AndroidEntryPoint
class ManageStockFragment : Fragment(), ManageStockAdapter.ClickListener {
    private var _binding: FragmentManageStockBinding? = null
    private val binding get() = _binding!!
    private var dialogBinding: DialogUpdateProductStockBinding? = null


    @Inject
    lateinit var pref: LocalPreference
    private val user: UserAccount? by lazy {
        pref?.getObject(LocalPreferenceConstants.USER, UserAccount::class.java)
    }

    private val viewModel: ManageStockViewModel by viewModels()

    private lateinit var adapter: ManageStockAdapter

    private var updateDialog: AlertDialog? = null

    private val args: ManageStockFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentManageStockBinding.inflate(inflater, container, false)
        dialogBinding = DialogUpdateProductStockBinding.inflate(inflater)
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

    private fun initAdapter() {
        val userId : String = user?.userId ?: ""
        adapter = ManageStockAdapter(this, userId)
    }

    private fun initView() {
        binding.apply {
            rvProducts.layoutManager = LinearLayoutManager(requireContext())
            rvProducts.adapter = adapter
        }
        // used for dialog
        val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.txt_dialog_title_update_product_stok))
            .setView(dialogBinding?.root)
            .setCancelable(true)
        updateDialog = dialogBuilder.create()
    }

    private fun observeProductList() {
        binding.apply {
            viewModel.getProductList(args.businessId).observe(viewLifecycleOwner) { result ->
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


    override fun onEditClick(product: Product) {
        updateDialog?.show()
        dialogBinding?.apply {
            etProductName.setText(product.productName)


            btnCancel.setOnClickListener {
                updateDialog?.cancel()
            }

            btnEdit.setOnClickListener {
                val stock = etProductStock.text.toString().trim().toInt()
                if (stock < 0){
                    etProductStock.error = "Stock belum ditentukan"
                } else {
                    product.productId?.let { productId ->
                        viewModel.updateProductStock(
                            productId,
                            stock
                        ).observe(viewLifecycleOwner){ result ->
                            updateDialog?.dismiss()
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
                                    dialogBinding?.etProductStock?.setText("")
                                    Toast.makeText(requireContext(), "Berhasil mengubah data", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun onClickAction() {
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                Navigation.findNavController(binding.root).navigateUp()
            }
        }
    }
}