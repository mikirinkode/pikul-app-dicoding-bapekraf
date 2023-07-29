package com.mikirinkode.pikul.feature.merchant.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.databinding.FragmentMerchantOnGoingTransactionBinding
import com.mikirinkode.pikul.databinding.FragmentOngoingOrderBinding
import com.mikirinkode.pikul.feature.customer.transaction.TransactionAdapter
import com.mikirinkode.pikul.feature.customer.transaction.TransactionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MerchantOnGoingTransactionFragment : Fragment(), MerchantTransactionAdapter.ClickListener {
    private var _binding: FragmentMerchantOnGoingTransactionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MerchantTransactionViewModel by viewModels()

    private lateinit var adapter: MerchantTransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMerchantOnGoingTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = MerchantTransactionAdapter(this)
        initRecyclerView()
        observeData()
        onClickAction()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initRecyclerView() {
        binding.apply {
            rvTransactions.layoutManager = LinearLayoutManager(requireContext())
            rvTransactions.adapter = adapter
        }
    }

    private fun observeData() {
        viewModel.getOnGoingTransactionList().observe(viewLifecycleOwner) { result ->
            when (result) {
                is PikulResult.Loading -> {}
                is PikulResult.LoadingWithProgress -> {} // TODO
                is PikulResult.Error -> {}
                is PikulResult.Success -> {
                    adapter.setData(result.data)
                }
            }
        }
    }

    override fun onButtonClickListener(action: String, transactionId: String, customerId: String) {
        if (action == MerchantTransactionAdapter.ACTION_PROCESS_ORDER) {
            viewModel.updateTransactionOnProcess(transactionId, customerId)
                .observe(viewLifecycleOwner) { result ->
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
                            Toast.makeText(requireContext(), "Berhasil Memperbarui Data", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        } else if (action == MerchantTransactionAdapter.ACTION_ORDER_READY_TO_PICK_UP) {
            viewModel.updateTransactionReadyToPickup(transactionId, customerId)
                .observe(viewLifecycleOwner) { result ->
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
                            Toast.makeText(requireContext(), "Berhasil Memperbarui Data", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }
    }

    private fun onClickAction() {

    }
}