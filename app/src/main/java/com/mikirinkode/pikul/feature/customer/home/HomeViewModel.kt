package com.mikirinkode.pikul.feature.customer.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.model.Brand
import com.mikirinkode.pikul.data.model.Category
import com.mikirinkode.pikul.data.model.DummyData
import com.mikirinkode.pikul.data.model.Merchant
import com.mikirinkode.pikul.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val preferences: LocalPreference
) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isError = MutableLiveData<Boolean>()
    val isError: LiveData<Boolean> = _isError

    private val _responseMessage = MutableLiveData<Event<String>>()
    val responseMessage: LiveData<Event<String>> = _responseMessage

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