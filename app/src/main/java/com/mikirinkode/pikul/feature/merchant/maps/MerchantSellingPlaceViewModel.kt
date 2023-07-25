package com.mikirinkode.pikul.feature.merchant.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.maps.SellingPlace
import com.mikirinkode.pikul.utils.DateHelper
import com.mikirinkode.pikul.utils.FireStoreUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MerchantSellingPlaceViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val preferences: LocalPreference
) : ViewModel() {

    fun searchLocation(query: String) {

    }

    fun addStopPoint(name: String, startTime: String, endTime: String, coordinate: String): LiveData<PikulResult<Boolean>> {
        val userId = auth.currentUser?.uid
        val result = MediatorLiveData<PikulResult<Boolean>>()
        result.postValue(PikulResult.Loading)

        if (userId != null) {
            val ref = fireStore.collection(FireStoreUtils.TABLE_SELLING_PLACES).document()

            val data = SellingPlace(
                userId = userId,
                placeId = ref.id,
                placeName = name,
                startTime = startTime,
                endTime = endTime,
                coordinate = coordinate,
                createdAt = DateHelper.getCurrentDateTime(),
                updatedAt = "",
            )

            ref.set(data)
                .addOnSuccessListener {
                    result.postValue(PikulResult.Success(true))
                }
                .addOnFailureListener {
                    result.postValue(PikulResult.Error(it.message.toString()))
                }
        }
        return result
    }

    companion object {
        private const val TAG = "MerchantStopPointVM"
    }
}