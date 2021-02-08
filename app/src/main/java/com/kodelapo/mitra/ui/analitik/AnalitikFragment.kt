package com.kodelapo.mitra.ui.analitik

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kodelapo.mitra.databinding.FragmentAnalitikBinding

class AnalitikFragment : Fragment() {

    private var _binding: FragmentAnalitikBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalitikBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }
}