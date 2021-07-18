package com.aslilokal.mitra.ui.account.register

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.aslilokal.mitra.databinding.ActivityRegisterBinding
import com.aslilokal.mitra.model.data.api.ApiHelper
import com.aslilokal.mitra.model.data.api.RetrofitInstance
import com.aslilokal.mitra.model.remote.request.RegisterRequest
import com.aslilokal.mitra.model.remote.response.LoginResponse
import com.aslilokal.mitra.ui.account.AccountViewModel
import com.aslilokal.mitra.ui.account.login.LoginActivity
import com.aslilokal.mitra.ui.account.verify.AccountRegistrationActivity
import com.aslilokal.mitra.ui.account.verify.VerifyEmailActivity
import com.aslilokal.mitra.utils.AslilokalDataStore
import com.aslilokal.mitra.utils.Status
import com.aslilokal.mitra.viewmodel.AslilokalVMProviderFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: AccountViewModel
    private lateinit var datastore : AslilokalDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        datastore = AslilokalDataStore(binding.root.context)
        hideProgressBar()
        binding.lnrLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.buttonDaftar.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val passSeller = binding.etPassword.text.toString()
            if (email.isEmpty() || passSeller.isEmpty()) {
                Toast.makeText(
                    binding.root.context,
                    "Email dan Password Tidak boleh kosong",
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else if (email.trim().isNotEmpty() || passSeller.trim().isNotEmpty()) {
                binding.llProgressBar.progressbar.visibility = View.VISIBLE
                val sellerData = RegisterRequest(
                    email,
                    passSeller,
                    "none"
                )
                setupObservers(sellerData)
            }
        }
        setupViewModel()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(AccountViewModel::class.java)
    }

    private fun setupObservers(sellerData: RegisterRequest) {
        viewModel.postRegister(sellerData).observe(this, {
            it.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        resource.data.let { response ->
                            val res: LoginResponse? = response?.body()
                            if (res?.success == false) {
                                Toast.makeText(
                                    binding.root.context,
                                    res.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else if (res?.success == true) {
                                if (res.emailVerifyStatus) {
                                    GlobalScope.launch(Dispatchers.IO) {
                                        datastore.save(
                                            "SHOPSTATUS",
                                            res.shopVerifyStatus.toString()
                                        )
                                        datastore.save(
                                            "ISLOGIN",
                                            "null"
                                        )
                                        datastore.save(
                                            "USERNAME",
                                            res.username.toString()
                                        )
                                        datastore.save(
                                            "TOKEN",
                                            "JWT " + res.token.toString()
                                        )
                                    }
                                    startActivity(
                                        Intent(
                                            this,
                                            AccountRegistrationActivity::class.java
                                        )
                                    )
                                    finish()
                                } else if (!res.emailVerifyStatus) {
                                    GlobalScope.launch(Dispatchers.IO) {
                                        datastore.save(
                                            "SHOPSTATUS",
                                            res.shopVerifyStatus.toString()
                                        )

                                        datastore.save(
                                            "ISLOGIN",
                                            "email"
                                        )
                                        datastore.save(
                                            "USERNAME",
                                            res.username.toString()
                                        )
                                        datastore.save(
                                            "TOKEN",
                                            "JWT " + res.token.toString()
                                        )
                                    }
                                    // intent email
                                    val intent = Intent(this, VerifyEmailActivity::class.java)
                                    intent.putExtra("emailSeller", sellerData.emailSeller)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                        }
                        binding.llProgressBar.progressbar.visibility = View.INVISIBLE
                    }
                    Status.LOADING -> {
                        showProgressBar()
                    }
                    Status.ERROR -> {
                        hideProgressBar()
                        Toast.makeText(
                            binding.root.context,
                            "Maaf ada kesalahan",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    private fun hideProgressBar() {
        binding.llProgressBar.progressbar.visibility = View.GONE
    }

    private fun showProgressBar() {
        binding.llProgressBar.progressbar.visibility = View.VISIBLE
    }
}