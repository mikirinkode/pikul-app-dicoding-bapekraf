package com.mikirinkode.pikul.feature.payment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.navigation.fragment.navArgs
import com.mikirinkode.pikul.databinding.FragmentMidtransWebViewBinding
import com.mikirinkode.pikul.feature.customer.main.MainActivity
import com.mikirinkode.pikul.feature.order.PaymentSuccessActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MidtransWebViewFragment : Fragment() {
    private var _binding: FragmentMidtransWebViewBinding? = null
    private val binding get() = _binding!!

//    private val args: MidtransWebViewFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMidtransWebViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        initView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        _binding = null
    }

//    private fun initView() {
//        binding.apply {
//            topAppBar.setNavigationOnClickListener {
//                // TODO
//            }
//            // Enable JavaScript (optional, only if required by the web content)
//            val webSettings: WebSettings = webView.settings
//            webSettings.javaScriptEnabled = true
//
//            webView.webViewClient = object : WebViewClient() {
//                override fun shouldOverrideUrlLoading(
//                    view: WebView?,
//                    request: WebResourceRequest?
//                ): Boolean {
//                    view?.loadUrl(request?.url.toString())
//                    return true
//                }
//
//                override fun onPageFinished(view: WebView?, url: String?) {
//                    super.onPageFinished(view, url)
//                    val currentUrl = webView.url
//
//                    Log.e("MidtransFragment", "onPageFinished: ${currentUrl}")
//
//                    if (currentUrl != null) {
//                        if (currentUrl.contains("transaction_status=settlement") ||
//                            currentUrl.contains("transaction_status=capture")
//                        ) {
////                            val action = MidtransWebViewFragmentDirections.actionOnPaymentSuccess()
////                            Navigation.findNavController(binding.root).navigate(action) // TODO: ERROR, kayaknya harus pke activity
//                            startActivity(
//                                Intent(
//                                    requireContext(),
//                                    PaymentSuccessActivity::class.java
//                                ).putExtra(
//                                    PaymentSuccessActivity.EXTRA_INTENT_TRANSACTION_ID,
//                                    args.transactionId
//                                )
//                            )
//                            requireActivity().finishAffinity()
//                        } else if (currentUrl.contains("transaction_status=pending")) { // TODO: handle klo blum dibayar
//                            startActivity(Intent(requireContext(), MainActivity::class.java))
//                            requireActivity().finishAffinity()
//                        }
//                    }
//                }
//            }
//
//
//            // Load a URL into the WebView
//            webView.loadUrl(args.paymentUrl)
//        }
//    }


}