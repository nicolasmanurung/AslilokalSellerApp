package com.aslilokal.mitra.ui.profil

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.aslilokal.mitra.NOTIFICATION_TOPIC
import com.aslilokal.mitra.R
import com.aslilokal.mitra.databinding.FragmentProfilBinding
import com.aslilokal.mitra.model.data.api.ApiHelper
import com.aslilokal.mitra.model.data.api.RetrofitInstance
import com.aslilokal.mitra.model.remote.response.Shop
import com.aslilokal.mitra.model.remote.response.ShopResponse
import com.aslilokal.mitra.ui.account.login.LoginActivity
import com.aslilokal.mitra.ui.profil.edit.EditShopInfoActivity
import com.aslilokal.mitra.utils.AslilokalDataStore
import com.aslilokal.mitra.utils.Constants.Companion.BUCKET_USR_URL
import com.aslilokal.mitra.utils.Status
import com.aslilokal.mitra.viewmodel.AslilokalVMProviderFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Response

class ProfilFragment : Fragment() {
    private var _binding: FragmentProfilBinding? = null
    private val binding get() = _binding!!
    private lateinit var datastore: AslilokalDataStore
    private lateinit var skeletonLayout: ShimmerFrameLayout
    private lateinit var viewModel: ProfilViewModel
    private var currentDataShop: Shop? = null
    private lateinit var username: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilBinding.inflate(inflater, container, false)
        datastore = AslilokalDataStore(binding.root.context)

        skeletonLayout = binding.shimmerViewContainer
        showSkeleton()


        setupViewModel()

        viewLifecycleOwner.lifecycleScope.launch {
            username = datastore.read("USERNAME").toString()
            val token = datastore.read("TOKEN").toString()
            setupObservers(token, username)
        }


        binding.ubahTxt.setOnClickListener {
            Log.d("CURRENTDATASHOP", currentDataShop.toString())
            if (currentDataShop != null) {
                val intent = Intent(activity, EditShopInfoActivity::class.java)
                intent.putExtra("currentShop", currentDataShop)
                startActivity(intent)
            }
        }

        binding.buttonLogout.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                datastore.save(
                    "ISLOGIN",
                    "null"
                )
                datastore.save(
                    "USERNAME",
                    "null"
                )
                datastore.save(
                    "TOKEN",
                    "null"
                )
            }
            val finalTopic = "$NOTIFICATION_TOPIC$username"
            Log.d("FINALTOPIC", finalTopic)
            FirebaseMessaging.getInstance().unsubscribeFromTopic(finalTopic)
            binding.root.context.startActivity(
                Intent(
                    binding.root.context,
                    LoginActivity::class.java
                )
            )
            activity?.finish()
        }

        binding.swipeRefresh.isRefreshing = false

        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
            viewLifecycleOwner.lifecycleScope.launch {
                val username = datastore.read("USERNAME").toString()
                val token = datastore.read("TOKEN").toString()
                setupObservers(token, username)
            }
        }
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(ProfilViewModel::class.java)
    }

    private fun setupObservers(token: String, idAccount: String) {
        viewModel.getProfile(token, idAccount).observe(viewLifecycleOwner, {
            it.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        hideSkeleton()
                        resource.data.let { response ->
                            response?.let { it1 ->
                                if (it1 != null) {
                                    setupUI(it1)
                                }
                            }
                        }
                    }
                    Status.ERROR -> {
                        hideSkeleton()
                        Toast.makeText(
                            binding.root.context,
                            "Maaf jaringan anda lemah, silahkan refresh",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    Status.LOADING -> {
                        showSkeleton()
                    }
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun setupUI(response: Response<ShopResponse>) {
        currentDataShop = response.body()?.result

        Glide.with(binding.root)
            .load(BUCKET_USR_URL + currentDataShop?.imgShop)
            .placeholder(R.drawable.loading_animation)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .priority(Priority.HIGH)
            .into(binding.profileImage)
        binding.txtNameShop.text = currentDataShop?.nameShop
        if (currentDataShop?.isTwentyFourHours == true) {
            binding.txtTimeOpen.text = "Buka 24 Jam"
        } else {
            binding.txtTimeOpen.text = currentDataShop?.openTime + "-" + currentDataShop?.closeTime
        }

        if (currentDataShop?.isPickup == true) {
            binding.txtStatusPickUp.text = "Ya"
        } else {
            binding.txtStatusPickUp.text = "Tidak"
        }
        if (currentDataShop?.isDelivery == true) {
            binding.txtStatusDelivery.text = "Ya"
        } else {
            binding.txtStatusDelivery.text = "Tidak"
        }
        binding.txtAddress.text = currentDataShop?.addressShop
        binding.txtWhatsappNumber.text = currentDataShop?.noWhatsappShop
    }

    private fun showSkeleton() {
        skeletonLayout.showShimmer(true)
        skeletonLayout.startShimmer()
    }

    private fun hideSkeleton() {
        skeletonLayout.stopShimmer()
        skeletonLayout.hideShimmer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        showSkeleton()
        viewLifecycleOwner.lifecycleScope.launch {
            val username = datastore.read("USERNAME").toString()
            val token = datastore.read("TOKEN").toString()
            setupObservers(token, username)
        }
    }
}