package com.aslilokal.mitra.ui.analitik.pencairan.pengajuan

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aslilokal.mitra.databinding.ActivityPengajuanPencairanBinding
import com.aslilokal.mitra.model.data.api.RetrofitInstance
import com.aslilokal.mitra.model.remote.response.InformationPayment
import com.aslilokal.mitra.model.remote.response.RevenueItem
import com.aslilokal.mitra.model.remote.response.Seller
import com.aslilokal.mitra.utils.AslilokalDataStore
import com.aslilokal.mitra.utils.CustomFunction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException

class PengajuanPencairanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPengajuanPencairanBinding
    private lateinit var datastore: AslilokalDataStore
    private lateinit var token: String
    private lateinit var username: String
    private var sumRevenue: Int? = null
    private var noPaymentInfo: String? = null
    private lateinit var providerPayment: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPengajuanPencairanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        datastore = AslilokalDataStore(binding.root.context)
        runBlocking {
            username = datastore.read("USERNAME").toString()
            token = datastore.read("TOKEN").toString()
        }
        sumRevenue = intent.getStringExtra("sumRevenue").toString().toInt()
        getSellerInformation()

        binding.btnConfirmRevenueOrder.setOnClickListener {
            if (binding.etSumSaldo.text.isNullOrEmpty() || noPaymentInfo == null) {
                binding.etSumSaldo.error = "Harap jumlah penarikan"
                Toast.makeText(
                    binding.root.context,
                    "Harap isi data yang lengkap",
                    Toast.LENGTH_SHORT
                ).show()
            }else{
                addRevenueOrder()
            }
        }
    }

    private fun addRevenueOrder() = CoroutineScope(Dispatchers.Main).launch {
        showProgress()
        try {
            val totalRequestRevenue =
                CustomFunction().reverseFormatRupiah(binding.etSumSaldo.text.toString())
            val newInformationPayment = InformationPayment(
                numberPayment = noPaymentInfo.toString(),
                providerPayment = providerPayment
            )
            val newRevenueItem = RevenueItem(
                _id = null,
                acceptedRevenue = totalRequestRevenue.toInt(),
                idSellerAccount = username,
                informationPayment = newInformationPayment,
                statusRevenue = "request",
                sumRevenueRequest = totalRequestRevenue.toInt(),
                acceptAt = null,
                createdAt = null
            )
            val response = RetrofitInstance.api.postRevenueRequest(token, newRevenueItem)
            if (response.isSuccessful) {
                hideProgress()
                Toast.makeText(binding.root.context, "Berhasil..", Toast.LENGTH_SHORT).show()
                onBackPressed()
            } else {
                hideProgress()
            }
        } catch (exception: Exception) {
            when (exception) {
                is IOException -> Toast.makeText(
                    binding.root.context,
                    "Jaringan lemah",
                    Toast.LENGTH_SHORT
                ).show()
                else -> {
                    Toast.makeText(
                        binding.root.context,
                        exception.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("KESALAHAN", exception.toString())
                }
            }
        }
    }

    private fun getSellerInformation() = CoroutineScope(Dispatchers.Main).launch {
        showProgress()
        try {
            val response = RetrofitInstance.api.getSellerBiodata(token, username)
            if (response.body()?.success == true) {
                hideProgress()
                response.body()?.result?.let { showData(it) }
            } else {
                Toast.makeText(
                    binding.root.context,
                    "Ada kesalahan coba lagi...",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (exception: Exception) {
            when (exception) {
                is IOException -> Toast.makeText(
                    binding.root.context,
                    "Jaringan lemah",
                    Toast.LENGTH_SHORT
                ).show()
                else -> {
                    Toast.makeText(
                        binding.root.context,
                        exception.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("KESALAHAN", exception.toString())
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showData(data: Seller) {
        Log.d("DATASELLER->", data.toString())
        binding.txtCurrentSaldo.text = CustomFunction().formatRupiah(sumRevenue!!.toDouble())
        if (data.paymentInfo.danaNumber?.isEmpty() == true) {
            binding.rbDana.visibility = View.GONE
        } else {
            binding.rbDana.text = data.paymentInfo.danaNumber
            binding.rbDana.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    noPaymentInfo = data.paymentInfo.danaNumber
                    providerPayment = "DANA"
                }
            }
        }
        if (data.paymentInfo.gopayNumber?.isNotEmpty() == true) {
            binding.rbGopay.visibility = View.GONE
        } else {
            binding.rbGopay.text = data.paymentInfo.danaNumber
            binding.rbGopay.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    noPaymentInfo = data.paymentInfo.gopayNumber
                    providerPayment = "GOPAY"
                }
            }
        }
        if (data.paymentInfo.ovoNumber?.isNotEmpty() == true) {
            binding.rbOvo.visibility = View.GONE
        } else {
            binding.rbOvo.text = data.paymentInfo.danaNumber
            binding.rbOvo.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    noPaymentInfo = data.paymentInfo.ovoNumber
                    providerPayment = "OVO"
                }
            }
        }

    }

    fun hideProgress() {
        binding.llProgressBar.progressbar.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    fun showProgress() {
        binding.llProgressBar.progressbar.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }
}