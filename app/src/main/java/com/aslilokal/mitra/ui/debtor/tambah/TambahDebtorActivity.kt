package com.aslilokal.mitra.ui.debtor.tambah

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.aslilokal.mitra.databinding.ActivityTambahDebtorBinding
import com.aslilokal.mitra.model.data.api.ApiHelper
import com.aslilokal.mitra.model.data.api.RetrofitInstance
import com.aslilokal.mitra.model.remote.response.DebtorItem
import com.aslilokal.mitra.ui.debtor.DebtorViewModel
import com.aslilokal.mitra.utils.AslilokalDataStore
import com.aslilokal.mitra.utils.Status
import com.aslilokal.mitra.viewmodel.AslilokalVMProviderFactory
import kotlinx.coroutines.runBlocking
import me.abhinay.input.CurrencySymbols

class TambahDebtorActivity : AppCompatActivity() {
    //    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private lateinit var binding: ActivityTambahDebtorBinding
    private lateinit var viewModel: DebtorViewModel
    private lateinit var datastore: AslilokalDataStore
    private lateinit var token: String
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTambahDebtorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        datastore = AslilokalDataStore(binding.root.context)
        setupViewModel()
        runBlocking {
            token = datastore.read("TOKEN").toString()
            username = datastore.read("USERNAME").toString()
        }

        binding.etSumDebt.setCurrency(CurrencySymbols.INDONESIA)
        binding.etSumDebt.setDelimiter(false)
        binding.etSumDebt.setDecimals(false)
        binding.etSumDebt.setSpacing(false)
        binding.etSumDebt.setSeparator(".")

        hideProgress()

        binding.btnTambahDebtor.setOnClickListener {
            if (checkIsEmpty()) {
                Toast.makeText(this, "Harap isi data yang masih kosong", Toast.LENGTH_SHORT).show()
            } else if (!(checkIsEmpty())) {
                var newDebtor =
                    DebtorItem(
                        null,
                        null,
                        null,
                        binding.etNotes.text.toString(),
                        username,
                        binding.etNameDebtor.text.toString(),
                        false,
                        binding.etSumDebt.cleanIntValue
                    )
                onAlertDialog(newDebtor)
            }
        }
    }

    private fun checkIsEmpty(): Boolean {
        return binding.etNameDebtor.text.toString().isEmpty() || binding.etNotes.text.toString()
            .isEmpty() || binding.etSumDebt.text.toString().isEmpty()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(DebtorViewModel::class.java)
    }

    private fun onAlertDialog(debtor: DebtorItem) {
        //Instantiate builder variable
        val builder = AlertDialog.Builder(binding.root.context)
        // set title
        builder.setTitle("Tambah pengutang")
        //set content area
        builder.setMessage("Apakah semua data sudah benar?")
        //set negative button
        builder.setPositiveButton(
            "Sudah"
        ) { dialog, id ->
            // User clicked Update Now button
            setObservable(debtor)
        }
        //set positive button
        builder.setNegativeButton(
            "Batal"
        ) { dialog, id ->
            // User cancelled the dialog
        }
        builder.show()
    }

    private fun setObservable(debtor: DebtorItem) {
        showProgress()
        viewModel.postOneDebtor(token, debtor).observe(this, {
            it.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        hideProgress()
                        when (resource.data?.body()?.success) {
                            true -> {
                                Toast.makeText(this, "Berhasil menambahkan", Toast.LENGTH_SHORT)
                                    .show()
                                onBackPressed()
                            }

                            false -> {
                                Toast.makeText(
                                    this,
                                    "Jaringan kamu lemah, coba ulang yah...",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        showProgress()
                    }

                    Status.ERROR -> {
                        hideProgress()
                        Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun hideProgress() {
        binding.llProgressBar.progressbar.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun showProgress() {
        binding.llProgressBar.progressbar.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }
}