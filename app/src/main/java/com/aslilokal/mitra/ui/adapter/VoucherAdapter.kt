package com.aslilokal.mitra.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aslilokal.mitra.databinding.ItemVoucherBinding
import com.aslilokal.mitra.model.remote.response.VoucherItem
import com.aslilokal.mitra.utils.CustomFunction

class VoucherAdapter : RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder>() {
    inner class VoucherViewHolder(private val binding: ItemVoucherBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(voucher: VoucherItem) {
            binding.txtValueVoucher.text = "Potongan harga ${voucher.valueVoucher} %"
            binding.txtCodeVoucher.text = voucher.codeVoucher
            binding.txtDateLimit.text = CustomFunction().isoTimeToSlashNormalDate(voucher.validity)
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<VoucherItem>() {
        override fun areItemsTheSame(oldItem: VoucherItem, newItem: VoucherItem): Boolean {
            return oldItem._id == newItem._id
        }

        override fun areContentsTheSame(oldItem: VoucherItem, newItem: VoucherItem): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoucherViewHolder =
        VoucherViewHolder(
            ItemVoucherBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: VoucherViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size
}