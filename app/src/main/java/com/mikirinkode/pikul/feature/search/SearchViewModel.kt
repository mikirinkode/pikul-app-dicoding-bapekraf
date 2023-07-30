package com.mikirinkode.pikul.feature.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.algolia.search.client.ClientSearch
import com.algolia.search.helper.deserialize
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.algolia.search.model.IndexName
import com.algolia.search.model.search.Query
import com.mikirinkode.pikul.data.model.Business
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val preferences: LocalPreference
) : ViewModel() {

    fun test() {
        viewModelScope.launch {

            CoroutineScope(Dispatchers.IO).launch{
                val client = ClientSearch(
                    applicationID = ApplicationID("EWKHGBBMT7"),
                    apiKey = APIKey("79724b305aa483e93838c4b2fb978150")
                )
                val index = client.initIndex(IndexName("dev_pikul"))

                val response = index.run {
                    search(Query("dagang"))
                }
                val result = response.hits
                Log.e(TAG, "result size: ${result.size}")
            }
        }
    }

    fun search(query: String): LiveData<PikulResult<List<Business>>> {
        val result = MutableLiveData<PikulResult<List<Business>>>()

        result.postValue(PikulResult.Loading)
        viewModelScope.launch {

            CoroutineScope(Dispatchers.IO).launch{
                val client = ClientSearch(
                    applicationID = ApplicationID("EWKHGBBMT7"),
                    apiKey = APIKey("79724b305aa483e93838c4b2fb978150")
                )
                val index = client.initIndex(IndexName("dev_pikul"))

                val response = index.run {
                    search(Query(query))
                }
//                val result = response.hits
//                Log.e(TAG, "result size: ${result.size}")

//                val result = ArrayList<Business>()
//                val test = response.hits.deserialize(Business.serializer())
//                result.addAll(test)
//                val serializedResponse = response.hits.deserialize(Business.serializer())
                val list = ArrayList<Business>()
                for (data in response.hits){
                    val json = data.json
                    val business = Business(
                        businessId = json["businessId"].toString(),
                        businessAddress = json["businessAddress"].toString(),
                        businessName = json["businessName"].toString(),
                        businessPhoto = json["businessPhoto"].toString(),
                        businessProvince = json["businessProvince"].toString(),
                        businessProductCategory = json["businessProductCategory"].toString(),
                    )
                    list.add(business)

                Log.e(TAG, "business id: ${json["businessId"].toString()}, name: ${json["businessName"].toString()}, photourl: ${json["businessPhoto"].toString()}")
                }
                result.postValue(PikulResult.Success(list))
            }
        }
        return result
    }


    fun getData(): MutableLiveData<PikulResult<Any>> {
        val result = MutableLiveData<PikulResult<Any>>()
        return result
    }

    companion object {
        private const val TAG = "SearchViewModel"
    }
}