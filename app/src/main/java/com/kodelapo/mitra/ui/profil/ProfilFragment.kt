package com.kodelapo.mitra.ui.profil

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.kodelapo.mitra.databinding.FragmentProfilBinding
import com.kodelapo.mitra.model.data.api.ApiHelper
import com.kodelapo.mitra.model.data.api.RetrofitInstance
import com.kodelapo.mitra.model.remote.response.Shop
import com.kodelapo.mitra.model.remote.response.ShopResponse
import com.kodelapo.mitra.ui.account.login.LoginActivity
import com.kodelapo.mitra.utils.Constants.Companion.BUCKET_USR_URL
import com.kodelapo.mitra.utils.KodelapoDataStore
import com.kodelapo.mitra.utils.Status
import com.kodelapo.mitra.viewmodel.KodelapoViewModelProviderFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Response

class ProfilFragment : Fragment() {

    private var _binding: FragmentProfilBinding? = null
    private val binding get() = _binding!!
    private lateinit var dataStore: KodelapoDataStore
    private lateinit var skeletonLayout: ShimmerFrameLayout
    private lateinit var viewModel: ProfilViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilBinding.inflate(inflater, container, false)

        skeletonLayout = binding.shimmerViewContainer
        showSkeleton()

        dataStore = KodelapoDataStore(binding.root.context)

        setupViewModel()

        viewLifecycleOwner.lifecycleScope.launch {
            val username = dataStore.read("USERNAME").toString()
            val token = dataStore.read("TOKEN").toString()
            setupObservers(token, username)
        }


        binding.buttonLogout.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                dataStore.save(
                    "ISLOGIN",
                    "null"
                )
                dataStore.save(
                    "USERNAME",
                    "null"
                )
                dataStore.save(
                    "TOKEN",
                    "null"
                )
            }

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
                val username = dataStore.read("USERNAME").toString()
                val token = dataStore.read("TOKEN").toString()
                setupObservers(token, username)
            }
        }
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            KodelapoViewModelProviderFactory(ApiHelper(RetrofitInstance.api))
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
                                setupUI(it1)
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
        var shopData: Shop = response.body()?.result!!

        Glide.with(binding.root)
            .load(BUCKET_USR_URL + shopData.imgShop)
            .into(binding.profileImage)
        binding.txtNameShop.text = shopData.nameShop
        binding.txtTimeOpen.text = shopData.openTime ?: "0"
        if (shopData.isPickup) {
            binding.txtStatusPickUp.text = "Ya"
        } else {
            binding.txtStatusPickUp.text = "Tidak"
        }
        if (shopData.isDelivery) {
            binding.txtStatusDelivery.text = "Ya"
        } else {
            binding.txtStatusDelivery.text = "Tidak"
        }
        binding.txtFreeOngkir.text = shopData.freeOngkirLimitKm + " KM"
        binding.txtAddress.text = shopData.addressShop
        binding.txtWhatsappNumber.text = shopData.noWhatsappShop
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
}