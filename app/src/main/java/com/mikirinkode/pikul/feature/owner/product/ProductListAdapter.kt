package com.mikirinkode.pikul.feature.owner.product

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.model.Product
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.databinding.ItemProductBinding
import com.mikirinkode.pikul.databinding.ItemUserBinding
import com.mikirinkode.pikul.feature.chat.room.ChatRoomActivity


class ProductListAdapter : RecyclerView.Adapter<ProductListAdapter.ViewHolder>() {

    private val list: ArrayList<Product> = ArrayList()

    inner class ViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {
            fun bind(product: Product){
                binding.apply {
                    tvProductName.text = product.productName
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    fun setData(newList: List<Product>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

}