package com.aslilokal.mitra.ui.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.aslilokal.mitra.databinding.ItemProductBinding
import com.aslilokal.mitra.model.remote.response.Product
import com.aslilokal.mitra.ui.kelola.edit.EditProductActivity
import com.aslilokal.mitra.utils.Constants.Companion.BUCKET_PRODUCT_URL
import com.tuonbondol.textviewutil.strike


class ProductAdapter :
    RecyclerView.Adapter<ProductAdapter.GridProductViewHolder>() {

    inner class GridProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(product: Product) {
            binding.imgProduct.setBackgroundColor(Color.rgb(217, 217, 217))

            Glide.with(itemView.context)
                .load(BUCKET_PRODUCT_URL + product.imgProduct)
                .priority(Priority.HIGH)
                .into(binding.imgProduct)

            binding.txtCurrentPrice.text = product.priceProduct
            binding.txtNameProduct.text = product.nameProduct
            when (product.promoPrice?.toIntOrNull()) {
                0 -> {
                    binding.lnrPromo.visibility = View.INVISIBLE
                    binding.txtPromoPrice.visibility = View.GONE
                }
                null -> {
                    binding.lnrPromo.visibility = View.INVISIBLE
                    binding.txtPromoPrice.visibility = View.GONE
                }
                else -> {
                    binding.txtPromoPrice.text = product.priceProduct
                    binding.txtCurrentPrice.text = product.promoPrice
                    binding.lnrPromo.visibility = View.VISIBLE
                    val sumCount =
                        (product.promoPrice.toFloat().div(product.priceProduct.toFloat()))
                    val countPercentage = (100 - (sumCount.times(100)).toInt())
                    binding.txtPromoPrice.strike()
                    binding.percentagePromo.text = "$countPercentage %"
                }
            }
            itemView.setOnClickListener {
//                Toast.makeText(binding.root.context, product._id, Toast.LENGTH_SHORT).show()
                val intent = Intent(binding.root.context, EditProductActivity::class.java)
                intent.putExtra("idProduct", product._id)
                binding.root.context.startActivity(intent)
            }
        }
    }


    private val differCallback = object : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem._id == newItem._id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridProductViewHolder =
        GridProductViewHolder(
            ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: GridProductViewHolder, position: Int) {
        val product = differ.currentList[position]
        holder.bind(product)
    }

    override fun getItemCount(): Int = differ.currentList.size
}