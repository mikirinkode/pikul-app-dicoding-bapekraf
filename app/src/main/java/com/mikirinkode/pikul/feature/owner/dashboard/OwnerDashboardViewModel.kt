package com.mikirinkode.pikul.feature.owner.dashboard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.model.Business
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.Product
import com.mikirinkode.pikul.data.model.maps.SellingPlace
import com.mikirinkode.pikul.feature.merchant.maps.MerchantSellingPlaceViewModel
import com.mikirinkode.pikul.feature.owner.product.ProductViewModel
import com.mikirinkode.pikul.utils.FireStoreUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OwnerDashboardViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val preferences: LocalPreference
) : ViewModel() {

    fun getProductList(): LiveData<PikulResult<Boolean>> {
        val result = MutableLiveData<PikulResult<Boolean>>()

        result.postValue(PikulResult.Loading)

        val userId = auth.currentUser?.uid
        if (userId != null) {
            fireStore.collection(FireStoreUtils.TABLE_PRODUCTS).whereEqualTo("ownerId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    val list = ArrayList<Product>()
                    for (doc in documents) {
                        val product = doc.toObject(Product::class.java)

                        val stock = product.productStocks?.get(userId)
                        if (stock != null) {
                            if (stock <= 0) {
                                result.postValue(PikulResult.Success(true))
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    val errorMessage: String =
                        it.message ?: "Terjadi kesalahan saat mengambil data produk"
                    result.postValue(PikulResult.Error(errorMessage))
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
                .get()
                .addOnSuccessListener { documents ->
                    val list = ArrayList<SellingPlace>()
                    for (doc in documents) {
                        if (doc != null) {
                            val sellingPlace: SellingPlace = doc.toObject()
                            list.add(sellingPlace)
                        }
                    }
                    result.postValue(PikulResult.Success(list))
                }
                .addOnFailureListener {
                    val errorMessage: String = it.message ?: "Terjadi Kesalahan pada Maps"
                    result.postValue(PikulResult.Error(errorMessage))
                }
        }
        return result
    }

    // TODO: ADD LISTENER DETACH
    fun getBusinessData(): LiveData<PikulResult<Business>> {
        Log.e(TAG, "getBusinessData")
        val result = MutableLiveData<PikulResult<Business>>()
        result.postValue(PikulResult.Loading)

        val userId = auth.currentUser?.uid
        Log.e(TAG, "userId: ${userId}")

        if (userId != null) {
            fireStore.collection(FireStoreUtils.TABLE_BUSINESSES).document(userId)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e(TAG, "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        Log.e(TAG, "Current data: ${snapshot.data}")
                        val businessData: Business? = snapshot.toObject(Business::class.java)
                        Log.e(TAG, "businessId: ${businessData?.businessId}")

                        if (businessData != null) {
                            result.postValue(PikulResult.Success(businessData))
                        }
                    } else {
                        Log.e(TAG, "Current data: null")
                    }

                }
//            fireStore.collection(FireStoreUtils.TABLE_BUSINESSES).document(userId).get()
//                .addOnSuccessListener {
//                    val businessData: Business? = it.toObject(Business::class.java)
//                    Log.e(TAG, "businessId: ${businessData?.businessId}")
//
//                    if (businessData != null) {
//                        result.postValue(PikulResult.Success(businessData))
//                    } else {
//                        val errorMessage: String = "Gagal mengambil data bisnis"
//                        result.postValue(PikulResult.Error(errorMessage))
//                    }
//                }
//                .addOnFailureListener {
//                    val errorMessage: String = it.message ?: "Gagal mengambil data bisnis"
//                    result.postValue(PikulResult.Error(errorMessage))
//                }
        }

        return result
    }

    fun updateSellingModeStatus(status: Boolean): MutableLiveData<PikulResult<Boolean>> {
        val result = MutableLiveData<PikulResult<Boolean>>()
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val update = mapOf<String, Any>(
                "sellingMode" to status
            )
            result.postValue(PikulResult.Loading)
            fireStore.collection(FireStoreUtils.TABLE_BUSINESSES).document(userId)
                .set(update, SetOptions.merge())

            fireStore.collection(FireStoreUtils.TABLE_SELLING_PLACES)
                .whereEqualTo("merchantId", userId).get()
                .addOnSuccessListener {
                    val visibility = mapOf<String, Any>(
                        "visibility" to status
                    )
                    for (doc in it) {
                        fireStore.collection(FireStoreUtils.TABLE_SELLING_PLACES).document(doc.id)
                            .set(visibility, SetOptions.merge())
                    }
                    result.postValue(PikulResult.Success(true))
                }
        }
        return result
    }


    companion object {
        private const val TAG = "OwnerDashboardVM"
    }
}