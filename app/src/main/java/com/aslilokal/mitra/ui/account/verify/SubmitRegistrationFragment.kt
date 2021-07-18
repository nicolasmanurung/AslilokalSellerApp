package com.aslilokal.mitra.ui.account.verify

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.aslilokal.mitra.MainActivity
import com.aslilokal.mitra.databinding.FragmentSubmitRegistrationBinding
import com.aslilokal.mitra.model.data.api.ApiHelper
import com.aslilokal.mitra.model.data.api.RetrofitInstance
import com.aslilokal.mitra.ui.account.AccountViewModel
import com.aslilokal.mitra.utils.AslilokalDataStore
import com.aslilokal.mitra.utils.Status
import com.aslilokal.mitra.viewmodel.AslilokalVMProviderFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class SubmitRegistrationFragment : Fragment() {

    private var _binding: FragmentSubmitRegistrationBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AccountViewModel
    private lateinit var token: String
    private lateinit var idSellerAccount: String
    private lateinit var datastore: AslilokalDataStore
    private lateinit var accountRegistrationActivity: AccountRegistrationActivity

    //    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubmitRegistrationBinding.inflate(inflater, container, false)
        datastore = AslilokalDataStore(binding.root.context)
        accountRegistrationActivity = activity as AccountRegistrationActivity

        runBlocking {
            token = datastore.read("TOKEN").toString()
            idSellerAccount = datastore.read("USERNAME").toString()
        }

        setupViewModel()

        binding.sendButton.isEnabled = false
        binding.sendButton.isClickable = false

        binding.checkBoxAgreement.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.sendButton.isEnabled = true
                binding.sendButton.isClickable = true
            } else {
                binding.sendButton.isEnabled = false
                binding.sendButton.isClickable = false
            }
        }

        binding.sendButton.setOnClickListener {
            setupObservable()
        }

        return binding.root
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            viewModelStore,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(AccountViewModel::class.java)
    }

    private fun setupObservable() {
        var status = "review"
        viewModel.putRegistrationShopSubmit(
            token,
            idSellerAccount,
            status
        ).observe(viewLifecycleOwner, {
            it.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        when (resource.data?.body()?.success) {
                            true -> {
                                GlobalScope.launch(Dispatchers.IO) {
                                    datastore.save(
                                        "ISLOGIN",
                                        "review"
                                    )
                                }
                                accountRegistrationActivity.hideProgress()
                                val intent = Intent(activity, MainActivity::class.java)
                                startActivity(intent)
                                activity?.finish()
                            }
                            false -> {
                                accountRegistrationActivity.hideProgress()
                                Toast.makeText(
                                    binding.root.context,
                                    "Jaringan kamu lemah, coba ulang yah...",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    Status.LOADING -> {
                        accountRegistrationActivity.showProgress()
                    }
                    Status.ERROR -> {
                        accountRegistrationActivity.showProgress()
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
}