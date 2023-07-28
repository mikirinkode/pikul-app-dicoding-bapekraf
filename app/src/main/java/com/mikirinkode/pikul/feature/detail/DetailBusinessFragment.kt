package com.mikirinkode.pikul.feature.detail

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.local.LocalPreferenceConstants
import com.mikirinkode.pikul.data.model.Business
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.Product
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.databinding.FragmentDetailBusinessBinding
import com.mikirinkode.pikul.databinding.FragmentJobVacancyBinding
import com.mikirinkode.pikul.feature.chat.room.ChatRoomActivity
import com.mikirinkode.pikul.feature.profile.OtherUserProfileActivity
import com.mikirinkode.pikul.utils.MoneyHelper
import dagger.hilt.android.AndroidEntryPoint
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
        val merchantId : String = if (args.merchantId != null) args.merchantId!! else args.businessId
        initAdapter(merchantId) // TODO: IT SHOULD BE MERCHANT ID!
        observeData(args.businessId, merchantId)
        initRecyclerView()
        onClickAction()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initAdapter(merchantId: String?){
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
                        initMerchantView(result.data)
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

            layoutMerchant.setOnClickListener {
                startActivity(Intent(requireContext(), OtherUserProfileActivity::class.java))
            }

            btnChat.setOnClickListener {
                val loggedUserId = user?.userId
                val interlocutorId = merchant.userId
                if (loggedUserId != null && interlocutorId != null) {
                    val conversationId = if (interlocutorId < loggedUserId) "$interlocutorId-$loggedUserId" else "$loggedUserId-$interlocutorId"
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
                        ))
                }
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
            if (totalOrderItemAmount > 0){
                btnOrder.text = "Pesan $totalOrderItemAmount item ${MoneyHelper.getFormattedPrice(totalOrderBillingAmount)}"
            } else {
                btnOrder.text = getString(R.string.txt_btn_order)
            }
        }
    }

    private fun onClickAction(){
        binding.apply {
            toolbar.setNavigationOnClickListener {
                requireActivity().onBackPressed()
            }

            btnOrder.setOnClickListener {
                val listOfProducts = adapter.getBookedProducts().toTypedArray()
                val action = DetailBusinessFragmentDirections.actionOrderSummary(
                    args.businessId,
                    args.businessId,
                    listOfProducts
                ) // TODO: MERCHANT ID
                Navigation.findNavController(binding.root).navigate(action)
            }
        }
    }
}