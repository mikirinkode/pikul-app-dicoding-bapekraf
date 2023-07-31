package com.mikirinkode.pikul.feature.owner.transaction

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.databinding.FragmentOwnerTransactionListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OwnerTransactionListFragment : Fragment() {

    private var _binding: FragmentOwnerTransactionListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OwnerTransactionListViewModel by viewModels()

    private val adapter: OwnerTransactionListAdapter by lazy {
        OwnerTransactionListAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentOwnerTransactionListBinding.inflate(inflater, container, false)
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

    private fun observeData(){
        binding.apply {
            viewModel.getAllOwnerTransactionList().observe(viewLifecycleOwner){ result ->
                when (result) {
                    is PikulResult.Loading -> {
                        layoutListLoading.visibility = View.VISIBLE
                    }
                    is PikulResult.LoadingWithProgress -> {} // TODO
                    is PikulResult.Error -> {
                        Toast.makeText(requireContext(), result.errorMessage, Toast.LENGTH_SHORT).show()
                    }
                    is PikulResult.Success -> {
                        layoutListLoading.visibility = View.GONE
                        adapter.setData(result.data)
                        if (result.data.isEmpty()){
                            layoutOnEmptyData.visibility = View.VISIBLE
                        } else {
                            layoutOnEmptyData.visibility = View.GONE
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
        }
    }
}