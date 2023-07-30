package com.mikirinkode.pikul.feature.owner.transaction

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mikirinkode.pikul.constants.PAYMENT_STATUS
import com.mikirinkode.pikul.constants.TRANSACTION_STATUS
import com.mikirinkode.pikul.data.model.PikulTransaction
import com.mikirinkode.pikul.databinding.ItemTransactionBinding
import com.mikirinkode.pikul.databinding.ItemTransactionOwnerBinding
import com.mikirinkode.pikul.feature.customer.transaction.detail.DetailTransactionActivity
import com.mikirinkode.pikul.feature.merchant.transaction.MerchantDetailTransactionActivity
import com.mikirinkode.pikul.utils.CommonHelper
import com.mikirinkode.pikul.utils.DateHelper
import com.mikirinkode.pikul.utils.MoneyHelper


class OwnerTransactionListAdapter : RecyclerView.Adapter<OwnerTransactionListAdapter.ViewHolder>() {

    private val list = ArrayList<PikulTransaction>()


    inner class ViewHolder(private val binding: ItemTransactionOwnerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: PikulTransaction) {
            binding.apply {
                if (transaction.paymentStatus == PAYMENT_STATUS.FAILED.toString()){
                    layoutFailedStatus.visibility = View.VISIBLE
                    layoutGoodStatus.visibility = View.GONE
                    tvDummyFailedStatus.text = CommonHelper.getReadAblePaymentStatus(PAYMENT_STATUS.FAILED.toString())
                    tvFailedStatus.text = CommonHelper.getReadAblePaymentStatus(PAYMENT_STATUS.FAILED.toString())
                } else {
                    layoutFailedStatus.visibility = View.GONE
                    layoutGoodStatus.visibility = View.VISIBLE
                    tvGoodStatus.text = CommonHelper.getReadAbleTransactionStatus(transaction.transactionStatus.toString())
                    tvDummyGoodStatus.text = CommonHelper.getReadAbleTransactionStatus(transaction.transactionStatus.toString())

                }



                tvBusinessName.text = transaction.businessName
                tvTransactionTotalBilling.text = MoneyHelper.getFormattedPrice(transaction.totalBilling ?: 0f)
                if (transaction.createdTimestamp != null){
                    tvTransactionDate.text = DateHelper.getFormattedTransactionDateFromTimestmap(transaction.createdTimestamp!!)
                } else {
                    tvTransactionDate.text = transaction.createdAt
                }
            }

            itemView.setOnClickListener {
                itemView.context.startActivity(
                    Intent(
                        itemView.context,
                        MerchantDetailTransactionActivity::class.java
                    )
                        .putExtra(
                            MerchantDetailTransactionActivity.EXTRA_INTENT_TRANSACTION_ID,
                            transaction.transactionId
                        )
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemTransactionOwnerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    fun setData(newList: List<PikulTransaction>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }


}