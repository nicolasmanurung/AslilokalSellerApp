package com.aslilokal.mitra.ui.debtor.detail

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.aslilokal.mitra.databinding.ActivityDetailDebtorBinding
import com.aslilokal.mitra.model.data.api.ApiHelper
import com.aslilokal.mitra.model.data.api.RetrofitInstance
import com.aslilokal.mitra.model.remote.response.DebtorItem
import com.aslilokal.mitra.ui.debtor.DebtorViewModel
import com.aslilokal.mitra.utils.CustomFunction
import com.aslilokal.mitra.utils.KodelapoDataStore
import com.aslilokal.mitra.utils.Status
import com.aslilokal.mitra.viewmodel.KodelapoViewModelProviderFactory
import kotlinx.coroutines.runBlocking
import me.abhinay.input.CurrencySymbols

class DetailDebtorActivity : AppCompatActivity() {
    //    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private lateinit var binding: ActivityDetailDebtorBinding
    private lateinit var viewModel: DebtorViewModel
    private var datastore = KodelapoDataStore(this)
    private lateinit var token: String
    private lateinit var username: String
    private lateinit var debtorItem: DebtorItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailDebtorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        runBlocking {
            token = datastore.read("TOKEN").toString()
            username = datastore.read("USERNAME").toString()
        }

        debtorItem = intent.getParcelableExtra<DebtorItem>("debtor") as DebtorItem

        hideProgress()

        binding.etSumDebt.setCurrency(CurrencySymbols.INDONESIA)
        binding.etSumDebt.setDelimiter(false)
        binding.etSumDebt.setDecimals(false)
        binding.etSumDebt.setSpacing(false)
        binding.etSumDebt.setSeparator(".")

        var tempEt = EditText(this)

        tempEt.setText(debtorItem.nameDebtor)
        binding.etNameDebtor.text = tempEt.editableText
        tempEt.setText(debtorItem.totalDebt.toString())
        binding.etSumDebt.text = tempEt.editableText
        tempEt.setText(debtorItem.descDebt)
        binding.etNotes.text = tempEt.editableText
        binding.txtDateCreated.text = CustomFunction().isoTimeToDateMonth(debtorItem.createAt!!)

        if (debtorItem.statusTransaction) {
            binding.ubahTambah.visibility = View.GONE
        } else {
            binding.ubahTambah.setOnClickListener {
                var newDebtor = DebtorItem(
                    debtorItem.__v,
                    debtorItem._id,
                    debtorItem.createAt,
                    debtorItem.descDebt,
                    debtorItem.idSellerAccount,
                    debtorItem.nameDebtor,
                    true,
                    debtorItem.totalDebt
                )
                onAlertTakeImage(newDebtor)
            }
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            KodelapoViewModelProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(DebtorViewModel::class.java)
    }

    private fun setObservable(debtorItem: DebtorItem) {
        showProgress()
        viewModel.putOneDebtor(token, debtorItem._id!!, debtorItem).observe(this, {
            it.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        hideProgress()
                        when (resource.data?.body()?.success) {
                            true -> {
                                Toast.makeText(this, "Berhasil di update", Toast.LENGTH_SHORT)
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

    private fun onAlertTakeImage(debtor: DebtorItem) {
        //Instantiate builder variable
        val builder = AlertDialog.Builder(binding.root.context)
        // set title
        builder.setTitle("Ubah status")
        //set content area
        builder.setMessage("Utang sudah dibayar?")
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