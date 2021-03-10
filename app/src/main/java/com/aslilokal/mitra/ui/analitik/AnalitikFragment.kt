package com.aslilokal.mitra.ui.analitik

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.aslilokal.mitra.databinding.FragmentAnalitikBinding
import com.aslilokal.mitra.ui.analitik.pemasukan.PemasukanFragment
import com.aslilokal.mitra.ui.analitik.pencairan.PencairanFragment
import com.aslilokal.mitra.ui.analitik.voucher.VoucherFragment

class AnalitikFragment : Fragment() {
    private var _binding: FragmentAnalitikBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalitikBinding.inflate(inflater, container, false)
        binding.vp.adapter = activity?.let {
            ViewPagerAdapter(it)
        }

        TabLayoutMediator(binding.tabs, binding.vp) { tab, position ->
            when (position) {
                0 -> tab.text = "Pemasukan"
                1 -> tab.text = "Pencairan"
                2 -> tab.text = "Voucher"
            }
        }.attach()
        binding.vp.isUserInputEnabled = false
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class ViewPagerAdapter internal constructor(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {

        override fun createFragment(position: Int): Fragment {
            when (position) {
                0 -> return PemasukanFragment()
                1 -> return PencairanFragment()
                2 -> return VoucherFragment()
            }
            return PemasukanFragment()
        }

        override fun getItemCount(): Int = 3
    }


}