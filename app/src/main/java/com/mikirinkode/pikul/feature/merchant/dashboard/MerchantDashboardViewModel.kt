package com.mikirinkode.pikul.feature.merchant.dashboard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.model.*
import com.mikirinkode.pikul.data.model.maps.SellingPlace
import com.mikirinkode.pikul.utils.FireStoreUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MerchantDashboardViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val preferences: LocalPreference
) : ViewModel() {

    fun getProductList(businessId: String): LiveData<PikulResult<Boolean>> {
        val result = MutableLiveData<PikulResult<Boolean>>()

        result.postValue(PikulResult.Loading)

        val userId = auth.currentUser?.uid
        Log.e(TAG, "get product list")
        if (userId != null) {
            fireStore.collection(FireStoreUtils.TABLE_PRODUCTS).whereEqualTo("businessId", businessId)
                .get()
                .addOnSuccessListener { documents ->
                    val list = ArrayList<Product>()
                    for (doc in documents) {
                        val product = doc.toObject(Product::class.java)

                        val stock = product.productStocks?.get(userId)
        Log.e(TAG, "get product ${stock}")
                        if (stock != null) {
                            if (stock <= 0) {
                                result.postValue(PikulResult.Success(true))
                            }
                        }else{
                                result.postValue(PikulResult.Success(true))
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

    fun getMerchantData(): LiveData<PikulResult<UserAccount>> {
        val result = MutableLiveData<PikulResult<UserAccount>>()

        val merchantId = auth.currentUser?.uid

        if (merchantId != null) {
            result.postValue(PikulResult.Loading)

            fireStore.collection(FireStoreUtils.TABLE_USER).document(merchantId).get()
                .addOnFailureListener {
                    val errorMessage = it.message ?: "Gagal Mengambil Data"
                    result.postValue(PikulResult.Error(errorMessage))
                }
                .addOnSuccessListener {doc ->
                    val user = doc.toObject(UserAccount::class.java)
                    if (user != null) {
                        result.postValue(PikulResult.Success(user))
                    }
                }
        }
        return result
    }

    fun getAgreement(): MutableLiveData<PikulResult<MerchantAgreement?>> {
        val result = MutableLiveData<PikulResult<MerchantAgreement?>>()
        val userId = auth.currentUser?.uid
        if (userId != null) {
            result.postValue(PikulResult.Loading)
            fireStore.collection(FireStoreUtils.TABLE_MERCHANT_AGREEMENT).whereEqualTo("merchantId", userId).whereEqualTo("active", true).get()
                .addOnFailureListener {} // TODO
                .addOnSuccessListener {documents ->
                    if (documents.isEmpty){
                        result.postValue(PikulResult.Success(null))
                    } else {
                        val agreement = documents.first().toObject(MerchantAgreement::class.java)
                        result.postValue(PikulResult.Success(agreement))
                    }
                }
        }
        return result
    }



    companion object {
        private const val TAG = "MerchantDashboardVM"
    }
}