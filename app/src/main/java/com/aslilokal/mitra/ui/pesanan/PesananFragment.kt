package com.aslilokal.mitra.ui.pesanan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.aslilokal.mitra.databinding.FragmentPesananBinding
import com.aslilokal.mitra.ui.pesanan.bayar.BayarFragment
import com.aslilokal.mitra.ui.pesanan.menunggu.MenungguFragment
import com.aslilokal.mitra.ui.pesanan.proses.ProsesFragment
import com.aslilokal.mitra.ui.pesanan.selesai.SelesaiFragment

class PesananFragment : Fragment() {
    private var _binding: FragmentPesananBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPesananBinding.inflate(inflater, container, false)
        binding.vp.adapter = activity?.let {
            ViewPagerAdapter(it)
        }

        TabLayoutMediator(binding.tabs, binding.vp) { tab, position ->
            when (position) {
                0 -> tab.text = "Bayar"
                1 -> tab.text = "Diproses"
                2 -> tab.text = "Menunggu"
                3 -> tab.text = "Selesai"
            }
        }.attach()

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
                0 -> return BayarFragment()
                1 -> return ProsesFragment()
                2 -> return MenungguFragment()
                3 -> return SelesaiFragment()
            }
            return BayarFragment()
        }

        override fun getItemCount(): Int = 4
    }
}