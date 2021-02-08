package com.kodelapo.mitra.ui.kelola.fashion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kodelapo.mitra.databinding.FragmentFashionBinding
import com.kodelapo.mitra.model.data.api.ApiHelper
import com.kodelapo.mitra.model.data.api.RetrofitInstance
import com.kodelapo.mitra.ui.adapter.ProductAdapter
import com.kodelapo.mitra.ui.kelola.KelolaProdukViewModel
import com.kodelapo.mitra.utils.Constants
import com.kodelapo.mitra.utils.KodelapoDataStore
import com.kodelapo.mitra.utils.ResourcePagination
import com.kodelapo.mitra.viewmodel.KodelapoViewModelProviderFactory
import kotlinx.coroutines.launch

class FashionFragment : Fragment() {

    private var _binding: FragmentFashionBinding? = null
    private val binding get() = _binding!!

    lateinit var viewModel: KelolaProdukViewModel
    lateinit var productAdapter: ProductAdapter

    private lateinit var dataStore: KodelapoDataStore
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFashionBinding.inflate(inflater, container, false)

        dataStore = KodelapoDataStore(binding.root.context)
        setupViewModel()

        viewLifecycleOwner.lifecycleScope.launch {
            val username = dataStore.read("USERNAME").toString()
            val token = dataStore.read("TOKEN").toString()
            viewModel.getProducts(token, username, "fashion")
        }

        setupRecyclerView()
        setupObserver()
        // Next Behavior
        //showSwipeProgress()


        // Inflate the layout for this fragment
        return binding.root
    }


    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            viewModelStore,
            KodelapoViewModelProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(KelolaProdukViewModel::class.java)
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter()
        binding.rvFashion.apply {
            adapter = productAdapter
            layoutManager = GridLayoutManager(binding.root.context, 2)
            addOnScrollListener(this@FashionFragment.scrollListener)
        }
    }

    private fun setupObserver() {
        viewModel.products.observe(viewLifecycleOwner, { response ->
            when (response) {
                is ResourcePagination.Success -> {
                    hideProgressBar()
                    hideErrorMessage()
                    hideSwipeProgress()
                    response.data?.result.let { productResponse ->
                        var product =
                            productAdapter.differ.submitList(productResponse?.docs?.toList())

                        if (productResponse?.docs?.toList()?.size!! <= 0) {
                            binding.lnrEmpty.visibility = View.VISIBLE
                        } else {
                            binding.lnrEmpty.visibility = View.GONE
                        }
                        val totalPages = productResponse.totalPages / Constants.QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.productPage == totalPages
                        if (isLastPage) {
                            binding.rvFashion.setPadding(0, 0, 0, 0)
                        }
                    }
                }
                is ResourcePagination.Error -> {
                    hideProgressBar()
                    hideSwipeProgress()
                    response.message?.let { message ->
                        Toast.makeText(activity, "An error occured: $message", Toast.LENGTH_SHORT)
                            .show()
                        showErrorMessage(message)
                    }
                }
                is ResourcePagination.Loading -> {
                    showProgressBar()
                    hideSwipeProgress()
                }
            }
        })

        binding.itemErrorMessage.btnRetry.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val username = dataStore.read("USERNAME").toString()
                val token = dataStore.read("TOKEN").toString()
                viewModel.getProducts(token, username, "fashion")
            }
        }
    }

    var isError = false
    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as GridLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNoErrors = !isError
            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_PAGE_SIZE
            val shouldPaginate =
                isNoErrors && isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning &&
                        isTotalMoreThanVisible && isScrolling
            if (shouldPaginate) {
                var username: String
                var token: String
                viewLifecycleOwner.lifecycleScope.launch {
                    username = dataStore.read("USERNAME").toString()
                    token = dataStore.read("TOKEN").toString()

                    viewModel.getProducts(token, username, "fashion")
                    isScrolling = false
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun hideSwipeProgress() {
        binding.swipeRefresh.isRefreshing = false
    }

    private fun showSwipeProgress() {
        binding.swipeRefresh.isRefreshing = true
    }

    private fun hideProgressBar() {
        binding.paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private fun hideErrorMessage() {
        binding.itemErrorMessage.itemError.visibility = View.INVISIBLE
        isError = false
    }

    private fun showErrorMessage(message: String) {
        binding.itemErrorMessage.itemError.visibility = View.VISIBLE
        binding.itemErrorMessage.tvErrorMessage.visibility = View.VISIBLE
        binding.itemErrorMessage.tvErrorMessage.text = message
        isError = true
    }
}