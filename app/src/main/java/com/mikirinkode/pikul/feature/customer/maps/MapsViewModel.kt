package com.mikirinkode.pikul.feature.customer.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.maps.SellingPlace
import com.mikirinkode.pikul.utils.Event
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

        fireStore.collection(FireStoreUtils.TABLE_SELLING_PLACES).get()
            .addOnSuccessListener {documents ->
                val list = ArrayList<SellingPlace>()
                for (doc in documents){
                    if (doc != null){
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
        return result
    }

    companion object {
        private const val TAG = "MapsViewModel"
    }
}