package com.mikirinkode.pikul.feature.customer.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.model.Business
import com.mikirinkode.pikul.databinding.ItemPopularBusinessBinding
import com.mikirinkode.pikul.feature.detail.DetailBusinessActivity

class PopularBusinessAdapter : RecyclerView.Adapter<PopularBusinessAdapter.ViewHolder>() {
    private val list = ArrayList<Business>()

    inner class ViewHolder(private val binding: ItemPopularBusinessBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Business) {
            binding.apply {
                tvBrandName.text = item.businessName
                tvBrandRating.text = item.businessRating.toString()

                Glide.with(itemView.context)
                    .load(item.businessPhoto)
                    .placeholder(R.drawable.progress_animation)
                    .into(ivBrandPicture)
            }
            itemView.setOnClickListener {
                itemView.context.startActivity(
                    Intent(itemView.context, DetailBusinessActivity::class.java)
                        .putExtra(DetailBusinessActivity.EXTRA_INTENT_BUSINESS_ID, item.businessId)
//                        .putExtra(DetailBusinessActivity.EXTRA_INTENT_MERCHANT_ID, ) // TODO
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemPopularBusinessBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    fun setData(newList: List<Business>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

}