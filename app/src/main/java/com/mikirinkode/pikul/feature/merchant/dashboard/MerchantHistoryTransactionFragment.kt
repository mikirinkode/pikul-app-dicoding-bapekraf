package com.mikirinkode.pikul.feature.merchant.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.databinding.FragmentMerchantHistoryTransactionBinding
import com.mikirinkode.pikul.databinding.FragmentOngoingOrderBinding
import com.mikirinkode.pikul.feature.customer.transaction.TransactionAdapter
import com.mikirinkode.pikul.feature.customer.transaction.TransactionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MerchantHistoryTransactionFragment : Fragment(), MerchantTransactionAdapter.ClickListener {
    private var _binding: FragmentMerchantHistoryTransactionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MerchantTransactionViewModel by viewModels()

    private lateinit var adapter: MerchantTransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMerchantHistoryTransactionBinding.inflate(inflater, container, false)
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
        viewModel.getCompleteTransactionList().observe(viewLifecycleOwner) { result ->
            when (result) {
                is PikulResult.Loading -> {
                    binding.layoutListLoading.visibility = View.VISIBLE
                }
                is PikulResult.LoadingWithProgress -> {} // TODO
                is PikulResult.Error -> {}
                is PikulResult.Success -> {
                    binding.layoutListLoading.visibility = View.GONE
                    adapter.setData(result.data)
                    if (result.data.isEmpty()){
                        binding.layoutOnEmptyData.visibility = View.VISIBLE
                    } else {
                        binding.layoutOnEmptyData.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onButtonClickListener(action: String, transactionId: String, customerId: String) {
//        TODO("Not yet implemented")
    }

    private fun onClickAction(){

    }
}