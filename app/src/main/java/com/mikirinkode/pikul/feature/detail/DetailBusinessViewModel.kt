package com.mikirinkode.pikul.feature.detail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.model.Business
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.Product
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.feature.owner.product.ProductViewModel
import com.mikirinkode.pikul.utils.FireStoreUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DetailBusinessViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val preferences: LocalPreference
) : ViewModel() {

    fun getBusinessData(businessId: String): MutableLiveData<PikulResult<Business>> {
        val result = MutableLiveData<PikulResult<Business>>()
        result.postValue(PikulResult.Loading)
        fireStore.collection(FireStoreUtils.TABLE_BUSINESSES).document(businessId).get()
            .addOnFailureListener {
                val errorMessage = it.message ?: "Gagal Mengambil Data"
                result.postValue(PikulResult.Error(errorMessage))
            }
            .addOnSuccessListener { document ->
                val business = document.toObject(Business::class.java)
                if (business != null) {
                    result.postValue(PikulResult.Success(business))
                }
            }
        return result
    }

    fun getMerchantData(ownerId: String): LiveData<PikulResult<UserAccount>> {
        val result = MutableLiveData<PikulResult<UserAccount>>()

        result.postValue(PikulResult.Loading)

        fireStore.collection(FireStoreUtils.TABLE_USER).document(ownerId).get()
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
        return result
    }

    fun getProductList(ownerId: String): LiveData<PikulResult<List<Product>>> {
        val result = MutableLiveData<PikulResult<List<Product>>>()

        result.postValue(PikulResult.Loading)

        fireStore.collection(FireStoreUtils.TABLE_PRODUCTS).whereEqualTo("ownerId", ownerId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Listen failed.", e)
                    val errorMessage: String = e.message ?: "Gagal mengambil data produk"
                    result.postValue(PikulResult.Error(errorMessage))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = ArrayList<Product>()
                    for (doc in snapshot) {
                        val product = doc.toObject(Product::class.java)
                        list.add(product)
                    }
                    result.postValue(PikulResult.Success(list))
                }
            }
//                .get()
//                .addOnSuccessListener { documents ->
//                    val list = ArrayList<Product>()
//                    for (doc in documents) {
//                        val product = doc.toObject(Product::class.java)
//                        list.add(product)
//                    }
//                    result.postValue(PikulResult.Success(list))
//                }
//                .addOnFailureListener {
//                    val errorMessage: String =
//                        it.message ?: "Terjadi kesalahan saat mengambil data produk"
//                    result.postValue(PikulResult.Error(errorMessage))
//                }


        return result
    }

    companion object {
        private const val TAG = "DetailBusinessVM"
    }
}