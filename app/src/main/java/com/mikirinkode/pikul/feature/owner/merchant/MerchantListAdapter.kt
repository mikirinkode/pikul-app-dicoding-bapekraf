package com.mikirinkode.pikul.feature.owner.merchant

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.model.PikulTransaction
import com.mikirinkode.pikul.data.model.Product
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.databinding.ItemMerchantAvailableBinding
import com.mikirinkode.pikul.databinding.ItemProductSummaryBinding
import com.mikirinkode.pikul.databinding.ItemTransactionBinding
import com.mikirinkode.pikul.feature.chat.room.ChatRoomActivity

class MerchantListAdapter(private val businessOwnerId: String, private val clickListener: ClickListener) :
    RecyclerView.Adapter<MerchantListAdapter.ViewHolder>() {

    private val list = ArrayList<UserAccount>()

    inner class ViewHolder(private val binding: ItemMerchantAvailableBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(merchant: UserAccount) {
            binding.apply {
                tvMerchantName.text = merchant.name
                tvMerchantAddress.text = merchant.province

                if (merchant?.avatarUrl.isNullOrBlank()) {
                    Glide.with(itemView.context)
                        .load(R.drawable.ic_default_user_avatar)
                        .into(ivUserAvatar)
                } else {
                    Glide.with(itemView.context)
                        .load(merchant?.avatarUrl)
                        .placeholder(R.drawable.progress_animation)
                        .into(ivUserAvatar)
                }

                btnSendMessage.setOnClickListener {
                    val loggedUserId = businessOwnerId
                    val interlocutorId = merchant.userId
                    if (loggedUserId != null && interlocutorId != null) {
                        val conversationId =
                            if (interlocutorId < loggedUserId) "$interlocutorId-$loggedUserId" else "$loggedUserId-$interlocutorId"
                        itemView.context.startActivity(
                            Intent(
                                itemView.context,
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

                btnInvite.setOnClickListener {
                    val loggedUserId = businessOwnerId
                    val interlocutorId = merchant.userId
                    if (loggedUserId != null && interlocutorId != null) {
                        val conversationId =
                            if (interlocutorId < loggedUserId) "$interlocutorId-$loggedUserId" else "$loggedUserId-$interlocutorId"

                        // TODO: NEXT CREATE CONFIRMATION

                        // send invite
                        clickListener.onInviteClicked(businessOwnerId, interlocutorId, conversationId)


                        itemView.context.startActivity(
                            Intent(
                                itemView.context,
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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemMerchantAvailableBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    fun setData(newList: List<UserAccount>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    interface ClickListener {
        fun onInviteClicked(businessOwnerId: String, merchantId: String, conversationId: String)
    }
}