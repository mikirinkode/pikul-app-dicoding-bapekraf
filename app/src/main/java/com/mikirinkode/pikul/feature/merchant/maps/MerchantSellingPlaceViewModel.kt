package com.mikirinkode.pikul.feature.merchant.maps

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.model.Business
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.data.model.maps.SellingPlace
import com.mikirinkode.pikul.feature.owner.dashboard.OwnerDashboardViewModel
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

    private val sellingPlaces = MutableLiveData<ArrayList<SellingPlace>>()

    fun getUserPlaceCoordinates(): LiveData<PikulResult<List<String>>> {
        val result = MutableLiveData<PikulResult<List<String>>>()
        result.postValue(PikulResult.Loading)

        fireStore.collection(FireStoreUtils.TABLE_USER).get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty){
                    val  list = ArrayList<String>()
                    for (doc in snapshot){
                        val user = doc.toObject(UserAccount::class.java)
                        user.coordinates?.let { list.add(it) }
                    }
                    result.postValue(PikulResult.Success(list))
                }
            }
        return result
    }

    fun getSellingPlaceById(placeId: String): LiveData<SellingPlace> {
        val result = MutableLiveData<SellingPlace>()

        sellingPlaces.value?.forEach { sellingPlace ->
            if (sellingPlace.placeId == placeId) {
                result.postValue(sellingPlace)
                return@forEach
            }
        }
        return result
    }

    fun getOwnerSellingPlaces(): LiveData<PikulResult<List<SellingPlace>>> {
        val result = MutableLiveData<PikulResult<List<SellingPlace>>>()
        val userId = auth.currentUser?.uid

        if (userId != null) {
            result.postValue(PikulResult.Loading)

            fireStore.collection(FireStoreUtils.TABLE_SELLING_PLACES)
                .whereEqualTo("merchantId", userId)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e(TAG, "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        Log.e(TAG, "Current data size: ${snapshot.size()}")
                        val list = ArrayList<SellingPlace>()
                        for (doc in snapshot) {
                            if (doc != null) {
                                val sellingPlace: SellingPlace = doc.toObject()
                                list.add(sellingPlace)
                            }
                        }
                        sellingPlaces.postValue(list)
                        result.postValue(PikulResult.Success(list))
                    } else {
                        result.postValue(PikulResult.Success(emptyList()))
                        Log.e(TAG, "Current data: null")
                    }
                }
//                .get()
//                .addOnSuccessListener { documents ->
//                    val list = ArrayList<SellingPlace>()
//                    for (doc in documents) {
//                        if (doc != null) {
//                            val sellingPlace: SellingPlace = doc.toObject()
//                            list.add(sellingPlace)
//                        }
//                    }
//                    sellingPlaces.postValue(list)
//                    result.postValue(PikulResult.Success(list))
//                }
//                .addOnFailureListener {
//                    val errorMessage: String = it.message ?: "Terjadi Kesalahan pada Maps"
//                    result.postValue(PikulResult.Error(errorMessage))
//                }
        }
        return result
    }

    fun deleteSellingPlaceById(placeId: String): MediatorLiveData<PikulResult<Boolean>> {
        val result = MediatorLiveData<PikulResult<Boolean>>()
        result.postValue(PikulResult.Loading)
        Log.e(TAG, "deleteSellingPlaceById")
        Log.e(TAG, "placeId: $placeId")

        fireStore.collection(FireStoreUtils.TABLE_SELLING_PLACES).document(placeId).delete()
            .addOnSuccessListener {
                result.postValue(PikulResult.Success(true))
            }
            .addOnFailureListener {
                val errorMessage: String = it.message ?: "Gagal Menghapus Lokasi"
                result.postValue(PikulResult.Error(errorMessage))
            }

        return result
    }

    fun addStopPoint(
        name: String,
        province: String,
        startTime: String,
        endTime: String,
        coordinate: String
    ): LiveData<PikulResult<Boolean>> {
        val userId = auth.currentUser?.uid
        val result = MediatorLiveData<PikulResult<Boolean>>()
        result.postValue(PikulResult.Loading)

        if (userId != null) {
            val ref = fireStore.collection(FireStoreUtils.TABLE_SELLING_PLACES).document()

            val data = SellingPlace(
                merchantId = userId,
                placeId = ref.id,
                placeName = name,
                province = province,
                visibility = true,
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