package com.aslilokal.mitra.ui.account.verify.review

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aslilokal.mitra.MainActivity
import com.aslilokal.mitra.databinding.ActivityReviewPageBinding
import com.aslilokal.mitra.model.data.api.RetrofitInstance
import com.aslilokal.mitra.utils.AslilokalDataStore
import kotlinx.coroutines.*
import java.io.IOException

class ReviewPageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReviewPageBinding
    private lateinit var datastore : AslilokalDataStore
    private lateinit var token: String
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        datastore = AslilokalDataStore(binding.root.context)
        //check review status and save into datastore
        runBlocking {
            username = datastore.read("USERNAME").toString()
            token = datastore.read("TOKEN").toString()
            getStatusReviewShop()
        }
    }

    private fun getStatusReviewShop() = CoroutineScope(Dispatchers.Main).launch {
        showProgress()
        try {
            val response = RetrofitInstance.api.getStatusShopVerify(token, username)
            if (response.body()?.success == true) {
                when (response.body()?.shopVerifyStatus) {
                    "none" -> {
                        hideProgress()
                        GlobalScope.launch(Dispatchers.IO) {
                            datastore.save(
                                "SHOPSTATUS",
                                "none"
                            )

                            datastore.save(
                                "ISLOGIN",
                                "null"
                            )
                        }
                        val intent = Intent(binding.root.context, MainActivity::class.java)
                        intent.putExtra("ISLOGIN", "true")
                        startActivity(intent)
                        finish()
                        Toast.makeText(
                            binding.root.context,
                            "Akun anda diverifikasi",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    "verify" -> {
                        hideProgress()
                        GlobalScope.launch(Dispatchers.IO) {
                            datastore.save(
                                "SHOPSTATUS",
                                "verify"
                            )

                            datastore.save(
                                "ISLOGIN",
                                "true"
                            )
                        }
                        val intent = Intent(binding.root.context, MainActivity::class.java)
                        intent.putExtra("ISLOGIN", "true")
                        startActivity(intent)
                        finish()
                        Toast.makeText(
                            binding.root.context,
                            "Akun anda diverifikasi",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    "review" -> {
                        hideProgress()
                    }
                    else -> {
                        hideProgress()
                        GlobalScope.launch(Dispatchers.IO) {
                            datastore.save(
                                "SHOPSTATUS",
                                "none"
                            )

                            datastore.save(
                                "ISLOGIN",
                                "null"
                            )
                        }
                        val intent = Intent(binding.root.context, MainActivity::class.java)
                        intent.putExtra("ISLOGIN", "true")
                        startActivity(intent)
                        finish()
                        Toast.makeText(
                            binding.root.context,
                            "Akun anda diverifikasi",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } else if (response.body()?.success == false) {
                Toast.makeText(
                    binding.root.context,
                    "Jaringan lemah, coba lagi...",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (exception: Exception) {
            when (exception) {
                is IOException -> Toast.makeText(
                    binding.root.context,
                    "Jaringan lemah",
                    Toast.LENGTH_SHORT
                ).show()
                else -> {
                    Toast.makeText(
                        binding.root.context,
                        exception.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("KESALAHAN", exception.toString())
                }
            }
        }
    }

    fun hideProgress() {
        binding.imageView2.visibility = View.VISIBLE
        binding.linearLayout2.visibility = View.VISIBLE
        binding.llProgressBar.progressbar.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    fun showProgress() {
        binding.imageView2.visibility = View.GONE
        binding.linearLayout2.visibility = View.GONE
        binding.llProgressBar.progressbar.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }
}