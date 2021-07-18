package com.aslilokal.mitra.ui.debtor.complete

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aslilokal.mitra.databinding.FragmentCompleteDebtorBinding
import com.aslilokal.mitra.model.data.api.ApiHelper
import com.aslilokal.mitra.model.data.api.RetrofitInstance
import com.aslilokal.mitra.model.remote.response.DebtorItem
import com.aslilokal.mitra.ui.adapter.DebtorAdapter
import com.aslilokal.mitra.ui.debtor.DebtorViewModel
import com.aslilokal.mitra.ui.debtor.list.ListAllDebtorActivity
import com.aslilokal.mitra.utils.ResourcePagination
import com.aslilokal.mitra.viewmodel.AslilokalVMProviderFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class CompleteDebtorFragment() : Fragment() {
    private var _binding: FragmentCompleteDebtorBinding? = null
    private val binding get() = _binding!!
    private lateinit var debtorAdapter: DebtorAdapter
    private lateinit var viewModel: DebtorViewModel

    private var completeData = ArrayList<DebtorItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompleteDebtorBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        setupViewModel()
        setupRecyclerView()

        val activity: ListAllDebtorActivity = activity as ListAllDebtorActivity
        GlobalScope.launch(Dispatchers.Main) {
            viewModel.getDebtor(
                activity.getTokenUser(),
                activity.getUsernameUser(),
                year = "2018",
                month = "1"
            )
            getData()
        }

        return binding.root
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            viewModelStore,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(DebtorViewModel::class.java)
    }


    private fun getData() {
        viewModel.debtors.observe(viewLifecycleOwner, { response ->
            when (response) {
                is ResourcePagination.Success -> {
                    response.data?.result.let { response ->
                        runBlocking {
                            separateData(response)
                        }
                    }
                }
            }
        })
    }

    private fun separateData(debtorList: ArrayList<DebtorItem>?) {
        if (debtorList != null) {
            Log.d("LISTALLDEBT", debtorList.toString())
            debtorList.forEach { item ->
                Log.d("FOREACHCOMPLETE", item.toString())
                if (item.statusTransaction) {
                    completeData.add(item)
                }
            }
            Log.d("COMPLETEDATA", completeData.size.toString())
            debtorAdapter.differ.submitList(completeData.toList())
        }
    }

    private fun setupRecyclerView() {
        debtorAdapter = DebtorAdapter()
        binding.rvCompleteDebtor.apply {
            adapter = debtorAdapter
            layoutManager = LinearLayoutManager(binding.root.context)
        }
    }
}