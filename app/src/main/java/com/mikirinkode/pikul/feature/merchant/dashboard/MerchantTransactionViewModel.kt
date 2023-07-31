package com.mikirinkode.pikul.feature.merchant.dashboard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.mikirinkode.pikul.constants.Constants
import com.mikirinkode.pikul.constants.NOTIFICATION_TYPE
import com.mikirinkode.pikul.constants.TRANSACTION_STATUS
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.PikulTransaction
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.utils.DateHelper
import com.mikirinkode.pikul.utils.FireStoreUtils
import com.onesignal.OneSignal
import dagger.hilt.android.lifecycle.HiltViewModel
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class MerchantTransactionViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val preferences: LocalPreference
) : ViewModel() {


    // TODO: SEND NOTIFICATION
    fun updateTransactionOnProcess(transactionId: String, customerId: String): LiveData<PikulResult<Boolean>> {
        val result = MutableLiveData<PikulResult<Boolean>>()
        result.postValue(PikulResult.Loading)

        val data = mapOf<String, Any>(
            "transactionStatus" to TRANSACTION_STATUS.ON_PROCESS_BY_MERCHANT.toString(),
            "updatedAt" to DateHelper.getCurrentDateTime(),
            "updatedAtTimestamp" to System.currentTimeMillis()
        )
        fireStore.collection(FireStoreUtils.TABLE_TRANSACTIONS).document(transactionId)
            .set(data, SetOptions.merge())
            .addOnFailureListener {
                val errorMessage: String = it.message ?: "Gagal memperbarui status"
                result.postValue(PikulResult.Error(errorMessage))
            }
            .addOnSuccessListener {
                result.postValue(PikulResult.Success(true))

                // post notification
                fireStore.collection(FireStoreUtils.TABLE_USER)
                    .document(customerId).get()
                    .addOnSuccessListener {
                        val user = it.toObject(UserAccount::class.java)
                        if (user != null){
                            if (user.oneSignalToken != null && user.oneSignalToken != ""){
                                val title = "PESANAN SEDANG DIPROSES"
                                var message = "Ketuk untuk lihat detail pesanan"
                                val receiverDeviceTokenList = listOf<String>(user.oneSignalToken)
                                postNotification(transactionId ?: "", title, message, receiverDeviceTokenList)
                            }
                        }
                    }
            }
        return result
    }

    private fun postNotification(
        transactionId: String,
        senderName: String,
        message: String,
        receiverDeviceTokenList: List<String>
    ) {
        Log.e("ChatRoomVM", "postNotification called")
        Log.e("ChatRoomVM", "receiver device token list: " + receiverDeviceTokenList)
//        Log.e("ChatRoomVM", "receiver device token: ${receiverDeviceTokenList?.get(0)}")
        val receivers = JSONArray(receiverDeviceTokenList)
        val customData = JSONObject().apply {
            put("transactionId", transactionId)
            put("notificationType", NOTIFICATION_TYPE.BOOKING_DETAIL_CUSTOMER.toString())
        }
        val notificationJson = JSONObject().apply {
            put("app_id", Constants.ONE_SIGNAL_APP_ID)
            put("include_player_ids", receivers)
            put("contents", JSONObject().put("en", message))
            put("headings", JSONObject().put("en", senderName))
            put("data", customData)
        }

        OneSignal.postNotification(
            notificationJson,
            object : OneSignal.PostNotificationResponseHandler {
                override fun onSuccess(response: JSONObject?) {
                    // Notification sent successfully
                }

                override fun onFailure(response: JSONObject?) {
                    // Failed to send notification
                }
            })
    }

    // TODO: SEND NOTIFICATION
    fun updateTransactionReadyToPickup(transactionId: String, customerId: String): LiveData<PikulResult<Boolean>> {
        val result = MutableLiveData<PikulResult<Boolean>>()
        result.postValue(PikulResult.Loading)

        val data = mapOf<String, Any>(
            "transactionStatus" to TRANSACTION_STATUS.READY_TO_PICK_UP.toString(),
            "updatedAt" to DateHelper.getCurrentDateTime(),
            "updatedAtTimestamp" to System.currentTimeMillis()
        )
        fireStore.collection(FireStoreUtils.TABLE_TRANSACTIONS).document(transactionId)
            .set(data, SetOptions.merge())
            .addOnFailureListener {
                val errorMessage: String = it.message ?: "Gagal memperbarui status"
                result.postValue(PikulResult.Error(errorMessage))
            }
            .addOnSuccessListener {
                result.postValue(PikulResult.Success(true))

                // post notification
                // post notification
                fireStore.collection(FireStoreUtils.TABLE_USER)
                    .document(customerId).get()
                    .addOnSuccessListener {
                        val user = it.toObject(UserAccount::class.java)
                        if (user != null){
                            if (user.oneSignalToken != null && user.oneSignalToken != ""){
                                val title = "PESANAN SIAP DIAMBIL"
                                var message = "Ketuk untuk lihat detail pesanan"
                                val receiverDeviceTokenList = listOf<String>(user.oneSignalToken)
                                postNotification(transactionId ?: "", title, message, receiverDeviceTokenList)
                            }
                        }
                    }
            }
        return result
    }


    fun getTransactionById(transactionId: String): MutableLiveData<PikulResult<PikulTransaction>> {
        val result = MutableLiveData<PikulResult<PikulTransaction>>()

        result.postValue(PikulResult.Loading)
        fireStore.collection(FireStoreUtils.TABLE_TRANSACTIONS).document(transactionId)
            .get()
            .addOnFailureListener { } // TODO
            .addOnSuccessListener {
                val transaction = it.toObject(PikulTransaction::class.java)
                if (transaction != null) {
                    result.postValue(PikulResult.Success(transaction))
                } else {
                    // TODO
                }
            }
        return result
    }
    fun getCompleteTransactionList(): MutableLiveData<PikulResult<List<PikulTransaction>>> {
        val result = MutableLiveData<PikulResult<List<PikulTransaction>>>()

        val userId = auth.currentUser?.uid
        val filteredTransaction = listOf<String>(
            TRANSACTION_STATUS.COMPLETED.toString(),
            TRANSACTION_STATUS.CANCELLED.toString(),
        )

        if (userId != null) {
            result.postValue(PikulResult.Loading)
            fireStore.collection(FireStoreUtils.TABLE_TRANSACTIONS)
                .whereEqualTo("merchantId", userId)
                .whereIn("transactionStatus", filteredTransaction)
                .addSnapshotListener { snapshot, error ->
                    if (error != null){
                        val errorMessage = error.message ?: "Gagal mengambil data transaksi"
                        result.postValue(PikulResult.Error(errorMessage))
                    } else {
                        if (snapshot != null){
                            val list = ArrayList<PikulTransaction>()

                            for (doc in snapshot){
                                val transaction = doc.toObject(PikulTransaction::class.java)
                                list.add(transaction)
                            }
                            result.postValue(PikulResult.Success(list))
                        }
                    }
                }
        }

        return result
    }


    fun getOnGoingTransactionList(): MutableLiveData<PikulResult<List<PikulTransaction>>> {
        val result = MutableLiveData<PikulResult<List<PikulTransaction>>>()

        val userId = auth.currentUser?.uid
        val filteredTransaction = listOf<String>(
            TRANSACTION_STATUS.WAITING_FOR_PAYMENT.toString(),
            TRANSACTION_STATUS.WAITING_FOR_MERCHANT.toString(),
            TRANSACTION_STATUS.ON_PROCESS_BY_MERCHANT.toString(),
            TRANSACTION_STATUS.READY_TO_PICK_UP.toString(),
        )

        if (userId != null) {
            result.postValue(PikulResult.Loading)
            fireStore.collection(FireStoreUtils.TABLE_TRANSACTIONS)
                .whereEqualTo("merchantId", userId)
                .whereIn("transactionStatus", filteredTransaction)
                .addSnapshotListener { snapshot, error ->
                    if (error != null){
                        val errorMessage = error.message ?: "Gagal mengambil data transaksi"
                        result.postValue(PikulResult.Error(errorMessage))
                    } else {
                        if (snapshot != null){
                            val list = ArrayList<PikulTransaction>()

                            for (doc in snapshot){
                                val transaction = doc.toObject(PikulTransaction::class.java)
                                list.add(transaction)
                            }


                            result.postValue(PikulResult.Success(list))
                        }
                    }
                }

        }

        return result
    }

    companion object {
        private const val TAG = "BaseViewModel"
    }
}