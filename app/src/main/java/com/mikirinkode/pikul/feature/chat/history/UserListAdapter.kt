package com.mikirinkode.pikul.feature.chat.history

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.databinding.ItemUserBinding
import com.mikirinkode.pikul.feature.chat.room.ChatRoomActivity


class UserListAdapter : RecyclerView.Adapter<UserListAdapter.ViewHolder>() {

    private val userList: ArrayList<UserAccount> = ArrayList()

    var userClickListener: UserClickListener? = null

    inner class ViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: UserAccount) {
            binding.apply {
                tvUserName.text = user.name

                if (user.avatarUrl != null && user.avatarUrl != "") {
                    Glide.with(itemView.context)
                        .load(user.avatarUrl).into(binding.ivUserAvatar)
                } else {
                    Glide.with(itemView.context)
                        .load(R.drawable.ic_default_user_avatar).into(binding.ivUserAvatar)
                }
            }
            itemView.setOnClickListener {
                userClickListener?.onUserClick(user)

                val interlocutorId = user.userId
                val loggedUserId = "FGz3dlEn9AYHbuTcKv3fLol8cp32"
                if (loggedUserId != null && interlocutorId != null) {
                    val conversationId = if (interlocutorId < loggedUserId) "$interlocutorId-$loggedUserId" else "$loggedUserId-$interlocutorId"
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(userList[position])
    }

    fun setData(newList: List<UserAccount>) {
        userList.clear()
        userList.addAll(newList)
        notifyDataSetChanged()
    }

    interface UserClickListener {
        fun onUserClick(user: UserAccount)
    }
}