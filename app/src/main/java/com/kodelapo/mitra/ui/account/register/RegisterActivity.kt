package com.kodelapo.mitra.ui.account.register

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kodelapo.mitra.databinding.ActivityRegisterBinding
import com.kodelapo.mitra.ui.account.login.LoginActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lnrLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}