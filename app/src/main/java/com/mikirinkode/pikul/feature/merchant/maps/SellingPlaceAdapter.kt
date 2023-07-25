package com.mikirinkode.pikul.feature.merchant.maps

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.local.LocalPreferenceConstants
import com.mikirinkode.pikul.data.model.maps.SellingPlace
import com.mikirinkode.pikul.databinding.ItemSellingPlaceBinding
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
                tvPlaceName.text = place.placeName
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
        Log.e("SellingPlaceAdapter", "data: ${list[position].placeName}, position: ${position}")
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
            Log.e("SellingPlaceAdapter", "data: ${place.placeName}, position: ${index}")
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