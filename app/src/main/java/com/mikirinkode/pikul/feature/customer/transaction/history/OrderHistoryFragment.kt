package com.mikirinkode.pikul.feature.customer.transaction.history

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.databinding.FragmentOrderHistoryBinding
import com.mikirinkode.pikul.feature.customer.transaction.TransactionAdapter
import com.mikirinkode.pikul.feature.customer.transaction.TransactionViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class OrderHistoryFragment : Fragment() {
    private var _binding: FragmentOrderHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TransactionViewModel by viewModels()

    private val adapter: TransactionAdapter by lazy {
        TransactionAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentOrderHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                is PikulResult.Loading -> {}
                is PikulResult.LoadingWithProgress -> {} // TODO
                is PikulResult.Error -> {}
                is PikulResult.Success -> {
                    adapter.setData(result.data)
                }
            }
        }
    }

    private fun onClickAction(){}
}