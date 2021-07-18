package com.aslilokal.mitra.ui.notifications

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.aslilokal.mitra.databinding.ActivityNotificationBinding
import com.aslilokal.mitra.model.data.api.ApiHelper
import com.aslilokal.mitra.model.data.api.RetrofitInstance
import com.aslilokal.mitra.ui.adapter.NotificationAdapter
import com.aslilokal.mitra.utils.AslilokalDataStore
import com.aslilokal.mitra.utils.ResourcePagination
import com.aslilokal.mitra.viewmodel.AslilokalVMProviderFactory
import kotlinx.coroutines.launch

class NotificationActivity : AppCompatActivity() {
    //    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private lateinit var binding: ActivityNotificationBinding
    private lateinit var datastore : AslilokalDataStore
    private lateinit var viewModel: NotificationViewModel
    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        datastore = AslilokalDataStore(binding.root.context)
        hideEmpty()
        setupViewModel()
        setupRecycler()

        getData()

        binding.swipeRefresh.setOnRefreshListener {
            getData()
        }

        setContentView(binding.root)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(NotificationViewModel::class.java)
    }

    private fun getData() {
        lifecycleScope.launch {
            val token = datastore.read("TOKEN").toString()
            val idUser = datastore.read("USERNAME").toString()
            viewModel.getNotifications(token, idUser)
            setupObserver()
        }
    }

    private fun setupRecycler() {
        notificationAdapter = NotificationAdapter()
        binding.rvNotification.apply {
            adapter = notificationAdapter
            layoutManager = LinearLayoutManager(binding.root.context)
        }
    }

    private fun setupObserver() {
        viewModel.notifications.observe(this, { response ->
            when (response) {
                is ResourcePagination.Success -> {
                    hideProgressBar()
                    response.data?.result.let { notificationResponse ->
                        notificationAdapter.differ.submitList(notificationResponse?.toList())
                        if (notificationResponse != null) {
                            if (notificationResponse.toList().isEmpty()) {
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
                            this,
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