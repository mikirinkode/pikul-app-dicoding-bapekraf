package com.mikirinkode.pikul.feature.customer.jobs

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.constants.PikulRole
import com.mikirinkode.pikul.data.model.Business
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.databinding.ItemJobVacancyBinding
import com.mikirinkode.pikul.databinding.ItemMerchantAvailableBinding
import com.mikirinkode.pikul.feature.chat.room.ChatRoomActivity

class JobVacancyAdapter(
    private val loggedUserId: String,
    private val loggedUserRole: String,
    private val clickListener: ClickListener
) :
    RecyclerView.Adapter<JobVacancyAdapter.ViewHolder>() {

    private val list = ArrayList<Business>()

    inner class ViewHolder(private val binding: ItemJobVacancyBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(business: Business) {
            binding.apply {
                tvBusinessName.text = business.businessName
                tvBusinessAddress.text = business.businessProvince

                if (business.businessPhoto.isNullOrBlank()) {
                    Glide.with(itemView.context)
                        .load(R.drawable.ic_default_user_avatar)
                        .into(ivUserAvatar)
                } else {
                    Glide.with(itemView.context)
                        .load(business.businessPhoto)
                        .placeholder(R.drawable.progress_animation)
                        .into(ivUserAvatar)
                }

                btnSendMessage.setOnClickListener {
                    val loggedUserId = loggedUserId
                    val interlocutorId = business.ownerId
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

                if (loggedUserRole == PikulRole.OWNER.toString()){
                    layoutActionButton.visibility = View.GONE
                } else {
                    layoutActionButton.visibility = View.VISIBLE
                }

                btnApply.setOnClickListener {
                    if (loggedUserRole != PikulRole.MERCHANT.toString()){
                        Toast.makeText(itemView.context, "Maaf anda belum mendaftar sebagai pedagang", Toast.LENGTH_SHORT).show()
                    } else {
                        val loggedUserId = loggedUserId
                        val interlocutorId = business.ownerId
                        if (loggedUserId != null && interlocutorId != null) {
                            val conversationId =
                                if (interlocutorId < loggedUserId) "$interlocutorId-$loggedUserId" else "$loggedUserId-$interlocutorId"

                            clickListener.onApplyClicked(interlocutorId, loggedUserId, conversationId)

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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemJobVacancyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    fun setData(newList: List<Business>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    interface ClickListener {
        fun onApplyClicked(businessOwnerId: String, merchantId: String, conversationId: String)
    }
}