package com.kodelapo.mitra.ui.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kodelapo.mitra.databinding.ItemProductBinding
import com.kodelapo.mitra.model.remote.response.Product
import com.kodelapo.mitra.utils.Constants.Companion.BUCKET_PRODUCT_URL
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
                .into(binding.imgProduct)
            binding.txtCurrentPrice.text = product.priceProduct
            binding.txtNameProduct.text = product.nameProduct
            if (!(product.promoPrice.isNullOrEmpty())) {
                binding.txtPromoPrice.text = product.priceProduct
                binding.txtCurrentPrice.text = product.promoPrice
                binding.lnrPromo.visibility = View.VISIBLE
                var sumCount = (product.promoPrice.toFloat() / product.priceProduct.toFloat())
                var countPercentage = (100 - (sumCount * 100).toInt())
                binding.percentagePromo.text = "$countPercentage %"
                binding.txtPromoPrice.strike()
            } else {
                binding.lnrPromo.visibility = View.INVISIBLE
                binding.txtPromoPrice.visibility = View.GONE
            }
//            itemView.setOnClickListener {
//                Toast.makeText(
//                    binding.root.context,
//                    product.promoPrice,
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
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