package com.mikirinkode.pikul.feature.order

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.Product
import com.mikirinkode.pikul.databinding.FragmentOrderSummaryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrderSummaryFragment : Fragment(), ProductSummaryAdapter.ClickListener {
    private var _binding: FragmentOrderSummaryBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ProductSummaryAdapter

    private val args: OrderSummaryFragmentArgs by navArgs()

    private val viewModel: OrderViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentOrderSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        initRecyclerView()
        handleArgs()
        onClickAction()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initAdapter(){
        adapter = ProductSummaryAdapter(args.merchantId, this) // TODO
    }

    private fun initRecyclerView(){
        binding.apply {
            rvProducts.layoutManager = LinearLayoutManager(requireContext())
            rvProducts.adapter = adapter
        }
    }

    private fun handleArgs(){
        adapter.setData(args.products.toList())
        updateTotalPriceView()
    }

    private fun updateTotalPriceView(){
        binding.apply {
            tvTotalItem.text = adapter.getTotalOrderItemAmount().toString()
            tvTotalBilling.text = adapter.getTotalOrderBilling().toString()
        }
    }

    override fun onProductOrderAmountChanged() {
        updateTotalPriceView()
    }

    private fun onClickAction(){
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                Navigation.findNavController(binding.root).navigateUp()
            }

            btnPay.setOnClickListener {
                val totalItem = adapter.getTotalOrderItemAmount()
                val totalBilling = adapter.getTotalOrderBilling()
                val pickupAddress = ""
                val pickupCoordinates = ""
                val businessId = ""
                val businessName = ""
                val merchantId = ""
                val merchantName = ""
                viewModel.createTransaction(
                    totalItem, totalBilling, pickupAddress, pickupCoordinates, businessId, businessName, merchantId, merchantName
                ).observe(viewLifecycleOwner){ result ->
                    when (result) {
                        is PikulResult.Loading -> {}
                        is PikulResult.LoadingWithProgress -> {}
                        is PikulResult.Error -> {} // TODO
                        is PikulResult.Success -> {
                            val paymentUrl = result.data
                            val action = OrderSummaryFragmentDirections.actionPayTransaction(paymentUrl)
                            Navigation.findNavController(binding.root).navigate(action)
                        }
                    }
                }
            }
        }
    }
}