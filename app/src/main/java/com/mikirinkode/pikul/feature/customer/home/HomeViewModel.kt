package com.mikirinkode.pikul.feature.customer.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.model.*
import com.mikirinkode.pikul.utils.Event
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

    fun getPopularItemList(): LiveData<List<Brand>> {
        val popularList = MutableLiveData<List<Brand>>()
        Log.e("HomeViewModel", "on getPopularItemList called")
        val list = DummyData.getDummyBrand()
        Log.e("HomeViewModel", "data: ${list.size}")
        popularList.postValue(list)
        return popularList
    }

    fun getPopularBusinessList(): MutableLiveData<PikulResult<List<Business>>> {
        val result = MutableLiveData<PikulResult<List<Business>>>()

        fireStore.collection(FireStoreUtils.TABLE_BUSINESSES).get()
            .addOnFailureListener {
                val errorMessage = it.message ?: "Gagal mengambil Data"
                result.postValue(PikulResult.Error(errorMessage))
            }
            .addOnSuccessListener {snapshots ->
                val list = ArrayList<Business>()
                for (doc in snapshots){
                    val business = doc.toObject(Business::class.java)
                    list.add(business)
                }
                result.postValue(PikulResult.Success(list))
            }
        return result
    }

    fun getNearbyMerchantList(): LiveData<List<Merchant>> {
        val list = MutableLiveData<List<Merchant>>()
        val merchantList = DummyData.getDummyMerchant()
        list.postValue(merchantList)
        return list

    }

    companion object {
        private const val TAG = "HomeViewModel"
    }
}