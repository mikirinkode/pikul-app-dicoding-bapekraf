package com.mikirinkode.pikul.feature.order

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.model.Product
import com.mikirinkode.pikul.databinding.ItemProductOrderBinding
import com.mikirinkode.pikul.databinding.ItemProductSummaryBinding
import com.mikirinkode.pikul.utils.MoneyHelper

class ProductSummaryAdapter(private val merchantId: String, private val clickListener: ClickListener) :
    RecyclerView.Adapter<ProductSummaryAdapter.ViewHolder>() {

    private val list: ArrayList<Product> = ArrayList()

    // to save the product order
    // the key is the product id
    // the value is the product amount
//    private val productOrderItemAmount: MutableMap<String, Int> = mutableMapOf()


    inner class ViewHolder(private val binding: ItemProductSummaryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product, position: Int) {
            binding.apply {
                tvItemName.text = product.productName
                tvItemAmountAndPrice.text = "${product.totalAmount} x ${MoneyHelper.getFormattedPrice(product.productPrice ?: 0f)}"
                val total = product.productPrice?.times(product.totalAmount)
                tvItemTotalPrice.text = MoneyHelper.getFormattedPrice(total ?: 0f)

                val stock = product.productStocks?.get(merchantId) ?: 0

                val totalAmount: Int = product.totalAmount
                etItemOrderAmount.setText(totalAmount.toString())

                if (totalAmount == 0) {
                    btnMinusAmount.visibility = View.GONE
                    etItemOrderAmount.visibility = View.GONE
                } else {
                    btnMinusAmount.visibility = View.VISIBLE
                    etItemOrderAmount.visibility = View.VISIBLE
                }

                btnPlusAmount.setOnClickListener {
                    val newAmount = product.totalAmount + 1
                    if (newAmount <= stock) {
                        product.totalAmount = newAmount
                    } else {
                        Toast.makeText(
                            itemView.context,
                            "Maksimal Produk sudah tercapai",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    clickListener.onProductOrderAmountChanged()
                    notifyItemChanged(position)
                }

                btnMinusAmount.setOnClickListener {
                    val newAmount = product.totalAmount - 1
                    if (newAmount >= 0) {
                        product.totalAmount = newAmount
                    }
                    clickListener.onProductOrderAmountChanged()
                    notifyItemChanged(position)
                }
            }
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductSummaryAdapter.ViewHolder {
        val binding =
            ItemProductSummaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ProductSummaryAdapter.ViewHolder, position: Int) {
        holder.bind(list[position], position)
    }

    fun setData(newList: List<Product>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    fun getProductById(productId: String): Product? {
        for (product in list) {
            if (product.productId == productId) {
                return product
            }
        }
        return null
    }

    fun getTotalOrderItemAmount(): Int {
        var totalItem = 0
        for (product in list) {
            totalItem += product.totalAmount
        }

        return totalItem
    }

    fun getBookedProducts(): List<Product> {
        val products = ArrayList<Product>()
        for (product in list){
            if (product.totalAmount > 0){
                products.add(product)
            }
        }

        return products
    }

    fun getTotalOrderBilling(): Float {
        var totalBilling: Float = 0f
        for (product in list) {
            if (product.totalAmount > 0) {
                val billing: Float = product.productPrice?.times(product.totalAmount) ?: 0f
                totalBilling += billing
            }
        }
        return totalBilling
    }

    interface ClickListener {
        fun onProductOrderAmountChanged()
    }
}