package com.aslilokal.mitra.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aslilokal.mitra.databinding.ItemTransaksiBinding
import com.aslilokal.mitra.model.remote.response.RevenueItem
import com.aslilokal.mitra.utils.CustomFunction

class PencairanAdapter : RecyclerView.Adapter<PencairanAdapter.PencairanViewHolder>() {
    inner class PencairanViewHolder(private val binding: ItemTransaksiBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(pencairan: RevenueItem) {
            binding.txtName.text =
                "${pencairan.informationPayment.providerPayment} - ${pencairan.informationPayment.numberPayment}"
            binding.txtDesc.text =
                "Dibuat pada ${CustomFunction().isoTimeToDateMonth(pencairan.createdAt)}"
            when (pencairan.statusRevenue) {
                "request" -> {
                    binding.txtStatus.text = "Diproses"
                }
                "done" -> {
                    binding.txtStatus.text = "Selesai"
                }
            }
            binding.txtSumTransaction.text =
                CustomFunction().formatRupiah(pencairan.sumRevenueRequest.toDouble())
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<RevenueItem>() {
        override fun areItemsTheSame(oldItem: RevenueItem, newItem: RevenueItem): Boolean {
            return oldItem._id == newItem._id
        }

        override fun areContentsTheSame(oldItem: RevenueItem, newItem: RevenueItem): Boolean {
            return oldItem == newItem
        }
    }


    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PencairanViewHolder =
        PencairanViewHolder(
            ItemTransaksiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: PencairanViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

}