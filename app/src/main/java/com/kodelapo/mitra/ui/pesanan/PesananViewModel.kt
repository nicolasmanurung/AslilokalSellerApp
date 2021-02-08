package com.kodelapo.mitra.ui.pesanan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PesananViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is pesanan Fragment"
    }
    val text: LiveData<String> = _text
}