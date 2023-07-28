package com.mikirinkode.pikul.feature.owner.stock

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.Product
import com.mikirinkode.pikul.feature.owner.product.ProductViewModel
import com.mikirinkode.pikul.utils.Event
import com.mikirinkode.pikul.utils.FireStoreUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ManageStockViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val preferences: LocalPreference
) : ViewModel() {

    fun updateProductStock(productId: String, stock: Int): LiveData<PikulResult<Boolean>> {
        Log.e(TAG, "updateProductStock")
        val result = MutableLiveData<PikulResult<Boolean>>()

        val userId = auth.currentUser?.uid
        Log.e(TAG, "userId: $userId")
        Log.e(TAG, "productId: $productId")
        Log.e(TAG, "sotck: $stock")
        if (userId != null) {
            val data = mapOf<String, Any>(
                "productStocks.$userId" to stock
            )

            result.postValue(PikulResult.Loading)
            fireStore.collection(FireStoreUtils.TABLE_PRODUCTS).document(productId).update(data)
                .addOnSuccessListener {
                    result.postValue(PikulResult.Success(true))
                }
                .addOnFailureListener {
                    val errorMessage: String = it.message ?: "Gagal mengubah stok"
                    result.postValue(PikulResult.Error(errorMessage))
                }
        }

        return result
    }

    // TODO: CHANGE FOR THE MERCHANT
    fun getProductList(businessId: String): LiveData<PikulResult<List<Product>>> {
        val result = MutableLiveData<PikulResult<List<Product>>>()

        result.postValue(PikulResult.Loading)

        fireStore.collection(FireStoreUtils.TABLE_PRODUCTS).whereEqualTo("businessId", businessId)
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

        return result
    }


    companion object {
        private const val TAG = "ManageStockVM"
    }
}