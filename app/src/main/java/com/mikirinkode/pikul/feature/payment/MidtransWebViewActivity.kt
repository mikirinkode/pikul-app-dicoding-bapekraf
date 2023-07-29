package com.mikirinkode.pikul.feature.payment

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.viewModels
import com.mikirinkode.pikul.databinding.ActivityMidtransWebViewBinding
import com.mikirinkode.pikul.feature.customer.main.MainActivity
import com.mikirinkode.pikul.feature.order.PaymentSuccessActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MidtransWebViewActivity : AppCompatActivity() {

    private val binding: ActivityMidtransWebViewBinding by lazy {
        ActivityMidtransWebViewBinding.inflate(layoutInflater)
    }

    private val viewModel: MidtransViewModel by viewModels()

    companion object {
        const val EXTRA_INTENT_PAYMENT_URL = "EXTRA_INTENT_PAYMENT_URL"
        const val EXTRA_INTENT_TRANSACTION_ID = "EXTRA_INTENT_TRANSACTION_ID"
        const val EXTRA_INTENT_FROM = "EXTRA_INTENT_FROM"

        const val FROM_CREATE_NEW_TRANSACTION = "FROM_CREATE_NEW_TRANSACTION"
        const val FROM_DETAIL_TRANSACTION = "FROM_DETAIL_TRANSACTION"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val transactionId = intent.getStringExtra(EXTRA_INTENT_TRANSACTION_ID)
        val paymentUrl = intent.getStringExtra(EXTRA_INTENT_PAYMENT_URL)
        if (transactionId != null) {
            if (paymentUrl != null) {
                initView(paymentUrl, transactionId)
            }
        }

        onClickAction()
    }


    private fun initView(paymentUrl: String, transactionId: String) {
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                // TODO
            }
            // Enable JavaScript (optional, only if required by the web content)
            val webSettings: WebSettings = webView.settings
            webSettings.javaScriptEnabled = true

            webView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    view?.loadUrl(request?.url.toString())
                    return true
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    val currentUrl = webView.url

                    Log.e("MidtransFragment", "onPageFinished: ${currentUrl}")

                    if (currentUrl != null) { // TODO: SEBENERNYA BELUM TERBAYAR
                        if (currentUrl.contains("transaction_status=settlement") ||
                            currentUrl.contains("transaction_status=capture")
                        ) {
                            goToSuccessView(transactionId)
                        } else if (currentUrl.contains("gopay-finish-deeplink")){ // TODO: SEBENERNYA BELUM TERBAYAR
                            goToSuccessView(transactionId)
                        } else if (currentUrl.contains("shopeepay-finish-deeplink")){ // TODO: SEBENERNYA BELUM TERBAYAR
                            goToSuccessView(transactionId)
                        }

                        else if (currentUrl.contains("redirection") && currentUrl.contains("#/407")) { // pembayaraan gagal karena melebihi waktu
                            viewModel.onPaymentFailed(transactionId)
                        } else if (currentUrl.contains("transaction_status=pending")) { // TODO: handle klo blum dibayar
                            startActivity(
                                Intent(
                                    this@MidtransWebViewActivity,
                                    MainActivity::class.java
                                )
                            )
                            finishAffinity()
                        }
                    }
                }
            }


            // Load a URL into the WebView
            webView.loadUrl(paymentUrl)
        }
    }

    private fun goToSuccessView(transactionId: String){
        startActivity(
            Intent(
                this@MidtransWebViewActivity,
                PaymentSuccessActivity::class.java
            ).putExtra(
                PaymentSuccessActivity.EXTRA_INTENT_TRANSACTION_ID,
                transactionId
            )
        )
        finishAffinity()
    }

    override fun onBackPressed() {
        val from = intent.getStringExtra(EXTRA_INTENT_FROM)
        if (from == FROM_DETAIL_TRANSACTION){
            super.onBackPressed()
        } else {
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }
    }

    private fun onClickAction() {
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                val from = intent.getStringExtra(EXTRA_INTENT_FROM)
                if (from == FROM_DETAIL_TRANSACTION){
                    super.onBackPressed()
                } else {
                    startActivity(Intent(this@MidtransWebViewActivity, MainActivity::class.java))
                    finishAffinity()
                }
            }
        }
    }
}