package com.aslilokal.mitra.ui.debtor.uncomplete

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aslilokal.mitra.databinding.FragmentUncompleteDebtorBinding
import com.aslilokal.mitra.model.data.api.ApiHelper
import com.aslilokal.mitra.model.data.api.RetrofitInstance
import com.aslilokal.mitra.model.remote.response.DebtorItem
import com.aslilokal.mitra.ui.adapter.DebtorAdapter
import com.aslilokal.mitra.ui.debtor.DebtorViewModel
import com.aslilokal.mitra.ui.debtor.list.ListAllDebtorActivity
import com.aslilokal.mitra.utils.ResourcePagination
import com.aslilokal.mitra.viewmodel.KodelapoViewModelProviderFactory
import kotlinx.coroutines.runBlocking


class UncompleteDebtorFragment() : Fragment() {

    private var _binding: FragmentUncompleteDebtorBinding? = null
    private val binding get() = _binding!!
    private lateinit var debtorAdapter: DebtorAdapter
    private lateinit var viewModel: DebtorViewModel

    private lateinit var listDebtorActivity: ListAllDebtorActivity
    private var uncompleteData = ArrayList<DebtorItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = activity?.run {
            ViewModelProvider(
                viewModelStore,
                KodelapoViewModelProviderFactory(ApiHelper(RetrofitInstance.api))
            ).get(DebtorViewModel::class.java)
        }!!

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUncompleteDebtorBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        listDebtorActivity = activity as ListAllDebtorActivity
        setupRecyclerView()
        return binding.root
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
        var listAllDebt = debtorList
        if (listAllDebt != null) {
            var uncomplete = listAllDebt.filter { !it.statusTransaction }
            if (uncomplete.isNotEmpty()) {
                uncomplete.forEach { debtorItem ->
                    uncompleteData.add(debtorItem)
                }
                debtorAdapter.differ.submitList(uncompleteData)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getData()
    }


    private fun setupRecyclerView() {
        debtorAdapter = DebtorAdapter()
        binding.rvUncompleteDebtor.apply {
            adapter = debtorAdapter
            layoutManager = LinearLayoutManager(binding.root.context)
        }
    }
}