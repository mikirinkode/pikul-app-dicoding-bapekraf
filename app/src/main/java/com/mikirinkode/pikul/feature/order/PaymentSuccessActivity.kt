package com.mikirinkode.pikul.feature.order

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.constants.TRANSACTION_STATUS
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.PikulTransaction
import com.mikirinkode.pikul.databinding.ActivityPaymentSuccessBinding
import com.mikirinkode.pikul.feature.customer.main.MainActivity
import com.mikirinkode.pikul.feature.customer.transaction.TransactionViewModel
import com.mikirinkode.pikul.feature.detail.DetailPickupPointMapsActivity
import com.mikirinkode.pikul.utils.CommonHelper
import com.mikirinkode.pikul.utils.MoneyHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentSuccessActivity : AppCompatActivity() {

    private val binding: ActivityPaymentSuccessBinding by lazy {
        ActivityPaymentSuccessBinding.inflate(layoutInflater)
    }

    private val viewModel: TransactionViewModel by viewModels()

    companion object {
        const val EXTRA_INTENT_TRANSACTION_ID = "EXTRA_INTENT_TRANSACTION_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val transactionId = intent.getStringExtra(EXTRA_INTENT_TRANSACTION_ID)
        if (transactionId != null) {
            observeData(transactionId)
        }
        onClickAction()
    }

    private fun observeData(transactionId: String) {
        binding.apply {
            viewModel.getTransactionById(transactionId)
                .observe(this@PaymentSuccessActivity) { result ->
                    when (result) {
                        is PikulResult.Loading -> {} // TODO
                        is PikulResult.LoadingWithProgress -> {}
                        is PikulResult.Error -> {}
                        is PikulResult.Success -> {
                            initView(result.data)
                            // post notification
                            if (result.data.transactionStatus == TRANSACTION_STATUS.WAITING_FOR_PAYMENT.toString()){
                                viewModel.onPaymentSuccessReceived(transactionId)
                                viewModel.sendNotification(
                                    result.data.transactionId,
                                    result.data.merchantId,
                                    result.data.customerName,
                                    result.data.productNames,
                                    result.data.productAmounts
                                )
                            }
                        }
                    }
                }
        }
    }


    private fun initView(data: PikulTransaction) {
        binding.apply {
            tvTotalBilling.text = MoneyHelper.getFormattedPrice(data.totalBilling ?: 0f)
            tvTotalItem.text = data.totalItem.toString()

            tvMerchantName.text = data.merchantName
            Glide.with(this@PaymentSuccessActivity)
                .load(R.drawable.ic_default_user_avatar)
                .placeholder(R.drawable.progress_animation)
                .into(ivMerchantAvatar) // TODO
            tvMerchantAddress.text = data.pickupAddress
            tvTransactionStatus.text =
                CommonHelper.getReadAbleTransactionStatus(TRANSACTION_STATUS.WAITING_FOR_MERCHANT.toString())

            btnSeeLocationOnMap.setOnClickListener {
                startActivity(
                    Intent(this@PaymentSuccessActivity, DetailPickupPointMapsActivity::class.java)
                        .putExtra(
                            DetailPickupPointMapsActivity.EXTRA_INTENT_COORDINATES,
                            data.pickupCoordinates ?: ""
                        )
                        .putExtra(
                            DetailPickupPointMapsActivity.EXTRA_INTENT_ADDRESS,
                            data.pickupAddress ?: ""
                        )
                )
            }
        }
    }

    override fun onBackPressed() {
//        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }

    private fun onClickAction() {
        binding.apply {
            btnBackToHome.setOnClickListener {

                startActivity(Intent(this@PaymentSuccessActivity, MainActivity::class.java))
                finishAffinity()
            }
            topAppBar.setOnClickListener {
                startActivity(Intent(this@PaymentSuccessActivity, MainActivity::class.java))
                finishAffinity()
            }
        }
    }
}