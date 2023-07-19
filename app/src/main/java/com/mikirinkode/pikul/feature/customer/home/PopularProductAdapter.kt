package com.mikirinkode.pikul.feature.customer.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mikirinkode.pikul.data.model.Brand
import com.mikirinkode.pikul.databinding.ItemPopularProductBinding

class PopularProductAdapter : RecyclerView.Adapter<PopularProductAdapter.ViewHolder>() {
    private val list = ArrayList<Brand>()

    inner class ViewHolder(private val binding: ItemPopularProductBinding) :
        RecyclerView.ViewHolder(binding.root) {
            fun bind(item: Brand){
                binding.apply {
                    tvBrandName.text = item.name

                    Glide.with(itemView.context)
                        .load(item.photoUrl)
                        .into(ivBrandPicture)
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPopularProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    fun setData(newList: List<Brand>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

}