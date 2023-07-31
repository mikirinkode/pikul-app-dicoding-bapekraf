package com.mikirinkode.pikul.feature.order

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ServerValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mikirinkode.pikul.constants.Constants
import com.mikirinkode.pikul.constants.PAYMENT_STATUS
import com.mikirinkode.pikul.constants.TRANSACTION_STATUS
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.model.*
import com.mikirinkode.pikul.data.network.ApiClient
import com.mikirinkode.pikul.utils.DateHelper
import com.mikirinkode.pikul.utils.FireStoreUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val preferences: LocalPreference
) : ViewModel() {

    fun getData(): MutableLiveData<PikulResult<Any>> {
        val result = MutableLiveData<PikulResult<Any>>()
        return result
    }

    private fun createPaymentUrl(paymentId: String, totalBilling: Float) {

        val data = SnapTransactionRequest(
            TransactionDetails(paymentId, totalBilling.toLong()),
            CreditCard(true)
        )
        CoroutineScope(Dispatchers.IO).launch {
            val response = ApiClient.getApiService(Constants.MIDTRANS_MAKE_TRANSACTION_URL)
                .makeTransaction(
                    "Basic U0ItTWlkLXNlcnZlci04RjdJV2NtQ1Y5YnVHRnpaWTd6Y3B5cGo=",
                    data
                ) // TODO: CHANGE

            Log.e(TAG, "is succesful ${response.isSuccessful}")
            Log.e(TAG, "data ${response.body()?.redirectUrl}")
            Log.e(TAG, "data ${response.body()?.token}")
            Log.e(TAG, "data ${response.body()?.errorMessages}")

        }

    }

    fun createTransaction(
        transactionTotalItem: Int,
        transactionTotalBilling: Float,
        pickupAddress: String,
        pickupCoordinates: String,
        businessId: String,
        businessName: String,
        merchantId: String,
        merchantName: String,
        customerName: String,
        listOfProduct: List<Product>
    ): LiveData<PikulResult<PikulTransaction>> {
        val result = MutableLiveData<PikulResult<PikulTransaction>>()
        val userId = auth.currentUser?.uid

        if (userId != null) {
            result.postValue(PikulResult.Loading)
            val productIdWithName = mutableMapOf<String, String>()
            val productIdWithPrice = mutableMapOf<String, Float>()
            val productIdWithAmount = mutableMapOf<String, Int>()

            for (product in listOfProduct){
                if (product.productId != null && product.productName != null){
                    productIdWithName[product.productId!!] = product.productName!!
                    productIdWithPrice[product.productId!!] = product.productPrice!!
                    productIdWithAmount[product.productId!!] = product.totalAmount

                    val stock: Int = product.productStocks?.get(merchantId) ?: 0
                    val finalAmount =  stock - product.totalAmount

                    decreaseStockAmount(product.productId!!, merchantId, finalAmount)
                }
            }

            val ref = fireStore.collection(FireStoreUtils.TABLE_TRANSACTIONS).document()

            result.postValue(PikulResult.Loading)
            CoroutineScope(Dispatchers.IO).launch {
                val data = SnapTransactionRequest(
                    TransactionDetails(ref.id, transactionTotalBilling.toLong()),
                    CreditCard(true)
                )
                CoroutineScope(Dispatchers.IO).launch {
                    val response = ApiClient.getApiService(Constants.MIDTRANS_MAKE_TRANSACTION_URL)
                        .makeTransaction(
                            Constants.MIDTRANS_AUTH,
                            data
                        ) // TODO: CHANGE

                    if (response.isSuccessful) {
                        val paymentUrl = response.body()?.redirectUrl


                        val pikulTransaction = PikulTransaction(
                            transactionId = ref.id,
                            alreadyPaid = false,
                            paymentStatus = PAYMENT_STATUS.WAITING_FOR_PAYMENT.toString(),
                            paymentUrl = paymentUrl,
                            paidAt = null,
                            transactionStatus = TRANSACTION_STATUS.WAITING_FOR_PAYMENT.toString(),
                            totalItem = transactionTotalItem,
                            totalBilling = transactionTotalBilling,
                            pickupAddress = pickupAddress,
                            pickupCoordinates = pickupCoordinates,

                            productNames = productIdWithName,
                            productPrices = productIdWithPrice,
                            productAmounts = productIdWithAmount,

                            customerId = userId,
                            customerName = customerName,

                            businessId = businessId,
                            businessName = businessName,

                            merchantId = merchantId,
                            merchantName = merchantName,

                            createdTimestamp = System.currentTimeMillis(),
                            createdAt = DateHelper.getCurrentDateTime(),
                            updatedAtTimestamp = System.currentTimeMillis(),
                            updatedAt = DateHelper.getCurrentDateTime(),
                        )

                        ref.set(pikulTransaction)
                            .addOnFailureListener {
                                val errorMessage: String = it.message ?: "Gagal membuat transaksi"
                                result.postValue(PikulResult.Error(errorMessage))
                            }
                            .addOnSuccessListener {
                                // decrease stock amount()

                                if (paymentUrl != null) {
                                    result.postValue(PikulResult.Success(pikulTransaction))
                                } else {

                                    result.postValue(PikulResult.Error("Gagal membuat transaksi"))
                                }
                            }
                    }
                }
            }
        }
        return result
    }

    private fun decreaseStockAmount(productId: String, merchantId: String, amount: Int){
        fireStore.collection(FireStoreUtils.TABLE_PRODUCTS).document(productId).update("productStocks.$merchantId", amount)
    }

    companion object {
        private const val TAG = "OrderViewModel"
    }
}