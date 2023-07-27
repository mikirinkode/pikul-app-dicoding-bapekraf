package com.mikirinkode.pikul.feature.customer.transaction

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mikirinkode.pikul.data.model.PikulTransaction
import com.mikirinkode.pikul.data.model.Product
import com.mikirinkode.pikul.databinding.ItemProductSummaryBinding
import com.mikirinkode.pikul.databinding.ItemTransactionBinding

class TransactionAdapter: RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    private val list = ArrayList<PikulTransaction>()

    inner class ViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {
            fun bind(transaction: PikulTransaction){
                binding.apply {
                    tvTransactionPaymentStatus.text = transaction.transactionStatus
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    fun setData(newList: List<PikulTransaction>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}