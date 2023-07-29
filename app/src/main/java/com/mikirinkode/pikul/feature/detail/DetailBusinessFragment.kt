package com.mikirinkode.pikul.feature.detail

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.constants.PAYMENT_STATUS
import com.mikirinkode.pikul.constants.TRANSACTION_STATUS
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.local.LocalPreferenceConstants
import com.mikirinkode.pikul.data.model.*
import com.mikirinkode.pikul.data.model.maps.SellingPlace
import com.mikirinkode.pikul.databinding.FragmentDetailBusinessBinding
import com.mikirinkode.pikul.databinding.FragmentJobVacancyBinding
import com.mikirinkode.pikul.feature.chat.room.ChatRoomActivity
import com.mikirinkode.pikul.feature.profile.OtherUserProfileActivity
import com.mikirinkode.pikul.utils.DateHelper
import com.mikirinkode.pikul.utils.MoneyHelper
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * TODO:
 * ADD LOADING INDICATOR
 * ON ERROR INDICATOR
 * ADD MAPS
 * ADD SELLING PLACE dan gunakan time yang ada disini untuk validasi pesanan
 */
@AndroidEntryPoint
class DetailBusinessFragment : Fragment(), ProductOrderAdapter.ClickListener {
    private var _binding: FragmentDetailBusinessBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetailBusinessViewModel by viewModels()

    private lateinit var adapter: ProductOrderAdapter

    @Inject
    lateinit var pref: LocalPreference

    private val user: UserAccount? by lazy {
        pref.getObject(LocalPreferenceConstants.USER, UserAccount::class.java)
    }

    private var merchantStartTime: String? = null
    private var merchantEndTime: String? = null

    private val args: DetailBusinessFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDetailBusinessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // if merchant id is null, then the merchant will be the owner
        // but what if the owner is not selling the data?
        // TODO: HANDLE THIS, i think need some validation at home screen
        val merchantId: String = if (args.merchantId != null) args.merchantId!! else args.businessId
        initAdapter(merchantId) // TODO: IT SHOULD BE MERCHANT ID!
        observeData(args.businessId, merchantId)
        initRecyclerView()
        onClickAction()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initAdapter(merchantId: String?) {
        adapter = ProductOrderAdapter(merchantId ?: "", this)
    }

    private fun initRecyclerView() {
        binding.apply {
            rvProducts.layoutManager = LinearLayoutManager(requireContext())
            rvProducts.adapter = adapter
        }
    }

    private fun observeData(businessId: String, merchantId: String) {
        if (businessId != null) {
            viewModel.getBusinessData(businessId).observe(viewLifecycleOwner) { result ->
                when (result) {
                    is PikulResult.Loading -> {}
                    is PikulResult.LoadingWithProgress -> {}
                    is PikulResult.Error -> {}
                    is PikulResult.Success -> {
                        adapter.setBusinessData(result.data)
                        initView(result.data)
                    }
                }
            }
            viewModel.getProductList(businessId).observe(viewLifecycleOwner) { result ->
                when (result) {
                    is PikulResult.Loading -> {}
                    is PikulResult.LoadingWithProgress -> {}
                    is PikulResult.Error -> {}
                    is PikulResult.Success -> {
                        adapter.setData(result.data)
                    }
                }
            }

            viewModel.getMerchantData(merchantId).observe(viewLifecycleOwner) { result ->
                when (result) {
                    is PikulResult.Loading -> {}
                    is PikulResult.LoadingWithProgress -> {}
                    is PikulResult.Error -> {}
                    is PikulResult.Success -> {
                        adapter.setMerchantData(result.data)
                        initMerchantView(result.data)
                    }
                }
            }

            viewModel.getMerchantPlace(merchantId).observe(viewLifecycleOwner) { result ->
                when (result) {
                    is PikulResult.Loading -> {}
                    is PikulResult.LoadingWithProgress -> {}
                    is PikulResult.Error -> {}
                    is PikulResult.Success -> {
                        adapter.setSellingPlace(result.data)
                        initMerchantPlace(result.data)
                    }
                }
            }
        }
    }

    private fun initMerchantView(merchant: UserAccount) {
        binding.apply {
            tvMerchantName.text = merchant.name

            if (merchant.avatarUrl != null && merchant.avatarUrl != "") {
                Glide.with(requireContext())
                    .load(merchant.avatarUrl)
                    .placeholder(R.drawable.progress_animation)
                    .into(ivMerchantAvatar)
            } else {
                Glide.with(requireContext())
                    .load(R.drawable.ic_default_user_avatar).into(ivMerchantAvatar)
            }

            // TODO
            layoutMerchantProfile.setOnClickListener {
//                startActivity(Intent(requireContext(), OtherUserProfileActivity::class.java))
            }

            btnChat.setOnClickListener {
                val loggedUserId = user?.userId
                val interlocutorId = merchant.userId
                if (loggedUserId != null && interlocutorId != null) {
                    val conversationId =
                        if (interlocutorId < loggedUserId) "$interlocutorId-$loggedUserId" else "$loggedUserId-$interlocutorId"
                    startActivity(
                        Intent(
                            requireContext(),
                            ChatRoomActivity::class.java
                        ).putExtra(
                            ChatRoomActivity.EXTRA_INTENT_CONVERSATION_ID,
                            conversationId
                        ).putExtra(
                            ChatRoomActivity.EXTRA_INTENT_INTERLOCUTOR_ID,
                            interlocutorId
                        )
                    )
                }
            }
        }
    }

    private fun initMerchantPlace(sellingPlace: SellingPlace) {
        binding.apply {
            if (sellingPlace.placeNoteForCustomer != "") {
                layoutMerchantNote.visibility = View.VISIBLE
                tvMerchantNote.text = sellingPlace.placeNoteForCustomer
            } else {
                layoutMerchantNote.visibility = View.GONE
            }

            merchantStartTime = sellingPlace.startTime
            merchantEndTime = sellingPlace.endTime
            tvMerchantAddress.text = sellingPlace.placeAddress
            tvTime.text = "${sellingPlace.startTime} - ${sellingPlace.endTime}"

            // TODO
            btnSeeLocationOnMap.setOnClickListener {

            }
        }
    }

    private fun initView(data: Business) {
        binding.apply {
            tvBrandName.text = data.businessName
            tvBrandRating.text = data.businessRating.toString()
            tvBrandDesc.text = data.businessDescription
            tvBrandCategory.text = data.businessProductCategory

            Glide.with(requireContext())
                .load(data.businessPhoto)
                .placeholder(R.drawable.progress_animation)
                .into(ivBusinessPhoto)


        }
    }

    override fun onProductOrderAmountChanged() {
        val totalOrderItemAmount = adapter.getTotalOrderItemAmount()
        val totalOrderBillingAmount = adapter.getTotalOrderBilling()

        binding.apply {
            if (totalOrderItemAmount > 0) {
                btnOrder.text = "Pesan $totalOrderItemAmount item ${
                    MoneyHelper.getFormattedPrice(totalOrderBillingAmount)
                }"
            } else {
                btnOrder.text = getString(R.string.txt_btn_order)
            }
        }
    }

    private fun isTimeValid(): Boolean {
        val currentHour = DateHelper.getCurrentHour().toInt()
        val currentMinute = DateHelper.getCurrentMinute().toInt()

        if (merchantStartTime != null&& merchantEndTime != null){
            val (startHour, startMinute) = merchantStartTime!!.split(":")
            val (endHour, endMinute) = merchantEndTime!!.split(":")


            val currentTimeInMinutes = currentHour * 60 + currentMinute
            val startTimeInMinutes = startHour.toInt() * 60 + startMinute.toInt()
            val endTimeInMinutes = endHour.toInt() * 60 + endMinute.toInt()

            Log.e("DetailBusinessFragment", "current hour: $currentHour, current min: $currentMinute")
            Log.e("DetailBusinessFragment", "start hour: $startHour, start min: $startMinute")
            Log.e("DetailBusinessFragment", "end hour: $endHour, end min: $endMinute")
            Log.e("DetailBusinessFragment", "current time in minutes: $currentTimeInMinutes")
            Log.e("DetailBusinessFragment", "start time in minutes: $startTimeInMinutes")
            Log.e("DetailBusinessFragment", "end time in minutes: $endTimeInMinutes")
            Log.e("DetailBusinessFragment", "valid: ${(currentTimeInMinutes in startTimeInMinutes..endTimeInMinutes)}")

            return currentTimeInMinutes in startTimeInMinutes..endTimeInMinutes
        } else {
            return false
        }
    }


    private fun onClickAction() {
        binding.apply {
            toolbar.setNavigationOnClickListener {
                requireActivity().onBackPressed()
            }

            btnOrder.setOnClickListener {
                var isValid = true
                val listOfProducts = adapter.getBookedProducts()
                if (!isTimeValid()) {
                    isValid = false
                    Toast.makeText(
                        requireContext(),
                        "Maaf saat ini pedagang sedang tidak berjualan",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (listOfProducts.isEmpty()) {
                    isValid = false
                    Toast.makeText(
                        requireContext(),
                        "Anda belum memilih produk",
                        Toast.LENGTH_SHORT
                    ).show()
                }



                if (isValid) {
                    val transactionTotalItem = adapter.getTotalOrderItemAmount()
                    val transactionTotalBilling = adapter.getTotalOrderBilling()
                    val sellingPlace = adapter.getSellingPlace()
                    val pickupAddress = sellingPlace?.placeAddress
                    val pickupCoordinates = sellingPlace?.coordinate

                    val businessId = args.businessId
                    val businessName = adapter.getBusinessData()?.businessName
                    val merchantId = args.merchantId
                    val merchantName = adapter.getMerchantData()?.name

                    val productIdWithName = mutableMapOf<String, String>()
                    val productIdWithPrice = mutableMapOf<String, Float>()
                    val productIdWithAmount = mutableMapOf<String, Int>()

                    for (product in listOfProducts){
                        if (product.productId != null && product.productName != null){
                            productIdWithName[product.productId!!] = product.productName!!
                            productIdWithPrice[product.productId!!] = product.productPrice!!
                            productIdWithAmount[product.productId!!] = product.totalAmount
                        }
                    }

                    val pikulTransaction = PikulTransaction(
                        transactionId = null,
                        alreadyPaid = false,
                        paymentStatus = null,
                        paymentUrl = null,
                        paidAt = null,
                        transactionStatus = null,
                        totalItem = transactionTotalItem,
                        totalBilling = transactionTotalBilling,
                        pickupAddress = pickupAddress,
                        pickupCoordinates = pickupCoordinates,

                        productNames = productIdWithName,
                        productPrices = productIdWithPrice,
                        productAmounts = productIdWithAmount,

                        customerId = null,

                        businessId = businessId,
                        businessName = businessName,

                        merchantId = merchantId,
                        merchantName = merchantName,

                        createdTimestamp = null,
                        createdAt = null,
                        updatedAt = null,
                    )

                    pref.saveObject(LocalPreferenceConstants.TRANSACTION_ORDER, pikulTransaction)

                    val action = DetailBusinessFragmentDirections.actionOrderSummary(
                        args.businessId,
                        args.businessId,
                        listOfProducts.toTypedArray(),
                        pikulTransaction
                    ) // TODO: MERCHANT ID
                    Navigation.findNavController(binding.root).navigate(action)
                }
            }
        }
    }
}