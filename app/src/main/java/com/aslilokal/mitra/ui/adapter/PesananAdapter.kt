package com.aslilokal.mitra.ui.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aslilokal.mitra.databinding.ItemPesananBinding
import com.aslilokal.mitra.model.remote.response.ResultOrder
import com.aslilokal.mitra.utils.Constants.Companion.BUCKET_PRODUCT_URL
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority

class PesananAdapter :
    RecyclerView.Adapter<PesananAdapter.PesananViewHolder>() {

    var onItemClick: ((ResultOrder) -> Unit)? = null

    inner class PesananViewHolder(private val binding: ItemPesananBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(order: ResultOrder) {

            Glide.with(itemView.context)
                .load(BUCKET_PRODUCT_URL + order.products[0].imgProduct)
                .priority(Priority.HIGH)
                .into(binding.imgThumbnailProduct)

            binding.btnAcceptOrder.setOnClickListener {
                onItemClick?.invoke(order)
            }
            binding.txtNameItemProduct.text = order.products[0].nameProduct
            binding.txtItemCount.text = order.products.size.toString() + " item"

            when (order.statusOrder) {
                "acceptrequired" -> {
                    binding.txtStatusOrder.text = "Menunggu konfirmasi"
                }
                "process" -> {
                    binding.txtStatusOrder.text = "Diproses"
                    when (order.isCancelBuyer) {
                        true -> {
                            binding.txtStatusOrder.text = "Pengajuan batal"
                            binding.btnAcceptOrder.setBackgroundColor(Color.parseColor("#F6C358"))
                        }
                    }

                    when (order.courierType) {
                        "seller" -> {
                            binding.btnAcceptOrder.text = "Antar"
                        }
                        "pickup" -> {
                            binding.btnAcceptOrder.text = "Selesai dikemas"
                        }
                        else -> {
                            binding.btnAcceptOrder.text = "Antar"
                        }
                    }
                }
                "delivered" -> {
                    when (order.isCancelBuyer) {
                        true -> {
                            binding.txtStatusOrder.text = "Pengajuan batal"
                            binding.btnAcceptOrder.setBackgroundColor(Color.parseColor("#F6C358"))
                        }
                        else -> {
                            binding.btnAcceptOrder.text = "Lihat detail"
                            binding.btnAcceptOrder.strokeColor =
                                ColorStateList.valueOf(Color.parseColor("#FF7676"))
                            binding.btnAcceptOrder.strokeWidth = 1
                            binding.btnAcceptOrder.setBackgroundColor(Color.WHITE)
                            binding.btnAcceptOrder.setTextColor(Color.parseColor("#FF7676"))
                        }
                    }
                }
                "done" -> {
                    binding.txtStatusOrder.text = "Selesai"
                    binding.txtStatusOrder.setTextColor(Color.WHITE)
                    binding.lnrStatusBackground.setBackgroundColor(Color.parseColor("#FF03DAC5"))

                    binding.btnAcceptOrder.text = "Lihat detail"
                    binding.btnAcceptOrder.strokeColor =
                        ColorStateList.valueOf(Color.parseColor("#FF7676"))
                    binding.btnAcceptOrder.setBackgroundColor(Color.WHITE)
                    binding.btnAcceptOrder.strokeWidth = 1
                    binding.btnAcceptOrder.setTextColor(Color.parseColor("#FF7676"))
                }
            }
//            itemView.setOnClickListener {
//                Toast.makeText(
//                    binding.root.context,
//                    order.products[0].imgProduct,
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<ResultOrder>() {
        override fun areItemsTheSame(oldItem: ResultOrder, newItem: ResultOrder): Boolean {
            return oldItem._id == newItem._id
        }

        override fun areContentsTheSame(oldItem: ResultOrder, newItem: ResultOrder): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PesananViewHolder =
        PesananViewHolder(
            ItemPesananBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: PesananViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size
}