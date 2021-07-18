package com.aslilokal.mitra.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aslilokal.mitra.databinding.ItemDebtorBinding
import com.aslilokal.mitra.model.remote.response.DebtorItem
import com.aslilokal.mitra.ui.debtor.detail.DetailDebtorActivity
import com.aslilokal.mitra.utils.CustomFunction

class DebtorAdapter : RecyclerView.Adapter<DebtorAdapter.DebtorViewHolder>() {
    inner class DebtorViewHolder(private val binding: ItemDebtorBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(debtor: DebtorItem) {
            binding.txtNameDebtor.text = debtor.nameDebtor
            binding.txtSumDebt.text = CustomFunction().formatRupiah(debtor.totalDebt.toDouble())
            binding.txtDateCreated.text = CustomFunction().isoTimeToDateMonth(debtor.createAt!!)

            itemView.setOnClickListener {
                var intent = Intent(binding.root.context, DetailDebtorActivity::class.java)
                intent.putExtra("debtor", debtor)
                binding.root.context.startActivity(intent)
            }
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<DebtorItem>() {
        override fun areItemsTheSame(oldItem: DebtorItem, newItem: DebtorItem): Boolean {
            return oldItem._id == newItem._id
        }

        override fun areContentsTheSame(oldItem: DebtorItem, newItem: DebtorItem): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DebtorViewHolder =
        DebtorViewHolder(
            ItemDebtorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: DebtorViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size
}