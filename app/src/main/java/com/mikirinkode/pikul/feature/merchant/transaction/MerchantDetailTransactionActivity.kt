package com.mikirinkode.pikul.feature.merchant.transaction

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.constants.PAYMENT_STATUS
import com.mikirinkode.pikul.constants.TRANSACTION_STATUS
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.PikulTransaction
import com.mikirinkode.pikul.data.model.Product
import com.mikirinkode.pikul.databinding.ActivityDetailTransactionBinding
import com.mikirinkode.pikul.databinding.ActivityMerchantDetailTransactionBinding
import com.mikirinkode.pikul.feature.customer.transaction.detail.DetailTransactionActivity
import com.mikirinkode.pikul.feature.customer.transaction.detail.DetailTransactionViewModel
import com.mikirinkode.pikul.feature.customer.transaction.detail.ProductOrderedAdapter
import com.mikirinkode.pikul.feature.detail.DetailBusinessActivity
import com.mikirinkode.pikul.feature.detail.DetailPickupPointMapsActivity
import com.mikirinkode.pikul.feature.payment.MidtransWebViewActivity
import com.mikirinkode.pikul.utils.CommonHelper
import com.mikirinkode.pikul.utils.MoneyHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MerchantDetailTransactionActivity : AppCompatActivity() {
    private val binding: ActivityMerchantDetailTransactionBinding by lazy {
        ActivityMerchantDetailTransactionBinding.inflate(layoutInflater)
    }
    private val viewModel: DetailTransactionViewModel by viewModels()

    private val adapter: ProductOrderedAdapter by lazy {
        ProductOrderedAdapter()
    }

    companion object {
        const val EXTRA_INTENT_TRANSACTION_ID = "EXTRA_INTENT_TRANSACTION_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initRecyclerView()
        handleIntent()
        onClickAction()
    }

    private fun initRecyclerView() {
        binding.apply {
            rvProducts.layoutManager = LinearLayoutManager(this@MerchantDetailTransactionActivity)
            rvProducts.adapter = adapter
        }
    }

    private fun handleIntent() {
        val extras = intent.extras
        val transactionId = extras?.getString(DetailTransactionActivity.EXTRA_INTENT_TRANSACTION_ID)

        if (transactionId != null) {
            observeData(transactionId)
        }
    }

    private fun observeData(transactionId: String) {
        viewModel.getTransactionById(transactionId).observe(this) { result ->
            when (result) {
                is PikulResult.Loading -> {} // TODO
                is PikulResult.LoadingWithProgress -> {}
                is PikulResult.Error -> {}
                is PikulResult.Success -> {
                    val productList = ArrayList<Product>()
                    result.data.productNames?.forEach { (key, _) ->
                        val product = Product(
                            productName = result.data.productNames?.get(key),
                            productPrice = result.data.productPrices?.get(key),
                            totalAmount = result.data.productAmounts?.get(key) ?: 0
                        )
                        productList.add(product)
                    }
                    adapter.setData(productList)
                    initView(result.data)
                }
            }
        }
    }

    private fun initView(data: PikulTransaction) {
        binding.apply {
            tvTotalBilling.text = MoneyHelper.getFormattedPrice(data.totalBilling ?: 0f)
            tvTotalItem.text = data.totalItem.toString()

            tvCustomerName.text = data.customerName
            tvMerchantName.text = data.merchantName
            Glide.with(this@MerchantDetailTransactionActivity)
                .load(R.drawable.ic_default_user_avatar)
                .into(ivCustomertAvatar) // TODO
            Glide.with(this@MerchantDetailTransactionActivity)
                .load(R.drawable.ic_default_user_avatar)
                .into(ivMerchantAvatar) // TODO
            tvMerchantAddress.text = data.pickupAddress
            tvTransactionStatus.text =
                CommonHelper.getReadAblePaymentStatus(data.paymentStatus ?: "")

            btnSeeLocationOnMap.setOnClickListener {
                startActivity(
                    Intent(
                        this@MerchantDetailTransactionActivity,
                        DetailPickupPointMapsActivity::class.java
                    )
                        .putExtra(
                            DetailPickupPointMapsActivity.EXTRA_INTENT_COORDINATES,
                            data.pickupCoordinates
                        )
                        .putExtra(
                            DetailPickupPointMapsActivity.EXTRA_INTENT_ADDRESS,
                            data.pickupAddress
                        )
                )
            }

        }
    }

    private fun onClickAction() {
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                onBackPressed()
            }


        }
    }
}