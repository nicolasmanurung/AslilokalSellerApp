package com.aslilokal.mitra.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aslilokal.mitra.databinding.ItemLatestOrderBinding
import com.aslilokal.mitra.model.remote.response.ResultOrder
import com.aslilokal.mitra.utils.CustomFunction

class LatestOrderAdapter : RecyclerView.Adapter<LatestOrderAdapter.LatestViewHolder>() {
    inner class LatestViewHolder(private val binding: ItemLatestOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(order: ResultOrder) {
            binding.txtNameBuyer.text = order.nameBuyer
            //binding.txtStatusOrder.text = order.statusOrder
            binding.txtStatusOrder.text = "Masuk"
            binding.txtValueMoney.text =
                CustomFunction().formatRupiah(order.totalPayment.toDouble())
            binding.txtDateFinish.text = CustomFunction().isoTimeToDateMonth(order.orderAt)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LatestViewHolder =
        LatestViewHolder(
            ItemLatestOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: LatestViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size
}