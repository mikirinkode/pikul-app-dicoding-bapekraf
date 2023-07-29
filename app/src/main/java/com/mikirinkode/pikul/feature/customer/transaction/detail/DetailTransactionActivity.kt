package com.mikirinkode.pikul.feature.customer.transaction.detail

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.constants.PAYMENT_STATUS
import com.mikirinkode.pikul.constants.TRANSACTION_STATUS
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.PikulTransaction
import com.mikirinkode.pikul.data.model.Product
import com.mikirinkode.pikul.databinding.ActivityDetailTransactionBinding
import com.mikirinkode.pikul.feature.detail.DetailBusinessActivity
import com.mikirinkode.pikul.feature.payment.MidtransWebViewActivity
import com.mikirinkode.pikul.utils.CommonHelper
import com.mikirinkode.pikul.utils.MoneyHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailTransactionActivity : AppCompatActivity() {

    private val binding: ActivityDetailTransactionBinding by lazy {
        ActivityDetailTransactionBinding.inflate(layoutInflater)
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
            rvProducts.layoutManager = LinearLayoutManager(this@DetailTransactionActivity)
            rvProducts.adapter = adapter
        }
    }

    private fun handleIntent() {
        val extras = intent.extras
        val transactionId = extras?.getString(EXTRA_INTENT_TRANSACTION_ID)

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

            tvMerchantName.text = data.merchantName
            Glide.with(this@DetailTransactionActivity)
                .load(R.drawable.ic_default_user_avatar)
                .placeholder(R.drawable.progress_animation)
                .into(ivMerchantAvatar) // TODO
            tvMerchantAddress.text = data.pickupAddress
            tvTransactionStatus.text =
                CommonHelper.getReadAblePaymentStatus(data.paymentStatus ?: "")

            when (data.paymentStatus) {
                PAYMENT_STATUS.WAITING_FOR_PAYMENT.toString() -> {
                    btnExtra.setText("Bayar Sekarang")
                    layoutBtnExtra.visibility = View.VISIBLE
                    btnExtra.setOnClickListener {
                        startActivity(
                            Intent(
                                this@DetailTransactionActivity,
                                MidtransWebViewActivity::class.java
                            )
                                .putExtra(
                                    MidtransWebViewActivity.EXTRA_INTENT_TRANSACTION_ID,
                                    data.transactionId
                                )
                                .putExtra(
                                    MidtransWebViewActivity.EXTRA_INTENT_PAYMENT_URL,
                                    data.paymentUrl
                                )
                                .putExtra(
                                    MidtransWebViewActivity.EXTRA_INTENT_FROM,
                                    MidtransWebViewActivity.FROM_DETAIL_TRANSACTION
                                )
                        )
                    }
                }
                PAYMENT_STATUS.ALREADY_PAID.toString() -> {
                    layoutBtnExtra.visibility = View.GONE
                    tvTransactionStatus.text =
                        CommonHelper.getReadAbleTransactionStatus(data.transactionStatus ?: "")

                    when (data.transactionStatus ?: "") {
//                        TRANSACTION_STATUS.WAITING_FOR_PAYMENT.toString() -> {}
                        TRANSACTION_STATUS.WAITING_FOR_MERCHANT.toString() -> {}
                        TRANSACTION_STATUS.ON_PROCESS_BY_MERCHANT.toString() -> {}
                        TRANSACTION_STATUS.READY_TO_PICK_UP.toString() -> {
                            btnExtra.setText("Pesanan Telah Diambil")
                            layoutBtnExtra.visibility = View.VISIBLE
                            btnExtra.setOnClickListener {
                                viewModel.updateTransactionAlreadyPickedUp(data.transactionId ?: "")
                                    .observe(this@DetailTransactionActivity) { result ->
                                        when (result) {
                                            is PikulResult.Loading -> {
                                                binding.layoutLoading.visibility = View.VISIBLE
                                            }
                                            is PikulResult.LoadingWithProgress -> {}
                                            is PikulResult.Error -> {
                                                binding.layoutLoading.visibility = View.GONE
                                                Toast.makeText(
                                                    this@DetailTransactionActivity,
                                                    result.errorMessage,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                            is PikulResult.Success -> {
                                                binding.layoutLoading.visibility = View.GONE
                                                Toast.makeText(
                                                    this@DetailTransactionActivity,
                                                    "Berhasil Memperbarui status pesanan",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                            }
                        }
                        TRANSACTION_STATUS.COMPLETED.toString() -> {
                            btnExtra.setText("Pesan Lagi")
                            layoutBtnExtra.visibility = View.VISIBLE
                            btnExtra.setOnClickListener {
                                startActivity(
                                    Intent(
                                        this@DetailTransactionActivity,
                                        DetailBusinessActivity::class.java
                                    )
                                        .putExtra(
                                            DetailBusinessActivity.EXTRA_INTENT_BUSINESS_ID,
                                            data.businessId
                                        )
                                        .putExtra(
                                            DetailBusinessActivity.EXTRA_INTENT_MERCHANT_ID,
                                            data.merchantId
                                        )
                                )
                            }
                        }
                        TRANSACTION_STATUS.CANCELLED.toString() -> {}
                    }
                }
                PAYMENT_STATUS.FAILED.toString() -> {
                    btnExtra.setText("Pesan Ulang")
                    layoutBtnExtra.visibility = View.VISIBLE
                    btnExtra.setOnClickListener {
                        startActivity(
                            Intent(
                                this@DetailTransactionActivity,
                                DetailBusinessActivity::class.java
                            )
                                .putExtra(
                                    DetailBusinessActivity.EXTRA_INTENT_BUSINESS_ID,
                                    data.businessId
                                )
                                .putExtra(
                                    DetailBusinessActivity.EXTRA_INTENT_MERCHANT_ID,
                                    data.merchantId
                                )
                        )
                    }
                }
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