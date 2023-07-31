package com.mikirinkode.pikul.feature.customer.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.model.*
import com.mikirinkode.pikul.data.model.maps.SellingPlace
import com.mikirinkode.pikul.feature.customer.maps.MapsViewModel
import com.mikirinkode.pikul.utils.FireStoreUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val preferences: LocalPreference
) : ViewModel() {


    private val _categoryList = MutableLiveData<List<Category>>()
    private val categoryList: LiveData<List<Category>> = _categoryList

    fun getCategoryList(): LiveData<List<Category>> {

        Log.e("HomeViewModel", "on getCategoryList called")
        val list = Category.getCategoryList()
        Log.e("HomeViewModel", "data: ${list.size}")
        _categoryList.postValue(list)
        return categoryList
    }



    fun getPopularBusinessList(): MutableLiveData<PikulResult<List<SellingPlace>>> {
        val result = MutableLiveData<PikulResult<List<SellingPlace>>>()

        result.postValue(PikulResult.Loading)
        // get merchant data
        fireStore.collection(FireStoreUtils.TABLE_USER)
            .whereNotEqualTo("role", "CUSTOMER")
            .get()
            .addOnFailureListener {
                val errorMessage = it.message ?: "Gagal mengambil Data"
//                result.postValue(PikulResult.Error(errorMessage)) // TODO
            }
            .addOnSuccessListener {merchantSnapshot ->
                val merchants = ArrayList<UserAccount>()
                for (doc in merchantSnapshot){
                    val user = doc.toObject(UserAccount::class.java)
                    merchants.add(user)
                    Log.e(TAG, "merchant id: ${user.userId}, name: ${user.name}")
                }
                // -- Finish get merchant data

                // now get the business data
                fireStore.collection(FireStoreUtils.TABLE_BUSINESSES).get()
                    .addOnFailureListener {
                        val errorMessage = it.message ?: "Gagal mengambil Data"
//                        result.postValue(PikulResult.Error(errorMessage)) // TODO
                    }
                    .addOnSuccessListener {snapshots ->
                        val businesses = ArrayList<Business>()
                        for (doc in snapshots){
                            val business = doc.toObject(Business::class.java)
                            businesses.add(business)
                        }
                    // -- Finish get business data

                        // now get the selling place data
                        // get the selling places
                        fireStore.collection(FireStoreUtils.TABLE_SELLING_PLACES)
                            .whereEqualTo("visibility", true)
                            .get()
                            .addOnSuccessListener {documents ->
                                val list = ArrayList<SellingPlace>()
                                for (doc in documents){
                                    if (doc != null){
                                        val sellingPlace: SellingPlace = doc.toObject()
                                        val isExist = list.firstOrNull { it.businessId == sellingPlace.businessId }

                                        if (sellingPlace.businessId != null && isExist == null){
                                            val businessData = businesses.firstOrNull { it.businessId == sellingPlace.businessId }
                                            val merchantData = merchants.firstOrNull { it.userId == sellingPlace.merchantId }

                                            Log.e(TAG, "selling place id: ${sellingPlace.placeId}, merchant id: ${sellingPlace.placeId}, business id: ${sellingPlace.businessId}")

                                            sellingPlace.businessName = businessData?.businessName
                                            sellingPlace.businessPhotoUrl = businessData?.businessPhoto
                                            sellingPlace.businessRating = businessData?.businessRating
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
        private const val TAG = "HomeViewModel"
    }
}