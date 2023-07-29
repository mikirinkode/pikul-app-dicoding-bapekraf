package com.mikirinkode.pikul.feature.merchant.dashboard

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mikirinkode.pikul.constants.PAYMENT_STATUS
import com.mikirinkode.pikul.constants.TRANSACTION_STATUS
import com.mikirinkode.pikul.data.model.PikulTransaction
import com.mikirinkode.pikul.data.model.Product
import com.mikirinkode.pikul.databinding.ItemProductSummaryBinding
import com.mikirinkode.pikul.databinding.ItemTransactionBinding
import com.mikirinkode.pikul.databinding.ItemTransactionMerchantBinding
import com.mikirinkode.pikul.feature.customer.transaction.detail.DetailTransactionActivity
import com.mikirinkode.pikul.utils.CommonHelper
import com.mikirinkode.pikul.utils.DateHelper
import com.mikirinkode.pikul.utils.MoneyHelper

class MerchantTransactionAdapter(private val clickListener: MerchantTransactionAdapter.ClickListener) :
    RecyclerView.Adapter<MerchantTransactionAdapter.ViewHolder>() {

    private val list = ArrayList<PikulTransaction>()

    companion object {
        const val ACTION_PROCESS_ORDER = "ACTION_PROCESS_ORDER"
        const val ACTION_ORDER_READY_TO_PICK_UP = "ACTION_ORDER_READY_TO_PICK_UP"
    }

    inner class ViewHolder(private val binding: ItemTransactionMerchantBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: PikulTransaction) {
            binding.apply {
                if (transaction.paymentStatus == PAYMENT_STATUS.FAILED.toString()) {
                    layoutFailedStatus.visibility = View.VISIBLE
                    layoutGoodStatus.visibility = View.GONE
                    tvDummyFailedStatus.text =
                        CommonHelper.getReadAblePaymentStatus(PAYMENT_STATUS.FAILED.toString())
                    tvFailedStatus.text =
                        CommonHelper.getReadAblePaymentStatus(PAYMENT_STATUS.FAILED.toString())
                } else {
                    layoutFailedStatus.visibility = View.GONE
                    layoutGoodStatus.visibility = View.VISIBLE
                    tvGoodStatus.text =
                        CommonHelper.getReadAbleTransactionStatus(transaction.transactionStatus.toString())
                    tvDummyGoodStatus.text =
                        CommonHelper.getReadAbleTransactionStatus(transaction.transactionStatus.toString())
                }
                if (transaction.createdTimestamp != null) {
                    tvTransactionDate.text =
                        DateHelper.getFormattedTransactionDateFromTimestmap(transaction.createdTimestamp!!)
                } else {
                    tvTransactionDate.text = transaction.createdAt
                }

                var order = ""
                transaction.productNames?.forEach { (key, value) ->
                    val thisOrder = "${transaction.productAmounts?.get(key)}x $value\n"
                    order += thisOrder
                }

                tvOrder.text = order
                tvCustomerName.text = transaction.customerName


                if (transaction.transactionStatus == TRANSACTION_STATUS.WAITING_FOR_MERCHANT.toString()) {
                    btnAction.text = "Proses Pesanan"
                    btnAction.visibility = View.VISIBLE
                    btnAction.setOnClickListener {
                        clickListener.onButtonClickListener(ACTION_PROCESS_ORDER, transaction.transactionId ?: "", transaction.customerId ?: "") // TODO: is it safe?
                    }
                } else if (transaction.transactionStatus == TRANSACTION_STATUS.ON_PROCESS_BY_MERCHANT.toString()) {
                    btnAction.text = "Pesanan Telah Selesai"
                    btnAction.visibility = View.VISIBLE
                    btnAction.setOnClickListener {
                        clickListener.onButtonClickListener(ACTION_ORDER_READY_TO_PICK_UP,transaction.transactionId ?: "", transaction.customerId ?: "")
                    }
                } else {
                    btnAction.visibility = View.GONE
                }

            }

            itemView.setOnClickListener { // TODO
//                itemView.context.startActivity(
//                    Intent(
//                        itemView.context,
//                        DetailTransactionActivity::class.java
//                    )
//                        .putExtra(
//                            DetailTransactionActivity.EXTRA_INTENT_TRANSACTION_ID,
//                            transaction.transactionId
//                        )
//                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemTransactionMerchantBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
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

    interface ClickListener {
        fun onButtonClickListener(action: String, transactionId: String, customerId: String)
    }
}