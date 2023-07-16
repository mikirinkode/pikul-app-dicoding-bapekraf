package com.mikirinkode.pikul.feature.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mikirinkode.pikul.data.model.Category
import com.mikirinkode.pikul.databinding.ItemCategoryBinding

class CategoryAdapter : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {
    private val list = ArrayList<Category>()

    inner class ViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
            fun bind(item: Category){
                binding.apply {
                    tvCategoryName.text = item.name
                    Glide.with(itemView.context)
                        .load(item.photoUrl)
                        .into(ivCategoryImage)
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    fun setData(newList: List<Category>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

}