package com.aslilokal.mitra.ui.pesanan.bayar

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.aslilokal.mitra.databinding.FragmentBayarBinding
import com.aslilokal.mitra.model.data.api.ApiHelper
import com.aslilokal.mitra.model.data.api.RetrofitInstance
import com.aslilokal.mitra.model.remote.request.FCMBuyerRequest
import com.aslilokal.mitra.model.remote.request.Notification
import com.aslilokal.mitra.model.remote.request.PesananRequest
import com.aslilokal.mitra.ui.adapter.PesananAdapter
import com.aslilokal.mitra.ui.pesanan.PesananViewModel
import com.aslilokal.mitra.utils.AslilokalDataStore
import com.aslilokal.mitra.utils.Constants
import com.aslilokal.mitra.utils.ResourcePagination
import com.aslilokal.mitra.viewmodel.AslilokalVMProviderFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class BayarFragment : Fragment() {
    //    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private var _binding: FragmentBayarBinding? = null
    private val binding get() = _binding!!
    private lateinit var datastore: AslilokalDataStore
    private lateinit var viewModel: PesananViewModel
    private lateinit var orderAdapter: PesananAdapter
    private lateinit var username: String
    private lateinit var token: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBayarBinding.inflate(inflater, container, false)
        datastore = AslilokalDataStore(binding.root.context)

        hideEmpty()
        setupViewModel()
        setupRecycler()


        getData()

        binding.swipeRefresh.setOnRefreshListener {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.getPesanan(token, username, "acceptrequired")
            }
            setupObserver()
        }

        orderAdapter.onItemClick = { order ->
            showDialog(order._id, order.idBuyerAccount)
        }

        return binding.root
    }

    private fun getData() {
        viewLifecycleOwner.lifecycleScope.launch {
            username = datastore.read("USERNAME").toString()
            token = datastore.read("TOKEN").toString()
            viewModel.getPesanan(token, username, "acceptrequired")
            setupObserver()
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            viewModelStore,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(PesananViewModel::class.java)
    }

    private fun setupRecycler() {
        orderAdapter = PesananAdapter()
        binding.rvBayar.apply {
            adapter = orderAdapter
            layoutManager = LinearLayoutManager(binding.root.context)
        }
    }

    private fun showDialog(idOrder: String, idBuyer: String) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Terima pesanan?")
        builder.setMessage("Harap langsung mengemas setelah menerima pesanan...")
        builder.setPositiveButton(
            "Terima Pesanan"
        ) { dialog, id ->
            changeStatus(idOrder, idBuyer)
        }

//        builder.setNegativeButton(
//            "Tolak Pesanan"
//        ) { dialog, id ->
//
//        }

        builder.show()
    }

    private fun changeStatus(idOrder: String, idBuyer: String) {
        val status = "process"
        val pesananRequest = PesananRequest(
            idBuyer,
            status
        )
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.editStatusPesanan(token, idOrder, pesananRequest)
            setupPutStatus(idBuyer)
        }
    }

    private fun setupObserver() {
        viewModel.orders.observe(viewLifecycleOwner, { response ->
            when (response) {
                is ResourcePagination.Success -> {
                    hideProgressBar()
                    response.data?.result.let { orderResponse ->
                        orderAdapter.differ.submitList(orderResponse?.toList())
                        Log.d("RESULT", "Counting")
                        if (orderResponse != null) {
                            Log.d("ISNOTNULL", "Counting")
                            if (orderResponse.toList().isEmpty()) {
                                Log.d("EMPTY", "Counting")
                                showEmpty()
                            } else {
                                hideEmpty()
                            }
                        }
                    }
                }
                is ResourcePagination.Error -> {
                    hideProgressBar()
                    hideEmpty()
                    response.message?.let { message ->
                        Toast.makeText(
                            activity,
                            "Jaringanmu lemah, coba refresh...",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                is ResourcePagination.Loading -> {
                    showProgressBar()
                    hideEmpty()
                }
            }
        })
    }

    private fun setupPutStatus(idBuyer: String) {
        viewModel.orderStatusResponse.observe(viewLifecycleOwner, { response ->
            when (response) {
                is ResourcePagination.Success -> {
                    getData()
                    // Firebase Notification to Buyer
                    var notificationProcess = FCMBuyerRequest(
                        Notification(
                            "Lihat, status pesanan kamu berubah...",
                            "Perubahan Status Pesanan!"
                        ),
                        "/topics/notification-$idBuyer"
                    )
                    sendNotificationToBuyer(notificationProcess)
                    Toast.makeText(activity, "Status di ubah menjadi diproses", Toast.LENGTH_LONG)
                        .show()
                }
                is ResourcePagination.Error -> {
                    hideProgressBar()
                    Toast.makeText(
                        activity,
                        "Maaf jaringanmu lemah, coba ulangi...",
                        Toast.LENGTH_LONG
                    ).show()
                }
                is ResourcePagination.Loading -> {
                    showProgressBar()
                }
            }
        })
    }

    private fun sendNotificationToBuyer(notification: FCMBuyerRequest) =
        CoroutineScope(Dispatchers.Main).launch {
            showProgressBar()
            try {
                val response = RetrofitInstance.apiFirebase.postBuyerNotificationOrderFirebase(
                    "key=${Constants.FIREBASE_SERVER_KEY_BUYER}",
                    notification
                )
            } catch (exception: Exception) {
                hideProgressBar()
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


    private fun showProgressBar() {
        binding.swipeRefresh.isRefreshing = true
    }

    private fun hideProgressBar() {
        binding.swipeRefresh.isRefreshing = false
    }

    private fun showEmpty() {
        binding.lnrEmpty.visibility = View.VISIBLE
    }

    private fun hideEmpty() {
        binding.lnrEmpty.visibility = View.GONE
    }
}