package com.mikirinkode.pikul.feature.customer.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mikirinkode.pikul.data.model.MerchantAgreement
import com.mikirinkode.pikul.databinding.ItemNearbyMerchantBinding

class NearbyMerchantAdapter : RecyclerView.Adapter<NearbyMerchantAdapter.ViewHolder>() {
    private val list = ArrayList<MerchantAgreement>()

    inner class ViewHolder(private val binding: ItemNearbyMerchantBinding) :
        RecyclerView.ViewHolder(binding.root) {
            fun bind(item: MerchantAgreement){
                binding.apply {
//                    tvBrandName.text = item.name // TODO: Change Brand Name
//                    tvMerchantName.text = item.name
//                    tvMerchantMoveStatus.text = "Status: ${item.movingStatus}"
//
//                    tvMerchantDistance.text = "1 KM"
//
//                    Glide.with(itemView.context)
//                        .load(item.avatarUrl)
//                        .into(ivMerchantProfile)

                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNearbyMerchantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    fun setData(newList: List<MerchantAgreement>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

}