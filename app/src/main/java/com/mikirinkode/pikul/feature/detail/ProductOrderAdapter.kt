package com.mikirinkode.pikul.feature.detail

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.model.Product
import com.mikirinkode.pikul.databinding.ItemOrderProductBinding
import com.mikirinkode.pikul.databinding.ItemProductBinding
import com.mikirinkode.pikul.feature.owner.product.ProductListAdapter
import com.mikirinkode.pikul.utils.MoneyHelper

class ProductOrderAdapter(private val ownerId: String, private val clickListener: ClickListener) :
    RecyclerView.Adapter<ProductOrderAdapter.ViewHolder>() {

    private val list: ArrayList<Product> = ArrayList()

    // to save the product order
    // the key is the product id
    // the value is the product amount
    private val productOrderItemAmount: MutableMap<String, Int> = mutableMapOf()


    inner class ViewHolder(private val binding: ItemOrderProductBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product, position: Int) {
            binding.apply {
                tvItemName.text = product.productName
                tvItemPrice.text = MoneyHelper.getFormattedPrice(product.productPrice ?: 0f)

                Log.e("ProductOrderAdapter", "\n it's first line")
                Log.e("ProductOrderAdapter", "owner id: ${ownerId}")

                val stock = product.productStocks?.get(ownerId) ?: 0
                Log.e("ProductOrderAdapter", "stock: ${stock}")
                tvItemStock.text = "Stok: ${stock}"

                Glide.with(itemView.context)
                    .load(product.productThumbnailUrl)
                    .placeholder(R.drawable.progress_animation)
                    .into(ivItemPhoto)


                val totalAmount: Int = productOrderItemAmount[product.productId] ?: 0
                etItemOrderAmount.setText(totalAmount.toString())

                Log.e("ProductOrderAdapter", "amount: ${totalAmount}")
                Log.e("ProductOrderAdapter", "productOrderItemAmount[product.productId]: ${productOrderItemAmount[product.productId]}")
                Log.e("ProductOrderAdapter", "productId: ${product.productId}")

                if (totalAmount == 0) {
                    btnMinusAmount.visibility = View.GONE
                    etItemOrderAmount.visibility = View.GONE
                } else {
                    btnMinusAmount.visibility = View.VISIBLE
                    etItemOrderAmount.visibility = View.VISIBLE
                }

                btnPlusAmount.setOnClickListener {
                    if (productOrderItemAmount[product.productId] == null && product.productId != null){
                        productOrderItemAmount[product.productId!!] = 1
                    } else {
                        val newAmount = productOrderItemAmount[product.productId!!]!! + 1
                        if (newAmount > stock){
                            Toast.makeText(itemView.context, "Maksimal Produk sudah tercapai", Toast.LENGTH_SHORT).show()
                        } else {
                            productOrderItemAmount[product.productId!!] = productOrderItemAmount[product.productId!!]!! + 1
                        }
//                        productOrderItemAmount[product.productId]?.plus(1)
                    }
//                    totalAmount.plus(1)
                    clickListener.onProductOrderAmountChanged(product, productOrderItemAmount[product.productId] ?: 0)
                    notifyItemChanged(position)
                    Log.e("ProductOrderAdapter", "plus clicked")
                    Log.e("ProductOrderAdapter", "plus clicked ${productOrderItemAmount[product.productId]?.plus(1)}")
                    Log.e("ProductOrderAdapter", "amount: ${totalAmount}")
                    Log.e("ProductOrderAdapter", "productOrderItemAmount[product.productId] == null && product.productId != null: ${productOrderItemAmount[product.productId] == null && product.productId != null}")
                    Log.e("ProductOrderAdapter", "productOrderItemAmount[product.productId]: ${productOrderItemAmount[product.productId]}")
                }

                btnMinusAmount.setOnClickListener {
                    if (totalAmount > 0 && productOrderItemAmount[product.productId!!] != null) {
                        productOrderItemAmount[product.productId!!] = productOrderItemAmount[product.productId!!]!! - 1
//                        productOrderItemAmount[product.productId]?.minus(1)
//                        totalAmount.minus(1)
                    } else {
                        if (product.productId != null){
                            productOrderItemAmount[product.productId!!] = 0
                        }
//                        totalAmount = 0
                    }
                    clickListener.onProductOrderAmountChanged(product, productOrderItemAmount[product.productId] ?: 0)
                    notifyItemChanged(position)
                    Log.e("ProductOrderAdapter", "minus clicked")
                    Log.e("ProductOrderAdapter", "amount: ${totalAmount}")
                }
            }
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductOrderAdapter.ViewHolder {
        val binding =
            ItemOrderProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ProductOrderAdapter.ViewHolder, position: Int) {
        holder.bind(list[position], position)
    }

    fun setData(newList: List<Product>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    fun getProductById(productId: String): Product? {
        for (product in list){
            if (product.productId == productId){
                return product
            }
        }
        return  null
    }
    
    fun getTotalOrderItemAmount(): Int {
        var totalItem = 0
        productOrderItemAmount.forEach { (productId, amount) ->
           totalItem += amount
        }
        return totalItem
    }
    
    fun getTotalOrderBilling(): Float {
        var totalBilling: Float = 0f
        productOrderItemAmount.forEach { (productId, amount) ->
            val product = getProductById(productId)
            if (product != null){
                val billing: Float = product.productPrice?.times(amount) ?: 0f
                totalBilling += billing
            }
        }
        return totalBilling
    }

    interface ClickListener{
        fun onProductOrderAmountChanged(product: Product, amount: Int)
    }
}