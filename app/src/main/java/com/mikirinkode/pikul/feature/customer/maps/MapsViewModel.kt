package com.mikirinkode.pikul.feature.customer.maps

import android.util.Log
import androidx.lifecycle.LiveData
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
import com.mikirinkode.pikul.utils.FireStoreUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapsViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val preferences: LocalPreference
) : ViewModel() {

    fun getSellingPlaces(): LiveData<PikulResult<List<SellingPlace>>> {
        val result = MutableLiveData<PikulResult<List<SellingPlace>>>()
        result.postValue(PikulResult.Loading)

        // get business data
        fireStore.collection(FireStoreUtils.TABLE_BUSINESSES).get()
            .addOnFailureListener {
                val errorMessage = it.message ?: "Gagal mengambil Data"
                result.postValue(PikulResult.Error(errorMessage))
            }
            .addOnSuccessListener {snapshots ->
                val businesses = ArrayList<Business>()
                for (doc in snapshots){
                    val business = doc.toObject(Business::class.java)
                    businesses.add(business)
                }

                Log.e(TAG, "business size: ${businesses.size}")

                // get merchant data
                fireStore.collection(FireStoreUtils.TABLE_USER)
                    .whereNotEqualTo("role", "CUSTOMER")
                    .get()
                    .addOnFailureListener {
                        val errorMessage = it.message ?: "Gagal mengambil Data"
                        result.postValue(PikulResult.Error(errorMessage))
                    }
                    .addOnSuccessListener {
                        val merchants = ArrayList<UserAccount>()
                        for (doc in it){
                            val user = doc.toObject(UserAccount::class.java)
                            merchants.add(user)
                        Log.e(TAG, "merchant id: ${user.userId}, name: ${user.name}")
                        }
                        Log.e(TAG, "merchants result size: ${merchants.size}")
                        // get the selling places
                        fireStore.collection(FireStoreUtils.TABLE_SELLING_PLACES)
                            .whereEqualTo("visibility", true)
                            .get()
                            .addOnSuccessListener {documents ->
                                val list = ArrayList<SellingPlace>()
                                for (doc in documents){
                                    if (doc != null){
                                        val sellingPlace: SellingPlace = doc.toObject()

                                        if (sellingPlace.businessId != null){
                                            val businessData = businesses.firstOrNull { it.businessId == sellingPlace.businessId }
                                            val merchantData = merchants.firstOrNull { it.userId == sellingPlace.merchantId }

                                            Log.e(TAG, "selling place id: ${sellingPlace.placeId}, merchant id: ${sellingPlace.placeId}, business id: ${sellingPlace.businessId}")

                                            sellingPlace.businessName = businessData?.businessName
                                            sellingPlace.businessPhotoUrl = businessData?.businessPhoto
                                            sellingPlace.merchantName = merchantData?.name
                                            sellingPlace.merchantPhotoUrl = merchantData?.avatarUrl

                                            list.add(sellingPlace)
                                        }
                                    }
                                }
                                result.postValue(PikulResult.Success(list))
                            }
                            .addOnFailureListener {
                                val errorMessage: String = it.message ?: "Terjadi Kesalahan pada Maps"
                                result.postValue(PikulResult.Error(errorMessage))
                            }
                    }
            }
        return result
    }

    companion object {
        private const val TAG = "MapsViewModel"
    }
}