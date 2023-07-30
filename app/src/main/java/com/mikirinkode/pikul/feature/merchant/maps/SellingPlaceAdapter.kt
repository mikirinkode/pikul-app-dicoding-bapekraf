package com.mikirinkode.pikul.feature.merchant.maps

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
import com.mikirinkode.pikul.data.model.maps.SellingPlace
import com.mikirinkode.pikul.databinding.ItemSellingPlaceBinding
import com.mikirinkode.pikul.feature.detail.DetailBusinessActivity
import com.mikirinkode.pikul.utils.MapsHelper

class SellingPlaceAdapter(private val pref: LocalPreference) :
    RecyclerView.Adapter<SellingPlaceAdapter.ViewHolder>() {
    private val list = ArrayList<SellingPlace>()

    inner class ViewHolder(private val binding: ItemSellingPlaceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(place: SellingPlace) {
            binding.apply {
                // set distance
                val placeLatLng = MapsHelper.getLatLngFromString(place.coordinate)
                val userLat = pref.getString(LocalPreferenceConstants.USER_LAST_LATITUDE)
                val userLong = pref.getString(LocalPreferenceConstants.USER_LAST_LONGITUDE)
                if (placeLatLng != null && !userLat.isNullOrBlank() && !userLong.isNullOrBlank()) {
                    val userLatLng = LatLng(userLat.toDouble(), userLong.toDouble())
                    val distance = MapsHelper.getPlaceDistance(placeLatLng, userLatLng)
                    val formattedDistance = String.format("%.1f", distance)
                    tvMerchantDistance.text = "$formattedDistance KM"
                }
                tvBusinessName.text = place.businessName
                tvTime.text = "${place.startTime} - ${place.endTime}"
                tvPlaceNote.text = "Catatan penjual: ${place.placeNoteForCustomer}"
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
                if (isLoggedIn == true) {
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

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SellingPlaceAdapter.ViewHolder {
        val binding =
            ItemSellingPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SellingPlaceAdapter.ViewHolder, position: Int) {
        Log.e("SellingPlaceAdapter", "data: ${list[position].placeNoteForCustomer}, position: ${position}")
        holder.bind(list[position])
    }

    // todo error
    // java.lang.NullPointerException: Attempt to invoke virtual method 'int java.util.ArrayList.size()' on a null object reference
    override fun getItemCount(): Int = list.size

    fun getDataByPosition(position: Int): SellingPlace {
        return list[position]
    }

    fun getDataPositionById(id: String): Int {
        for ((index, place) in list.withIndex()) {
            Log.e("SellingPlaceAdapter", "data: ${place.placeNoteForCustomer}, position: ${index}")
            if (place.placeId == id) {
                return index
            }
        }
        return RecyclerView.NO_POSITION
    }

    fun setData(newList: List<SellingPlace>) {
        this.list.clear()
        this.list.addAll(newList)
        notifyDataSetChanged()
    }
}