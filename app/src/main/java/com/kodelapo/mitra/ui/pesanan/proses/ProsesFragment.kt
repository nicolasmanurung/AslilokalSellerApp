package com.kodelapo.mitra.ui.pesanan.proses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.kodelapo.mitra.databinding.FragmentProsesBinding
import com.kodelapo.mitra.model.data.api.ApiHelper
import com.kodelapo.mitra.model.data.api.RetrofitInstance
import com.kodelapo.mitra.model.remote.response.OrderResponse
import com.kodelapo.mitra.ui.adapter.PesananAdapter
import com.kodelapo.mitra.ui.pesanan.PesananViewModel
import com.kodelapo.mitra.utils.KodelapoDataStore
import com.kodelapo.mitra.utils.ResourcePagination
import com.kodelapo.mitra.viewmodel.KodelapoViewModelProviderFactory
import kotlinx.coroutines.launch

class ProsesFragment : Fragment() {

    private var _binding: FragmentProsesBinding? = null
    private val binding get() = _binding!!
    private lateinit var dataStore: KodelapoDataStore
    private lateinit var viewModel: PesananViewModel
    private lateinit var orderAdapter: PesananAdapter
    private var list = ArrayList<OrderResponse>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProsesBinding.inflate(inflater, container, false)

        hideEmpty()
        setupViewModel()
        setupRecycler()

        dataStore = KodelapoDataStore(binding.root.context)

        getData()

        binding.swipeRefresh.setOnRefreshListener {
            getData()
        }

        return binding.root
    }

    private fun getData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val username = dataStore.read("USERNAME").toString()
            val token = dataStore.read("TOKEN").toString()
            viewModel.getPesanan(token, username, "process")
            setupObserver()
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            viewModelStore,
            KodelapoViewModelProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(PesananViewModel::class.java)
    }

    private fun setupRecycler() {
        orderAdapter = PesananAdapter()
        binding.rvDiproses.apply {
            adapter = orderAdapter
            layoutManager = LinearLayoutManager(binding.root.context)
        }
    }

    private fun setupObserver() {
        viewModel.orders.observe(viewLifecycleOwner, { response ->
            when (response) {
                is ResourcePagination.Success -> {
                    hideProgressBar()
                    response.data?.result.let { orderResponse ->
                        orderAdapter.differ.submitList(orderResponse?.toList())
                        if (orderResponse != null) {
                            if (orderResponse.toList().isEmpty()) {
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