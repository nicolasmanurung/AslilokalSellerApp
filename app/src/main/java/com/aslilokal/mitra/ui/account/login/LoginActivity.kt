package com.aslilokal.mitra.ui.account.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.aslilokal.mitra.MainActivity
import com.aslilokal.mitra.databinding.ActivityLoginBinding
import com.aslilokal.mitra.model.data.api.ApiHelper
import com.aslilokal.mitra.model.data.api.RetrofitInstance
import com.aslilokal.mitra.model.remote.request.LoginRequest
import com.aslilokal.mitra.model.remote.response.LoginResponse
import com.aslilokal.mitra.ui.account.register.RegisterActivity
import com.aslilokal.mitra.ui.account.verify.AccountRegistrationActivity
import com.aslilokal.mitra.ui.account.verify.VerifyEmailActivity
import com.aslilokal.mitra.ui.account.verify.review.ReviewPageActivity
import com.aslilokal.mitra.utils.AslilokalDataStore
import com.aslilokal.mitra.utils.Status
import com.aslilokal.mitra.viewmodel.AslilokalVMProviderFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var datastore : AslilokalDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        datastore = AslilokalDataStore(binding.root.context)

        binding.lnrDaftar.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        binding.llProgressBar.progressbar.visibility = View.INVISIBLE

        binding.buttonLogin.setOnClickListener {
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
                val sellerData = LoginRequest(
                    email,
                    passSeller
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
        ).get(LoginViewModel::class.java)
    }

    private fun setupObservers(sellerData: LoginRequest) {
        viewModel.postLogin(sellerData).observe(this) {
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
                                if (!res.emailVerifyStatus) {
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

                                    val intent = Intent(this, VerifyEmailActivity::class.java)
                                    intent.putExtra("emailSeller", sellerData.emailSeller)
                                    startActivity(intent)
                                    finish()
                                } else if (res.emailVerifyStatus) {
                                    GlobalScope.launch(Dispatchers.IO) {
                                        datastore.save(
                                            "SHOPSTATUS",
                                            res.shopVerifyStatus.toString()
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
                                    when (res.shopVerifyStatus) {
                                        "none" -> {
                                            GlobalScope.launch(Dispatchers.IO) {
                                                datastore.save(
                                                    "ISLOGIN",
                                                    "null"
                                                )
                                            }
                                            startActivity(
                                                Intent(
                                                    this,
                                                    AccountRegistrationActivity::class.java
                                                )
                                            )
                                            finish()
                                        }
                                        "verify" -> {
                                            GlobalScope.launch(Dispatchers.IO) {
                                                datastore.save(
                                                    "ISLOGIN",
                                                    "true"
                                                )
                                            }
                                            val intent = Intent(this, MainActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                        "review" -> {
                                            GlobalScope.launch(Dispatchers.IO) {
                                                datastore.save(
                                                    "ISLOGIN",
                                                    "review"
                                                )
                                            }
                                            val intent =
                                                Intent(this, ReviewPageActivity::class.java)
//                                            intent.putExtra("idSellerAccount", res.username)
                                            startActivity(intent)
                                            finish()
                                        }
                                        else -> {
                                            GlobalScope.launch(Dispatchers.IO) {
                                                datastore.save(
                                                    "ISLOGIN",
                                                    "null"
                                                )
                                            }
                                            startActivity(
                                                Intent(
                                                    this,
                                                    AccountRegistrationActivity::class.java
                                                )
                                            )
                                            finish()
                                        }
                                    }
                                }
                            }
                        }
                        binding.llProgressBar.progressbar.visibility = View.INVISIBLE
                    }
                    Status.ERROR -> {
                        binding.llProgressBar.progressbar.visibility = View.INVISIBLE
                        Toast.makeText(
                            binding.root.context,
                            "Maaf ada kesalahan",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    Status.LOADING -> {
                        binding.llProgressBar.progressbar.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}