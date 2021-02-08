package com.kodelapo.mitra.ui.kelola.edit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kodelapo.mitra.databinding.ActivityEditProductBinding

class EditProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProductBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}