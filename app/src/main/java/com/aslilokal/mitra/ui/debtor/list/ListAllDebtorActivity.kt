package com.aslilokal.mitra.ui.debtor.list

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.aslilokal.mitra.databinding.ActivityListAllDebtorBinding
import com.aslilokal.mitra.model.data.api.ApiHelper
import com.aslilokal.mitra.model.data.api.RetrofitInstance
import com.aslilokal.mitra.model.remote.response.DebtorItem
import com.aslilokal.mitra.ui.debtor.DebtorViewModel
import com.aslilokal.mitra.ui.debtor.complete.CompleteDebtorFragment
import com.aslilokal.mitra.ui.debtor.tambah.TambahDebtorActivity
import com.aslilokal.mitra.ui.debtor.uncomplete.UncompleteDebtorFragment
import com.aslilokal.mitra.utils.CustomFunction
import com.aslilokal.mitra.utils.KodelapoDataStore
import com.aslilokal.mitra.utils.ResourcePagination
import com.aslilokal.mitra.viewmodel.KodelapoViewModelProviderFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ListAllDebtorActivity : AppCompatActivity() {
    //    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private lateinit var binding: ActivityListAllDebtorBinding
    private lateinit var viewModel: DebtorViewModel
    private var datastore = KodelapoDataStore(this)

    private lateinit var token: String
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListAllDebtorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        runBlocking {
            token = datastore.read("TOKEN").toString()
            username = datastore.read("USERNAME").toString()
            viewModel.getDebtor(token, username, year = "2018", month = "1")
        }

        binding.llProgressBar.progressbar.visibility = View.INVISIBLE
        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
            GlobalScope.launch(Dispatchers.Main) {
                viewModel.getDebtor(token, username, year = "2018", month = "1")
                setupObservers()
            }
        }

        binding.fabDebtor.setOnClickListener {
            startActivity(Intent(this, TambahDebtorActivity::class.java))
        }

        setupObservers()
    }


    class DebtorViewPagerAdapter internal constructor(
        fragmentActivity: FragmentActivity
    ) :
        FragmentStateAdapter(fragmentActivity) {

        override fun getItemCount(): Int {
            return 2
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> UncompleteDebtorFragment()
                1 -> CompleteDebtorFragment()
                else -> UncompleteDebtorFragment()
            }
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            KodelapoViewModelProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(DebtorViewModel::class.java)
    }

    fun setupObservers() {
        viewModel.debtors.observe(this, { response ->
            when (response) {
                is ResourcePagination.Success -> {
                    response.data?.result.let { response ->
                        runBlocking {
                            separateData(response)
                        }
                    }
                }

                is ResourcePagination.Error -> {
                    hideProgress()
                    hideSwipeProgress()
                    response.message?.let { message ->
                        Toast.makeText(this, "Error pada: $message", Toast.LENGTH_SHORT).show()
                    }
                }

                is ResourcePagination.Loading -> {
                    showProgress()
                    hideSwipeProgress()
                }
            }
        })
    }

    private fun separateData(debtorList: ArrayList<DebtorItem>?) {
        var customFunction = CustomFunction()
        var finalUncomplete = ArrayList<DebtorItem>()
        var finalComplete = ArrayList<DebtorItem>()
        var listAllDebt = debtorList
        if (listAllDebt != null) {
            var uncomplete = listAllDebt.filter { !it.statusTransaction }
            var complete = listAllDebt.filter { it.statusTransaction }

            if (uncomplete.isNotEmpty()) {
                var sumAll = 0
                uncomplete.forEach { debtorItem ->
                    sumAll += debtorItem.totalDebt
                    finalUncomplete.add(debtorItem)
                }
                //uncompleteArrayList = finalUncomplete
                binding.txtSumUncomplete.text = customFunction.formatRupiah(sumAll.toDouble())
            } else {
                binding.txtSumUncomplete.text = customFunction.formatRupiah(0.toDouble())
            }

            if (complete.isNotEmpty()) {
                var sumAll = 0
                complete.forEach { debtorItem ->
                    sumAll += debtorItem.totalDebt
                    finalComplete.add(debtorItem)
                }
                //completeArrayList = finalComplete
                binding.txtSumComplete.text = customFunction.formatRupiah(sumAll.toDouble())
            } else {
                binding.txtSumComplete.text = customFunction.formatRupiah(0.toDouble())
            }
        }

        hideSwipeProgress()
        hideProgress()
        binding.vp.adapter = DebtorViewPagerAdapter(this)
        TabLayoutMediator(binding.tabs, binding.vp) { tab, position ->
            when (position) {
                0 -> tab.text = "Belum Lunas"
                1 -> tab.text = "Sudah Lunas"
            }
        }.attach()
        binding.vp.isUserInputEnabled = false
    }

    private fun hideProgress() {
        binding.llProgressBar.progressbar.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun hideSwipeProgress() {
        binding.swipeRefresh.isRefreshing = false
    }

    private fun showProgress() {
        binding.llProgressBar.progressbar.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }
}