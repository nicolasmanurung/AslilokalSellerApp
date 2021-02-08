package com.kodelapo.mitra.ui.account.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.kodelapo.mitra.MainActivity
import com.kodelapo.mitra.databinding.ActivityLoginBinding
import com.kodelapo.mitra.model.data.api.ApiHelper
import com.kodelapo.mitra.model.data.api.RetrofitInstance
import com.kodelapo.mitra.model.remote.request.LoginRequest
import com.kodelapo.mitra.model.remote.response.LoginResponse
import com.kodelapo.mitra.ui.account.register.RegisterActivity
import com.kodelapo.mitra.utils.KodelapoDataStore
import com.kodelapo.mitra.utils.Status
import com.kodelapo.mitra.viewmodel.KodelapoViewModelProviderFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private var dataStore = KodelapoDataStore(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lnrDaftar.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
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
            KodelapoViewModelProviderFactory(ApiHelper(RetrofitInstance.api))
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
                                GlobalScope.launch(Dispatchers.IO) {
                                    dataStore.save(
                                        "ISLOGIN",
                                        "true"
                                    )
                                    dataStore.save(
                                        "USERNAME",
                                        res.username.toString()
                                    )
                                    dataStore.save(
                                        "TOKEN",
                                        "JWT " + res.token.toString()
                                    )
                                }
                                val intent = Intent(this, MainActivity::class.java)
                                intent.putExtra("ISLOGIN", "true")
                                startActivity(intent)
                                finish()
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