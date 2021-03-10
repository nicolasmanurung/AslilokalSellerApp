package com.aslilokal.mitra.ui.analitik.voucher

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.aslilokal.mitra.databinding.FragmentVoucherBinding
import com.aslilokal.mitra.model.data.api.ApiHelper
import com.aslilokal.mitra.model.data.api.RetrofitInstance
import com.aslilokal.mitra.model.remote.response.VoucherItem
import com.aslilokal.mitra.ui.adapter.VoucherAdapter
import com.aslilokal.mitra.ui.analitik.AnalitikViewModel
import com.aslilokal.mitra.utils.CustomFunction
import com.aslilokal.mitra.utils.KodelapoDataStore
import com.aslilokal.mitra.utils.ResourcePagination
import com.aslilokal.mitra.utils.Status
import com.aslilokal.mitra.viewmodel.KodelapoViewModelProviderFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*


class VoucherFragment : Fragment() {
    private var _binding: FragmentVoucherBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AnalitikViewModel
    private lateinit var datastore: KodelapoDataStore
    private lateinit var username: String
    private lateinit var token: String
    private lateinit var voucherAdapter: VoucherAdapter
    private lateinit var finalDateVoucher: String
    private lateinit var oneVoucher: VoucherItem
//    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVoucherBinding.inflate(inflater, container, false)
        datastore = KodelapoDataStore(binding.root.context)

        showLoadingList()

        runBlocking {
            username = datastore.read("USERNAME").toString()
            token = datastore.read("TOKEN").toString()
        }

        setupViewModel()
        setupRecycler()

        getListVouchersData()

        binding.swipeRefresh.setOnRefreshListener {
            showLoadingList()
            getListVouchersData()
        }

        binding.btnDatePickup.setOnClickListener {
            getDate()
        }

        binding.etSumVoucherPercent.inputType = InputType.TYPE_CLASS_NUMBER or
                InputType.TYPE_NUMBER_FLAG_DECIMAL or
                InputType.TYPE_NUMBER_FLAG_SIGNED

        binding.etCodeVoucher.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                var code: String = s.toString().replace(" ", "")
                if (!code.equals(code.toUpperCase())) {
                    code = code.toUpperCase()
                }
                if (s.toString() != code) {
                    binding.etCodeVoucher.setText(code)
                    binding.etCodeVoucher.setSelection(code.length)
                }

            }

        })

        binding.btnSubmitVoucher.setOnClickListener {
            if (checkIsEmpty()) {
                //Toast.makeText(activity, finalDateVoucher, Toast.LENGTH_SHORT).show()
                // Do post
                oneVoucher = VoucherItem(
                    null,
                    null,
                    binding.etCodeVoucher.text.toString(),
                    null,
                    username,
                    null,
                    finalDateVoucher,
                    binding.etSumVoucherPercent.text.toString().toInt()
                )
                showLoadingList()
                setupPostVoucher()
            }
        }
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            viewModelStore,
            KodelapoViewModelProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(AnalitikViewModel::class.java)
    }


    private fun setupRecycler() {
        voucherAdapter = VoucherAdapter()
        binding.rvAllVoucher.apply {
            adapter = voucherAdapter
            layoutManager = LinearLayoutManager(binding.root.context)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getDate() {
        val callendar = Calendar.getInstance()
        val year = callendar.get(Calendar.YEAR)
        val month = callendar.get(Calendar.MONTH)
        val day = callendar.get(Calendar.DAY_OF_MONTH)

        val date = DatePickerDialog(
            binding.root.context,
            { view, year, month, dayOfMonth ->
                val finalMonth = month + 1
                val finalDay = dayOfMonth + 1
                finalDateVoucher =
                    CustomFunction().normalDateToIsoTime("$finalDay/$finalMonth/$year")
                binding.txtDateConvert.text = "$dayOfMonth/$finalMonth/$year"
            },
            year,
            month,
            day
        )
        date.show()
    }

    private fun checkIsEmpty(): Boolean {
        var dateVoucher = binding.txtDateConvert.text

        if (binding.etCodeVoucher.text.toString().isEmpty()) {
            binding.etCodeVoucher.error = "Harap di isi"
            return false
        }
        if (binding.etSumVoucherPercent.text.toString().isEmpty()) {
            binding.etSumVoucherPercent.error = "Harap di isi"
            return false
        }
        if (dateVoucher.isEmpty()) {
            binding.btnDatePickup.error = "Pilih tanggal"
            return false
        }

        if (binding.etSumVoucherPercent.text.toString().toInt() > 100) {
            binding.etSumVoucherPercent.error = "Tidak lebih dari 100%"
            return false
        }

        return true
    }

    private fun getListVouchersData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getAllVouchers(token, username)
            setupListVoucher()
        }
    }

    private fun setupListVoucher() {
        viewModel.vouchersRequest.observe(viewLifecycleOwner, { response ->
            when (response) {
                is ResourcePagination.Success -> {
                    hideLoadingList()
                    response.data?.result.let { voucherResponse ->
                        voucherAdapter.differ.submitList(voucherResponse?.toList())
                        if (voucherResponse != null) {
                            if (voucherResponse.toList().isEmpty()) {
                                Toast.makeText(
                                    activity,
                                    "Sepertinya datanya masih kosong...",
                                    Toast.LENGTH_SHORT
                                ).show()
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

    private fun setupPostVoucher() {
        viewModel.postOneVoucher(
            token, oneVoucher
        ).observe(viewLifecycleOwner, {
            it.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        when (resource.data?.body()?.success) {
                            true -> {
                                getListVouchersData()
                                binding.etSumVoucherPercent.setText("")
                                binding.etCodeVoucher.setText("")
                                binding.txtDateConvert.text = ""
                            }
                            false -> {
                                hideLoadingList()
                            }
                        }
                    }

                    Status.ERROR -> {
                        hideLoadingList()
                        Toast.makeText(binding.root.context, resource.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        })
    }

    private fun showLoadingList() {
        binding.swipeRefresh.isRefreshing = true
        binding.rvSkeletonLayout.showShimmerAdapter()
        binding.rvAllVoucher.visibility = View.GONE
    }

    private fun hideLoadingList() {
        binding.swipeRefresh.isRefreshing = false
        binding.rvSkeletonLayout.hideShimmerAdapter()
        binding.rvAllVoucher.visibility = View.VISIBLE
    }

}