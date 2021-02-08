package com.kodelapo.mitra.ui.kelola

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.kodelapo.mitra.databinding.FragmentKelolaProdukBinding
import com.kodelapo.mitra.ui.kelola.fashion.FashionFragment
import com.kodelapo.mitra.ui.kelola.jasa.JasaFragment
import com.kodelapo.mitra.ui.kelola.kuliner.KulinerFragment
import com.kodelapo.mitra.ui.kelola.review.ReviewFragment
import com.kodelapo.mitra.ui.kelola.sembako.SembakoFragment

class KelolaProdukFragment : Fragment() {

    private lateinit var kelolaProdukViewModel: KelolaProdukViewModel
    private var _binding: FragmentKelolaProdukBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKelolaProdukBinding.inflate(inflater, container, false);
        binding.vp.adapter = activity?.let {
            ViewPagerAdapter(it)
        }

        TabLayoutMediator(binding.tabs, binding.vp) { tab, position ->
            when (position) {
                0 -> tab.text = "Kuliner"
                1 -> tab.text = "Jasa"
                2 -> tab.text = "Sembako"
                3 -> tab.text = "Fashion"
                4 -> tab.text = "Review"
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
                0 -> return KulinerFragment()
                1 -> return JasaFragment()
                2 -> return SembakoFragment()
                3 -> return FashionFragment()
                4 -> return ReviewFragment()
            }
            return KulinerFragment()
        }

        override fun getItemCount(): Int {
            return 5
        }
    }

}