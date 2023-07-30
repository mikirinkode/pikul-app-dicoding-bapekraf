package com.mikirinkode.pikul.feature.customer.home

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.LatLng
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.local.LocalPreferenceConstants
import com.mikirinkode.pikul.data.model.Business
import com.mikirinkode.pikul.data.model.maps.SellingPlace
import com.mikirinkode.pikul.databinding.ItemPopularBusinessBinding
import com.mikirinkode.pikul.feature.detail.DetailBusinessActivity
import com.mikirinkode.pikul.utils.MapsHelper

class PopularBusinessAdapter(private val pref: LocalPreference) : RecyclerView.Adapter<PopularBusinessAdapter.ViewHolder>() {
    private val list = ArrayList<SellingPlace>()

    inner class ViewHolder(private val binding: ItemPopularBusinessBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(place: SellingPlace) {
            binding.apply {

                tvBusinessName.text = place.businessName
                tvBrandRating.text = place.businessRating.toString()

                tvMerchantName.text = place.merchantName
                Log.e("SellingPlaceAdapter", "businessPhoto: ${place.businessPhotoUrl}")

                Glide.with(itemView.context)
                    .load(place.businessPhotoUrl)
                    .placeholder(R.drawable.progress_animation)
                    .into(ivBusinessAvatar)

                if (place.merchantPhotoUrl != null && place.merchantPhotoUrl != ""){
                    Glide.with(itemView.context)
                        .load(place.merchantPhotoUrl)
                        .placeholder(R.drawable.progress_animation)
                        .into(ivMerchantAvatar)
                } else {
                    Glide.with(itemView.context)
                        .load(R.drawable.ic_default_user_avatar)
                        .into(ivMerchantAvatar)
                }
            }

            val isLoggedIn = pref.getBoolean(LocalPreferenceConstants.IS_LOGGED_IN)

            itemView.setOnClickListener {
                if (isLoggedIn == true){
                    itemView.context.startActivity(
                        Intent(itemView.context, DetailBusinessActivity::class.java)
                            .putExtra(DetailBusinessActivity.EXTRA_INTENT_BUSINESS_ID, place.businessId)
                            .putExtra(DetailBusinessActivity.EXTRA_INTENT_MERCHANT_ID, place.merchantId)
                    )
                } else {
                    Toast.makeText(itemView.context, "Harap login terlebih dahulu", Toast.LENGTH_SHORT).show()
                }
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

    fun setData(newList: List<SellingPlace>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

}