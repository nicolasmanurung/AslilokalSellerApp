package com.aslilokal.mitra.ui.pesanan.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aslilokal.mitra.databinding.ActivityDetailPesananBinding

class DetailPesananActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailPesananBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPesananBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}