package com.mikirinkode.pikul.feature.owner.merchant

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.databinding.ItemMyMerchantBinding

class MyMerchantAdapter() :
    RecyclerView.Adapter<MyMerchantAdapter.ViewHolder>() {

    private val list = ArrayList<UserAccount>()

    inner class ViewHolder(private val binding: ItemMyMerchantBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(merchant: UserAccount) {
            binding.apply {
                tvMerchantName.text = merchant.name
                tvMerchantAddress.text = merchant.province

                if (merchant?.avatarUrl.isNullOrBlank()) {
                    Glide.with(itemView.context)
                        .load(R.drawable.ic_default_user_avatar)
                        .into(ivUserAvatar)
                } else {
                    Glide.with(itemView.context)
                        .load(merchant?.avatarUrl)
                        .placeholder(R.drawable.progress_animation)
                        .into(ivUserAvatar)
                }


            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemMyMerchantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    fun setData(newList: List<UserAccount>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }


}