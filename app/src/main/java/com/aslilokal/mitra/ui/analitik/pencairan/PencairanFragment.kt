package com.aslilokal.mitra.ui.analitik.pencairan

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.aslilokal.mitra.databinding.FragmentPencairanBinding
import com.aslilokal.mitra.model.data.api.ApiHelper
import com.aslilokal.mitra.model.data.api.RetrofitInstance
import com.aslilokal.mitra.model.remote.response.RevenueItem
import com.aslilokal.mitra.ui.adapter.PencairanAdapter
import com.aslilokal.mitra.ui.analitik.AnalitikViewModel
import com.aslilokal.mitra.ui.analitik.pencairan.pengajuan.PengajuanPencairanActivity
import com.aslilokal.mitra.utils.AslilokalDataStore
import com.aslilokal.mitra.utils.CustomFunction
import com.aslilokal.mitra.utils.ResourcePagination
import com.aslilokal.mitra.utils.Status
import com.aslilokal.mitra.viewmodel.AslilokalVMProviderFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class PencairanFragment : Fragment() {
    private var _binding: FragmentPencairanBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AnalitikViewModel
    private lateinit var datastore: AslilokalDataStore
    private lateinit var pencairanAdapter: PencairanAdapter

    private lateinit var username: String
    private lateinit var token: String
    private var sumRevenue: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPencairanBinding.inflate(inflater, container, false)
        datastore = AslilokalDataStore(binding.root.context)
        showLoadingList()

        setupViewModel()
        setupRecycler()

        runBlocking {
            username = datastore.read("USERNAME").toString()
            token = datastore.read("TOKEN").toString()
        }

        getListRiwayatData()
        setupGetTotalRevenue()

        binding.swipeRefresh.setOnRefreshListener {
            showLoadingList()
            getListRiwayatData()
        }

        // Inflate the layout for this fragment
        return binding.root
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            viewModelStore,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(AnalitikViewModel::class.java)
    }

    private fun setupRecycler() {
        pencairanAdapter = PencairanAdapter()
        binding.rvPencairan.apply {
            adapter = pencairanAdapter
            layoutManager = LinearLayoutManager(binding.root.context)
        }
    }

    private fun getListRiwayatData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getAllRevenueRequest(
                token,
                username
            )
            setupListRiwayatPencairan()
        }
    }

    private fun setupListRiwayatPencairan() {
        viewModel.revenueRequest.observe(viewLifecycleOwner, { response ->
            when (response) {
                is ResourcePagination.Success -> {
                    hideLoadingList()
                    response.data?.result.let { revenueResponse ->
                        pencairanAdapter.differ.submitList(revenueResponse?.toList())
                        if (revenueResponse != null) {
                            if (revenueResponse.toList().isEmpty()) {
                                Toast.makeText(
                                    activity,
                                    "Sepertinya datanya masih kosong...",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                getSumValue(revenueResponse)
                            }
                        }
                    }
                }

                is ResourcePagination.Error -> {
                    hideLoadingList()
                    response.message?.let { message ->
                        Toast.makeText(
                            activity,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                is ResourcePagination.Loading -> {
                    showLoadingList()
                }
            }
        })
    }

    private fun setupGetTotalRevenue() {
        viewModel.getTotalRevenue(token, username).observe(viewLifecycleOwner, {
            it.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        resource.data?.body().let { response ->
                            if (response?.result == null) {
                                binding.txtSumSaldo.text = "Rp 0"
                            } else {
                                binding.txtSumSaldo.text =
                                    CustomFunction().formatRupiah(response.result.sumSaldo.toDouble())
                                binding.rlPencairan.setOnClickListener {
                                    sumRevenue = response.result.sumSaldo.toInt()

                                    val intent = Intent(
                                        binding.root.context,
                                        PengajuanPencairanActivity::class.java
                                    )
                                    intent.putExtra("sumRevenue", sumRevenue.toString())
                                    binding.root.context.startActivity(intent)
                                }
                            }
                        }
                    }
                }
            }
        })
    }

    private fun getSumValue(revenueResponse: ArrayList<RevenueItem>) {
        var sumTotalDiCairkan = 0
        for (item in revenueResponse) {
            when (item.statusRevenue) {
                "done" -> {
                    sumTotalDiCairkan += item.sumRevenueRequest
                }
            }
        }
        binding.txtTotalSuccessPencairan.text =
            CustomFunction().formatRupiah(sumTotalDiCairkan.toDouble())
    }


    private fun showLoadingList() {
        binding.swipeRefresh.isRefreshing = true
        binding.saldoSkeletonLayout.showShimmer(true)
        binding.saldoSkeletonLayout.startShimmer()
        binding.rvSkeletonLayout.showShimmerAdapter()
        binding.rvPencairan.visibility = View.GONE
    }

    private fun hideLoadingList() {
        binding.swipeRefresh.isRefreshing = false
        binding.saldoSkeletonLayout.stopShimmer()
        binding.saldoSkeletonLayout.hideShimmer()
        binding.rvSkeletonLayout.hideShimmerAdapter()
        binding.rvPencairan.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}