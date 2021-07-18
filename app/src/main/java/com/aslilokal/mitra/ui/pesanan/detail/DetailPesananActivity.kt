package com.aslilokal.mitra.ui.pesanan.detail

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aslilokal.mitra.R
import com.aslilokal.mitra.databinding.ActivityDetailPesananBinding
import com.aslilokal.mitra.databinding.ItemMicroPesananBinding
import com.aslilokal.mitra.model.data.api.ApiHelper
import com.aslilokal.mitra.model.data.api.RetrofitInstance
import com.aslilokal.mitra.model.remote.request.FCMBuyerRequest
import com.aslilokal.mitra.model.remote.request.Notification
import com.aslilokal.mitra.model.remote.request.PesananRequest
import com.aslilokal.mitra.model.remote.response.ProductOrder
import com.aslilokal.mitra.model.remote.response.ResultOrder
import com.aslilokal.mitra.ui.adapter.PesananProductAdapter
import com.aslilokal.mitra.utils.AslilokalDataStore
import com.aslilokal.mitra.utils.Constants
import com.aslilokal.mitra.utils.Constants.Companion.BUCKET_USR_EVIDANCE_URL
import com.aslilokal.mitra.utils.CustomFunction
import com.aslilokal.mitra.utils.Status
import com.aslilokal.mitra.viewmodel.AslilokalVMProviderFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class DetailPesananActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailPesananBinding
    private lateinit var detailViewmodel: DetailViewModel
    private lateinit var tokenKey: String
    private lateinit var idOrder: String
    private lateinit var productPesananAdapter: PesananProductAdapter

    private lateinit var aslilokalDataStore: AslilokalDataStore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPesananBinding.inflate(layoutInflater)
        setContentView(binding.root)
        aslilokalDataStore = AslilokalDataStore(binding.root.context)
        idOrder = intent.getStringExtra("idOrder").toString()
        setupRvProductsPesanan()
        setupDetailViewModel()
        CoroutineScope(Dispatchers.Main).launch {
            tokenKey = aslilokalDataStore.read("TOKEN").toString()
            setupObservers()
        }

    }

    private fun setupDetailViewModel() {
        detailViewmodel = ViewModelProvider(
            this,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(DetailViewModel::class.java)
    }

    private fun setupObservers() {
        detailViewmodel.getDetailOrder(
            tokenKey,
            idOrder
        ).observe(this, {
            it.let { resource ->
                showLoadingProgress()
                when (resource.status) {
                    Status.SUCCESS -> {
                        //hideLoading
                        hideLoadingProgress()
                        when (resource.data?.body()?.success) {
                            true -> {
                                val response = resource.data.body()
                                response?.result?.let { it1 -> showData(it1) }
                            }
                            false -> Toast.makeText(
                                this,
                                "Jaringan kamu lemah, coba ulang yah...",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    Status.ERROR -> {
                        hideLoadingProgress()
                    }

                    Status.LOADING -> {
                        showLoadingProgress()
                    }
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun showData(response: ResultOrder) {
        when (response.statusOrder) {
            "acceptrequired" -> {
                binding.txtNoteBatal.visibility = View.GONE
                binding.lnrBatal.visibility = View.GONE
                binding.btnAcceptOrder.visibility = View.VISIBLE
                binding.btnAcceptOrder.setOnClickListener {
                    showDialog(response.idBuyerAccount)
                }
            }
            "process" -> {
                binding.txtNoteBatal.visibility = View.GONE
                binding.lnrBatal.visibility = View.GONE
            }
            "delivered" -> {
                binding.txtNoteBatal.visibility = View.GONE
                binding.lnrBatal.visibility = View.GONE
            }
            "done" -> {
                binding.txtNoteBatal.visibility = View.GONE
                binding.lnrBatal.visibility = View.GONE
            }
        }
        Log.d("RESULTORDER->", response.toString())
        binding.txtNameBuyer.text = response.nameBuyer
        binding.txtAlamatBuyer.text = response.addressBuyer
        binding.txtNoTelp.text = response.numberTelp
        binding.txtDeliveryPrice.text =
            CustomFunction().formatRupiah(response.courierCost.toDouble())
        binding.txtSumPrice.text = CustomFunction().formatRupiah(response.totalPayment.toDouble())
        binding.txtTimeRemainingOrder.text =
            CustomFunction().isoTimeToAddDaysTime(response.orderAt)
        Log.d("TIMEORDER", CustomFunction().isoTimeToAddDaysTime(response.orderAt))

        binding.imgEvidancePayment.visibility = View.VISIBLE
        Glide.with(binding.root.context)
            .load(BUCKET_USR_EVIDANCE_URL + response.imgPayment)
            .placeholder(R.drawable.loading_animation)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .priority(Priority.HIGH)
            .into(binding.imgEvidancePayment)

        productPesananAdapter.differ.submitList(response.products.toList())
        setupRvMicroPembayaran(response.products)
        var tempSumTotalValue = 0
        for (item in response.products) {
            tempSumTotalValue = item.priceAt * item.qty
        }
        if (response.voucherId.isNotEmpty()) {
            binding.lnrVoucherApply.visibility = View.VISIBLE
            var tempVoucherActualPrice =
                tempSumTotalValue - (response.totalPayment - response.courierCost)
            binding.txtVoucherActualRupiah.text =
                "-" + CustomFunction().formatRupiah(tempVoucherActualPrice.toDouble())
        }

        if (response.courierType != "CUSTOM") {
            binding.txtNameCourier.text = response.courierType
            binding.txtNameExpedition.text = response.courierType
            binding.txtNoResi.text = response.resiCode
        } else {
            binding.txtNameExpedition.text = "Penjual"
            binding.txtNoResi.visibility = View.GONE
        }
    }

    private fun showDialog(idBuyer: String) {
        val builder = AlertDialog.Builder(binding.root.context)
        builder.setTitle("Terima pesanan?")
        builder.setMessage("Harap langsung mengemas setelah menerima pesanan...")
        builder.setPositiveButton(
            "Terima Pesanan"
        ) { dialog, id ->
            setupPutStatus(idBuyer)
        }

//        builder.setNegativeButton(
//            "Tolak Pesanan"
//        ) { dialog, id ->
//
//        }

        builder.show()
    }

    private fun setupPutStatus(idBuyer: String) = CoroutineScope(Dispatchers.Main).launch {
        showLoadingProgress()
        try {
            val status = "process"
            val pesananRequest = PesananRequest(
                idBuyer,
                status
            )
            val response = RetrofitInstance.api.putStatusOneOrder(tokenKey, idOrder, pesananRequest)
            if (response.isSuccessful) {
                hideLoadingProgress()
                // Firebase Notification to Buyer
                val notificationProcess = FCMBuyerRequest(
                    Notification(
                        "Lihat, status pesanan kamu berubah...",
                        "Perubahan Status Pesanan!"
                    ),
                    "/topics/notification-$idBuyer"
                )
                sendNotificationToBuyer(notificationProcess)
            } else {
                hideLoadingProgress()
                Toast.makeText(
                    binding.root.context,
                    "Maaf jaringanmu lemah, coba ulangi...",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (exception: Exception) {
            hideLoadingProgress()
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
                    Log.d("Ada Kesalahan", exception.toString())
                }
            }
        }
    }

    private fun sendNotificationToBuyer(notification: FCMBuyerRequest) =
        CoroutineScope(Dispatchers.Main).launch {
            showLoadingProgress()
            try {
                val response = RetrofitInstance.apiFirebase.postBuyerNotificationOrderFirebase(
                    "key=${Constants.FIREBASE_SERVER_KEY_BUYER}",
                    notification
                )
                if (response.isSuccessful) {
                    hideLoadingProgress()
                } else {
                    hideLoadingProgress()
                }
            } catch (exception: Exception) {
                hideLoadingProgress()
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
                        Log.d("Ada Kesalahan", exception.toString())
                    }
                }
            }
        }

    private fun setupRvProductsPesanan() {
        productPesananAdapter = PesananProductAdapter()
        binding.rvProductPesanan.apply {
            adapter = productPesananAdapter
            layoutManager = LinearLayoutManager(binding.root.context)
        }
    }

    private fun setupRvMicroPembayaran(listProducts: ArrayList<ProductOrder>) {
        val itemCartAdapter = MicroPembayaranAdapter(listProducts)
        binding.rvMicroPesanan.apply {
            adapter = itemCartAdapter
            layoutManager = LinearLayoutManager(binding.root.context)
        }
    }

    private fun showLoadingProgress() {
        binding.llProgressBar.progressbar.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    private fun hideLoadingProgress() {
        binding.llProgressBar.progressbar.visibility = View.INVISIBLE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    class MicroPembayaranAdapter(private val listItem: ArrayList<ProductOrder>) :
        RecyclerView.Adapter<MicroPembayaranAdapter.MicroPembayaranViewHolder>() {
        inner class MicroPembayaranViewHolder(private val binding: ItemMicroPesananBinding) :
            RecyclerView.ViewHolder(binding.root) {
            @SuppressLint("SetTextI18n")
            fun bind(itemProduct: ProductOrder) {
                binding.txtNameProduct.text = itemProduct.nameProduct
                binding.txtSumProduct.text = itemProduct.qty.toString() + "X"
                binding.txtPriceProduct.text =
                    CustomFunction().formatRupiah(itemProduct.priceAt.toDouble())
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): MicroPembayaranViewHolder = MicroPembayaranViewHolder(
            ItemMicroPesananBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

        override fun onBindViewHolder(holder: MicroPembayaranViewHolder, position: Int) {
            holder.bind(listItem[position])
        }

        override fun getItemCount(): Int = listItem.size
    }
}