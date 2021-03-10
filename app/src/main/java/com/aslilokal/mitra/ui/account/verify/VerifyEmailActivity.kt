package com.aslilokal.mitra.ui.account.verify

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.aslilokal.mitra.databinding.ActivityVerifyEmailBinding
import com.aslilokal.mitra.model.data.api.ApiHelper
import com.aslilokal.mitra.model.data.api.RetrofitInstance
import com.aslilokal.mitra.model.remote.response.StatusResponse
import com.aslilokal.mitra.ui.account.AccountViewModel
import com.aslilokal.mitra.utils.KodelapoDataStore
import com.aslilokal.mitra.utils.Status
import com.aslilokal.mitra.viewmodel.KodelapoViewModelProviderFactory
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class VerifyEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerifyEmailBinding
    private lateinit var viewModel: AccountViewModel
    private lateinit var emailSeller: String

    //    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private var datastore = KodelapoDataStore(this)
    private lateinit var verifyToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideProgress()

        emailSeller = intent.getStringExtra("emailSeller")!!
        binding.tvEmail.text = emailSeller

        setupViewModel()
        countDownTimer.start()

        binding.etCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (count == 6) {
                    showProgress()
                    verifyToken = s.toString()
                    lifecycleScope.launch {
                        val token = datastore.read("TOKEN").toString()
                        getToken(token, verifyToken)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
                val result: String = s.toString().replace(" ", "")
                if (s.toString() != result) {
                    binding.etCode.setText(result)
                    binding.etCode.setSelection(result.length)
                }
            }
        })

        binding.txtResend.setOnClickListener {
            lifecycleScope.launch {
                val token = datastore.read("TOKEN").toString()
                postResubmitToken(token)
            }
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            KodelapoViewModelProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(AccountViewModel::class.java)
    }

    private fun postResubmitToken(authToken: String) {
        Log.d("TOKEN", authToken)
        viewModel.postResubmit(authToken).observe(this, {
            it.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        hideProgress()
                        resource.data.let { response ->
                            val response: StatusResponse? = response?.body()
                            if (response?.success == false) {
                                Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                            } else if (response?.success == true) {
                                Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                                binding.txtResend.visibility = View.GONE

                                binding.tvReceive.visibility = View.VISIBLE
                                binding.tvTime.visibility = View.VISIBLE
                                countDownTimer.start()
                            }
                        }
                    }

                    Status.LOADING -> {
                        showProgress()
                    }

                    Status.ERROR -> {
                        hideProgress()
                        Toast.makeText(this, "Ada kesalahan", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun getToken(authToken: String, verifyToken: String) {
        viewModel.getToken(authToken, verifyToken).observe(this, {
            it.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        hideProgress()
                        resource.data.let { response ->
                            val tokenResponse: StatusResponse? = response?.body()
                            if (tokenResponse?.success == false) {
                                Toast.makeText(this, tokenResponse.message, Toast.LENGTH_SHORT)
                                    .show()
                            } else if (tokenResponse?.success == true) {
                                startActivity(Intent(this, AccountRegistrationActivity::class.java))
                                finish()
                            }
                        }
                    }
                    Status.LOADING -> {
                        showProgress()
                    }

                    Status.ERROR -> {
                        hideProgress()
                        Log.d("VERIF", resource.message.toString())
                        Toast.makeText(this, "Ada kesalahan", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }


    private val countDownTimer = object : CountDownTimer(30000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            binding.txtResend.visibility = View.GONE
            val secondLeft =
                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                )
            binding.tvTime.text = String.format(
                "Kirim ulang kode dalam %d:%d",
                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                secondLeft
            )
        }

        override fun onFinish() {
            binding.txtResend.visibility = View.VISIBLE
            binding.tvReceive.visibility = View.GONE
            binding.tvTime.text = ""
        }
    }

    private fun hideProgress() {
        binding.llProgressBar.progressbar.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private fun showProgress() {
        binding.llProgressBar.progressbar.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }
}