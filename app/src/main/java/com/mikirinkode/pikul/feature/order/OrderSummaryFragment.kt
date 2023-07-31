package com.mikirinkode.pikul.feature.order

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.Product
import com.mikirinkode.pikul.databinding.FragmentOrderSummaryBinding
import com.mikirinkode.pikul.feature.detail.DetailPickupPointMapsActivity
import com.mikirinkode.pikul.feature.payment.MidtransWebViewActivity
import com.mikirinkode.pikul.utils.MoneyHelper
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

    private fun initAdapter() {
        adapter = ProductSummaryAdapter(args.merchantId, this) // TODO
    }

    private fun initRecyclerView() {
        binding.apply {
            rvProducts.layoutManager = LinearLayoutManager(requireContext())
            rvProducts.adapter = adapter
        }
    }

    private fun handleArgs() {
        adapter.setData(args.products.toList())
        updateTotalPriceView()
        initView()
    }

    private fun initView() {
        binding.apply {
            tvMerchantAddress.text = args.transaction.pickupAddress
        }
    }

    private fun updateTotalPriceView() {
        binding.apply {
            tvTotalItem.text = adapter.getTotalOrderItemAmount().toString()
            tvTotalBilling.text = MoneyHelper.getFormattedPrice(adapter.getTotalOrderBilling())
        }
    }

    override fun onProductOrderAmountChanged() {
        updateTotalPriceView()
    }

    private fun onClickAction() {
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                Navigation.findNavController(binding.root).navigateUp()
            }

            btnSeeLocationOnMap.setOnClickListener {
                startActivity(
                    Intent(requireContext(), DetailPickupPointMapsActivity::class.java)
                        .putExtra(
                            DetailPickupPointMapsActivity.EXTRA_INTENT_COORDINATES,
                            args.transaction.pickupCoordinates ?: ""
                        )
                        .putExtra(
                            DetailPickupPointMapsActivity.EXTRA_INTENT_ADDRESS,
                            args.transaction.pickupAddress ?: ""
                        )
                )
            }

            btnPay.setOnClickListener {
                val transaction = args.transaction
                val totalItem = adapter.getTotalOrderItemAmount()
                val totalBilling = adapter.getTotalOrderBilling()
                val pickupAddress = transaction.pickupAddress ?: ""
                val pickupCoordinates = transaction.pickupCoordinates ?: ""
                val businessId = transaction.businessId ?: ""
                val businessName = transaction.businessName ?: ""
                val merchantId = transaction.merchantId ?: ""
                val merchantName = transaction.merchantName ?: ""
                val customerName = transaction.customerName ?: ""
                val listOfProduct = adapter.getBookedProducts()

                viewModel.createTransaction(
                    totalItem,
                    totalBilling,
                    pickupAddress,
                    pickupCoordinates,
                    businessId,
                    businessName,
                    merchantId,
                    merchantName,
                    customerName,
                    listOfProduct
                ).observe(viewLifecycleOwner) { result ->
                    when (result) {
                        is PikulResult.Loading -> {
                            layoutLoading.visibility = View.VISIBLE
                        }
                        is PikulResult.LoadingWithProgress -> {}
                        is PikulResult.Error -> {
                            layoutLoading.visibility = View.GONE
                            Toast.makeText(requireContext(), result.errorMessage, Toast.LENGTH_SHORT).show()
                        } // TODO
                        is PikulResult.Success -> {
                            layoutLoading.visibility = View.GONE
                            val paymentUrl = result.data.paymentUrl ?: ""
                            val transactionId = result.data.transactionId ?: ""
//                            val action =
//                                OrderSummaryFragmentDirections.actionPayTransaction(paymentUrl, transactionId)
//                            Navigation.findNavController(binding.root).navigate(action)

                            startActivity(
                                Intent(
                                    requireContext(),
                                    MidtransWebViewActivity::class.java
                                )
                                    .putExtra(
                                        MidtransWebViewActivity.EXTRA_INTENT_TRANSACTION_ID,
                                        transactionId
                                    )
                                    .putExtra(
                                        MidtransWebViewActivity.EXTRA_INTENT_PAYMENT_URL,
                                        paymentUrl
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}