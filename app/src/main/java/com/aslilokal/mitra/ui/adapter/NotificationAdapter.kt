package com.aslilokal.mitra.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aslilokal.mitra.databinding.ItemNotificationBinding
import com.aslilokal.mitra.model.remote.response.Notification
import com.aslilokal.mitra.model.remote.response.NotificationResponse
import com.aslilokal.mitra.utils.CustomFunction

class NotificationAdapter : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    var onItemClick: ((NotificationResponse) -> Unit)? = null

    inner class NotificationViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(notification: Notification) {
            binding.dateNotification.text =
                CustomFunction().isoTimeToDateMonth(notification.createdAt)
            when (notification.statusNotification) {
                "paymentrequired" -> {
                    binding.txtTitleNotification.text = "Pesanan diterima"
                    binding.txtMessageNotification.text =
                        "Ada pesanan nih. Pastikan pembayarannya yuk..."
                }
                "cancel" -> {
                    binding.txtTitleNotification.text = "Cancel pesanan"
                    binding.txtMessageNotification.text = "Ada pesanan yang di cancel nih..."
                }
                "review" -> {
                    binding.txtTitleNotification.text = "Pesanan di review"
                    binding.txtTitleNotification.text = "Lihat, ada yang mereview pesanannya..."
                }
            }
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<Notification>() {
        override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem._id == newItem._id
        }

        override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder =
        NotificationViewHolder(
            ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size
}