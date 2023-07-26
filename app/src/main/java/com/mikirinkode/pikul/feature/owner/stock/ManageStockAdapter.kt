package com.mikirinkode.pikul.feature.owner.stock

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.model.Product
import com.mikirinkode.pikul.databinding.ItemProductBinding
import com.mikirinkode.pikul.databinding.ItemProductStockBinding
import com.mikirinkode.pikul.feature.owner.product.ProductListAdapter
import com.mikirinkode.pikul.utils.MoneyHelper

class ManageStockAdapter (private val clickListener: ClickListener, private val userId: String) : RecyclerView.Adapter<ManageStockAdapter.ViewHolder>() {

    private val list: ArrayList<Product> = ArrayList()
    inner class ViewHolder(private val binding: ItemProductStockBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product){
            binding.apply {
                tvProductName.text = product.productName
                tvProductCategory.text = product.productCategory
                tvProductPrice.text = MoneyHelper.getFormattedPrice(product.productPrice ?: 0f)
                val stock = product.productStocks?.get(userId) ?: 0
                tvStock.text = "Stok: ${stock}"

                Glide.with(itemView.context)
                    .load(product.productThumbnailUrl)
                    .placeholder(R.drawable.progress_animation)
                    .into(ivProductPhoto)

                btnEditStock.setOnClickListener {
                    clickListener.onEditClick(product)
                }
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductStockBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ManageStockAdapter.ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    fun setData(newList: List<Product>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    interface ClickListener {
        fun onEditClick(product: Product)
    }
}
