package com.aslilokal.mitra.ui.kelola.jasa

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aslilokal.mitra.databinding.FragmentJasaBinding
import com.aslilokal.mitra.model.data.api.ApiHelper
import com.aslilokal.mitra.model.data.api.RetrofitInstance
import com.aslilokal.mitra.ui.adapter.ProductAdapter
import com.aslilokal.mitra.ui.kelola.KelolaProdukViewModel
import com.aslilokal.mitra.utils.Constants.Companion.QUERY_PAGE_SIZE
import com.aslilokal.mitra.utils.AslilokalDataStore
import com.aslilokal.mitra.utils.ResourcePagination
import com.aslilokal.mitra.viewmodel.AslilokalVMProviderFactory
import kotlinx.coroutines.launch

class JasaFragment : Fragment() {
    //    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private var _binding: FragmentJasaBinding? = null
    private val binding get() = _binding!!

    lateinit var viewModel: KelolaProdukViewModel
    lateinit var productAdapter: ProductAdapter

    private lateinit var datastore: AslilokalDataStore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJasaBinding.inflate(inflater, container, false)

        datastore = AslilokalDataStore(binding.root.context)
        setupViewModel()

        viewLifecycleOwner.lifecycleScope.launch {
            val username = datastore.read("USERNAME").toString()
            val token = datastore.read("TOKEN").toString()
            viewModel.getProducts(token, username, "jasa")
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
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(KelolaProdukViewModel::class.java)
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter()
        binding.rvJasa.apply {
            adapter = productAdapter
            layoutManager = GridLayoutManager(binding.root.context, 2)
            addOnScrollListener(this@JasaFragment.scrollListener)
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
                        productAdapter.differ.submitList(productResponse?.docs?.toList())

                        if (productResponse?.docs?.toList()?.size!! <= 0) {
                            binding.lnrEmpty.visibility = View.VISIBLE
                        } else {
                            binding.lnrEmpty.visibility = View.GONE
                        }
                        val totalPages = productResponse.totalPages
                        isLastPage = (viewModel.productPage - 1) == totalPages
                        if (isLastPage) {
                            binding.rvJasa.setPadding(0, 0, 0, 0)
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
                    hideErrorMessage()
                }
            }
        })

        binding.itemErrorMessage.btnRetry.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val username = datastore.read("USERNAME").toString()
                val token = datastore.read("TOKEN").toString()
                viewModel.getProducts(token, username, "jasa")
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
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
            val shouldPaginate =
                isNoErrors && isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning &&
                        isTotalMoreThanVisible && isScrolling
            if (shouldPaginate) {
                var username: String
                var token: String
                viewLifecycleOwner.lifecycleScope.launch {
                    username = datastore.read("USERNAME").toString()
                    token = datastore.read("TOKEN").toString()
                    viewModel.getProducts(token, username, "jasa")
                    isScrolling = false
                }
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                Log.d("ISSCROLL", "Eksekusi")
                isScrolling = true
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