package com.mikirinkode.pikul.feature.detail

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.local.LocalPreferenceConstants
import com.mikirinkode.pikul.data.model.Business
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.Product
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.databinding.ActivityDetailBusinessBinding
import com.mikirinkode.pikul.feature.chat.room.ChatRoomActivity
import com.mikirinkode.pikul.feature.profile.OtherUserProfileActivity
import com.mikirinkode.pikul.utils.MoneyHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DetailBusinessActivity : AppCompatActivity(), ProductOrderAdapter.ClickListener {

    private val binding: ActivityDetailBusinessBinding by lazy {
        ActivityDetailBusinessBinding.inflate(layoutInflater)
    }

    private val viewModel: DetailBusinessViewModel by viewModels()

    private lateinit var adapter: ProductOrderAdapter

    @Inject
    lateinit var pref: LocalPreference

    private val user: UserAccount? by lazy {
        pref.getObject(LocalPreferenceConstants.USER, UserAccount::class.java)
    }

    companion object {
        const val EXTRA_INTENT_BUSINESS_ID = "EXTRA_INTENT_BUSINESS_ID"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        handleIntent()
        initRecyclerView()
        onClickAction()
    }


    private fun handleIntent() {
        val extras = intent.extras
        val businessId = extras?.getString(EXTRA_INTENT_BUSINESS_ID)
        observeData(businessId)
        initAdapter(businessId)
    }

    private fun initAdapter(ownerId: String?){
        adapter = ProductOrderAdapter(ownerId ?: "", this)
    }

    private fun initRecyclerView() {
        binding.apply {
            rvProducts.layoutManager = LinearLayoutManager(this@DetailBusinessActivity)
            rvProducts.adapter = adapter
        }
    }

    private fun observeData(businessId: String?) {
        if (businessId != null) {
            viewModel.getBusinessData(businessId).observe(this) { result ->
                when (result) {
                    is PikulResult.Loading -> {}
                    is PikulResult.LoadingWithProgress -> {}
                    is PikulResult.Error -> {}
                    is PikulResult.Success -> {
                        initView(result.data)
                    }
                }
            }
            viewModel.getProductList(businessId).observe(this) { result ->
                when (result) {
                    is PikulResult.Loading -> {}
                    is PikulResult.LoadingWithProgress -> {}
                    is PikulResult.Error -> {}
                    is PikulResult.Success -> {
                        adapter.setData(result.data)
                    }
                }
            }

            viewModel.getMerchantData(businessId).observe(this) { result ->
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
                Glide.with(this@DetailBusinessActivity)
                    .load(merchant.avatarUrl)
                    .placeholder(R.drawable.progress_animation)
                    .into(ivMerchantAvatar)
            } else {
                Glide.with(this@DetailBusinessActivity)
                    .load(R.drawable.ic_default_user_avatar).into(ivMerchantAvatar)
            }

            layoutMerchant.setOnClickListener {
                startActivity(Intent(this@DetailBusinessActivity, OtherUserProfileActivity::class.java))
            }

            btnChat.setOnClickListener {
                val loggedUserId = user?.userId
                val interlocutorId = merchant.userId
                if (loggedUserId != null && interlocutorId != null) {
                val conversationId = if (interlocutorId < loggedUserId) "$interlocutorId-$loggedUserId" else "$loggedUserId-$interlocutorId"
                startActivity(
                    Intent(
                        this@DetailBusinessActivity,
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

            Glide.with(this@DetailBusinessActivity)
                .load(data.businessPhoto)
                .placeholder(R.drawable.progress_animation)
                .into(ivBusinessPhoto)


        }
    }

    override fun onProductOrderAmountChanged(product: Product, amount: Int) {
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

        }
    }
}