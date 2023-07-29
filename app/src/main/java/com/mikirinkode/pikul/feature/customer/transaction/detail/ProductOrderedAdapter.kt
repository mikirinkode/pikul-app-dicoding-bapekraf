package com.mikirinkode.pikul.feature.customer.transaction.detail

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.model.Product
import com.mikirinkode.pikul.databinding.ItemProductOrderBinding
import com.mikirinkode.pikul.databinding.ItemProductOrderedBinding
import com.mikirinkode.pikul.databinding.ItemProductSummaryBinding
import com.mikirinkode.pikul.utils.MoneyHelper

class ProductOrderedAdapter() :
    RecyclerView.Adapter<ProductOrderedAdapter.ViewHolder>() {

    private val list: ArrayList<Product> = ArrayList()



    inner class ViewHolder(private val binding: ItemProductOrderedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.apply {
                tvItemName.text = product.productName
                tvItemAmountAndPrice.text = "${product.totalAmount} x ${MoneyHelper.getFormattedPrice(product.productPrice ?: 0f)}"
                val total = product.productPrice?.times(product.totalAmount)
                tvItemTotalPrice.text = MoneyHelper.getFormattedPrice(total ?: 0f)

            }
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductOrderedAdapter.ViewHolder {
        val binding =
            ItemProductOrderedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ProductOrderedAdapter.ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    fun setData(newList: List<Product>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}