package com.kodelapo.mitra.ui.beranda

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.kodelapo.mitra.R
import com.kodelapo.mitra.databinding.FragmentBerandaBinding
import com.kodelapo.mitra.databinding.ItemBerandaImageBinding
import com.kodelapo.mitra.model.local.ItemBerandaImage

class BerandaFragment : Fragment() {

    private lateinit var berandaViewModel: BerandaViewModel
    private var _binding: FragmentBerandaBinding? = null
    private val binding get() = _binding!!
    private val imageList = ArrayList<SlideModel>()
    private val berandaImageList = ArrayList<ItemBerandaImage>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBerandaBinding.inflate(inflater, container, false)

        imageList.add(SlideModel(R.drawable.slider1, ScaleTypes.CENTER_CROP))
        imageList.add(SlideModel(R.drawable.slider2, ScaleTypes.CENTER_CROP))
        imageList.add(SlideModel(R.drawable.slider3, ScaleTypes.CENTER_CROP))

        berandaImageList.add(ItemBerandaImage(R.drawable.beranda4))
        berandaImageList.add(ItemBerandaImage(R.drawable.beranda3))
        berandaImageList.add(ItemBerandaImage(R.drawable.beranda2))
        berandaImageList.add(ItemBerandaImage(R.drawable.beranda1))

        binding.imageBerandaSlider.setImageList(imageList)

        showRvGridImage()
        return binding.root
    }

    private fun showRvGridImage() {
        binding.rvImageBeranda.layoutManager = GridLayoutManager(binding.root.context, 2)
        val gridImageAdapter = GridImageBerandaAdapter(berandaImageList)
        binding.rvImageBeranda.adapter = gridImageAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class GridImageBerandaAdapter(private val listImage: ArrayList<ItemBerandaImage>) :
        RecyclerView.Adapter<GridImageBerandaAdapter.GridViewHolder>() {

        inner class GridViewHolder(private val binding: ItemBerandaImageBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(image: ItemBerandaImage) {
                Glide.with(itemView.context)
                    .load(image.imgName)
                    .into(binding.imgItemBeranda)
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): GridImageBerandaAdapter.GridViewHolder {
            val binding =
                ItemBerandaImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return GridViewHolder(binding)
        }

        override fun onBindViewHolder(
            holder: GridImageBerandaAdapter.GridViewHolder,
            position: Int
        ) {
            holder.bind(listImage[position])
        }

        override fun getItemCount(): Int = listImage.size
    }
}