package com.kodelapo.mitra.ui.notifications

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kodelapo.mitra.databinding.ActivityNotificationBinding

class NotificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotificationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}