package com.aslilokal.mitra.ui.profil.edit

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.aslilokal.mitra.R
import com.aslilokal.mitra.databinding.ActivityEditShopInfoBinding
import com.aslilokal.mitra.model.data.api.ApiHelper
import com.aslilokal.mitra.model.data.api.RetrofitInstance
import com.aslilokal.mitra.model.remote.response.City
import com.aslilokal.mitra.model.remote.response.RajaOngkirAddress
import com.aslilokal.mitra.model.remote.response.Shop
import com.aslilokal.mitra.ui.profil.ProfilViewModel
import com.aslilokal.mitra.utils.AslilokalDataStore
import com.aslilokal.mitra.utils.Constants
import com.aslilokal.mitra.utils.Constants.Companion.BUCKET_USR_URL
import com.aslilokal.mitra.utils.Constants.Companion.RO_KEY_ID
import com.aslilokal.mitra.utils.CustomFunction
import com.aslilokal.mitra.utils.ResourcePagination
import com.aslilokal.mitra.viewmodel.AslilokalVMProviderFactory
import com.aslilokal.mitra.viewmodel.ROViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import id.zelory.compressor.Compressor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.util.*

class EditShopInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditShopInfoBinding
    private lateinit var ROviewmodel: ROViewModel
    private lateinit var viewModel: ProfilViewModel
    private lateinit var currentDataShop: Shop
    private lateinit var username: String
    private lateinit var token: String
    private var isSellectAutocomplete: Boolean? = false
    private lateinit var listCity: ArrayList<City>
    private lateinit var filePhoto: File
    private var FILE_NAME: String = "imgShopImgUpdate"
    private lateinit var imageRequestFile: RequestBody
    private lateinit var foto: MultipartBody.Part
    private lateinit var imgKey: String

    companion object {
        private val IMAGE_CHOOSE = 1000
        private val PERMISSION_CODE = 1001
        private val IMAGE_CAMERA = 13
    }

    private var datastore = AslilokalDataStore(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditShopInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupROViewmodel()
        setupViewModel()

        setupROObserver()

        currentDataShop = intent.getParcelableExtra<Shop>("currentShop") as Shop
        setupUI()

        binding.checkBoxTime.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.txtJamBuka.visibility = View.GONE
                binding.rlJamBuka.visibility = View.GONE
                binding.etStartOpen.isEnabled = false
                binding.etEndOpen.isEnabled = false
            } else {
                binding.txtJamBuka.visibility = View.VISIBLE
                binding.rlJamBuka.visibility = View.VISIBLE
                binding.etStartOpen.isEnabled = true
                binding.etEndOpen.isEnabled = true
            }
        }

        binding.originLocation.addTextChangedListener {
            if (it?.isNotEmpty() == true) {
                binding.txtChangeProvince.visibility = View.VISIBLE
            }
            if (getCityFromAutocomplete(it.toString()) == null) {
                isSellectAutocomplete = false
            } else {
                isSellectAutocomplete = false
                binding.txtChangeProvince.visibility = View.GONE
            }
        }

        binding.txtChangeProvince.setOnClickListener {
            isSellectAutocomplete = false
            binding.originLocation.setText("")
        }

        binding.etStartOpen.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                getOpenTime()
            }
        }

        binding.etStartOpen.setOnClickListener {
            getOpenTime()
        }


        binding.etEndOpen.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                getCloseTime()
            }
        }

        binding.etEndOpen.setOnClickListener {
            getCloseTime()
        }
        binding.btnSaveData.setOnClickListener {
            setupData()
        }

        binding.profileImage.setOnClickListener {
            onAlertClickImage()
        }

        runBlocking {
            username = datastore.read("USERNAME").toString()
            token = datastore.read("TOKEN").toString()
            ROviewmodel.getCitiesByRO(RO_KEY_ID)
        }
    }

    private fun onAlertClickImage() {
        val builder = AlertDialog.Builder(binding.root.context)
        builder.setTitle("Kelola Gambar")
        builder.setMessage("Mau apa?")
        builder.setPositiveButton("Ganti gambar") { dialog, id ->
            onAlertTakeImage()
        }

//        builder.setNegativeButton("Lihat gambar") { dialog, id ->
//            //Toast.makeText(this, imageProduct, Toast.LENGTH_SHORT).show()
//        }
        builder.show()
    }

    private fun onAlertTakeImage() {
        //Instantiate builder variable
        val builder = AlertDialog.Builder(binding.root.context)
        // set title
        builder.setTitle("Ambil gambar")
        //set content area
        builder.setMessage("Silahkan pilih gambar bukti pembayaranmu")
        //set negative button
        builder.setPositiveButton(
            "Dari Galery"
        ) { dialog, id ->
            // User clicked Update Now button
            chooseImageGallery()
        }
        //set positive button
        builder.setNegativeButton(
            "Dari Kamera"
        ) { dialog, id ->
            // User cancelled the dialog
            chooseFromCamera()
        }
        builder.show()
    }

    private fun chooseImageGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED || checkSelfPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_DENIED
            ) {
                val permissions = arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                requestPermissions(permissions, PERMISSION_CODE)
            } else {
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                intent.type = "image/*"
                startActivityForResult(intent, IMAGE_CHOOSE)
            }
        } else {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_CHOOSE)
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun chooseFromCamera() {
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        filePhoto = getPhotoFile(FILE_NAME)
        val providerFile =
            FileProvider.getUriForFile(this, "com.aslilokal.mitra.fileprovider", filePhoto)
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerFile)
        if (takePhotoIntent.resolveActivity(this.packageManager) != null) {
            startActivityForResult(takePhotoIntent, IMAGE_CAMERA)
        } else {
            Toast.makeText(this, "Camera ga bisa dibuka nih", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getPhotoFile(fileName: String): File {
        val directoryStorage = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", directoryStorage)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == IMAGE_CAMERA && resultCode == Activity.RESULT_OK) {
            filePhoto.let { imageFile ->
                lifecycleScope.launch {
                    filePhoto = Compressor.compress(binding.root.context, imageFile)
                    val takenPhoto =
                        CustomFunction().rotateBitmapOrientation(filePhoto.absolutePath)
                    binding.profileImage.visibility = View.VISIBLE
                    binding.profileImage.setImageBitmap(takenPhoto)
                    imageRequestFile = filePhoto.asRequestBody("image/jpg".toMediaTypeOrNull())
                    foto = MultipartBody.Part.createFormData(
                        FILE_NAME,
                        filePhoto.name,
                        imageRequestFile
                    )
                    setupPutImage()
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
        if (requestCode == IMAGE_CHOOSE && resultCode == Activity.RESULT_OK) {
            filePhoto = File(getFileUri(data?.data))
            filePhoto.let { imageFile ->
                lifecycleScope.launch {
                    filePhoto = Compressor.compress(binding.root.context, imageFile)
                    binding.profileImage.visibility = View.VISIBLE
                    binding.profileImage.setImageURI(data?.data)

                    imageRequestFile = filePhoto.asRequestBody("image/jpg".toMediaTypeOrNull())
                    foto = MultipartBody.Part.createFormData(
                        FILE_NAME,
                        filePhoto.name,
                        imageRequestFile
                    )
                    setupPutImage()
                }
            }
        }
    }

    private fun getFileUri(uri: Uri?): String {
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? =
            uri?.let { contentResolver.query(it, filePathColumn, null, null, null) }
        cursor?.moveToFirst()
        val columnIndex: Int = cursor!!.getColumnIndex(filePathColumn[0])
        val path: String = cursor.getString(columnIndex)
        cursor.close()
        return path
    }

    private fun setupPutImage() = CoroutineScope(Dispatchers.Main).launch {
        showProgress()
        try {
            val response = RetrofitInstance.api.putImageShopUpdate(
                token,
                imgKey.toRequestBody("text/plain".toMediaTypeOrNull()),
                foto
            )
            if (response.body()?.success == true) {
                hideProgress()
                binding.root.context.cacheDir.deleteRecursively()
                Toast.makeText(
                    binding.root.context,
                    "Berhasil mengupdate",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                hideProgress()
                Glide.with(binding.root.context)
                    .load(BUCKET_USR_URL + imgKey)
                    .placeholder(R.drawable.loading_animation)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .priority(Priority.HIGH)
                    .into(binding.profileImage)
                Toast.makeText(
                    binding.root.context,
                    "Jaringan lemah, coba lagi...",
                    Toast.LENGTH_SHORT
                ).show()
            }

        } catch (exception: Exception) {
            hideProgress()
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


    private fun setupROViewmodel() {
        ROviewmodel = ViewModelProvider(
            this,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.apiRO))
        ).get(ROViewModel::class.java)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(ProfilViewModel::class.java)
    }

    private fun setupObservers() {
        viewModel.putShopResults.observe(this, { response ->
            when (response) {
                is ResourcePagination.Success -> {
                    hideProgress()
                    onBackPressed()
                    Toast.makeText(
                        this,
                        "Berhasil di ubah",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is ResourcePagination.Loading -> {
                    showProgress()
                }

                is ResourcePagination.Error -> {
                    hideProgress()
                    response.message?.let { message ->
                        Toast.makeText(
                            this,
                            "Sepertinya jaringanmu lemah, coba refresh...",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    private fun setupROObserver() {
        ROviewmodel.citiesResults.observe(this, { response ->
            when (response) {
                is ResourcePagination.Success -> {
                    response.data.let { cityResponse ->
                        hideProgress()
                        initSpinner(cityResponse?.rajaongkir?.results ?: return@observe)
                    }
                }

                is ResourcePagination.Loading -> {
                    showProgress()
                }

                is ResourcePagination.Error -> {
                    Toast.makeText(
                        binding.root.context,
                        response.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun initSpinner(cityResults: ArrayList<City>) {
        listCity = cityResults
        val cities = mutableListOf<String>()
        for (i in cityResults.indices) cities.add(
            cityResults[i].province + ", " + cityResults[i].city_name ?: ""
        )
        val cityAdapter = ArrayAdapter(
            binding.root.context,
            android.R.layout.simple_spinner_dropdown_item,
            cities
        )
        binding.originLocation.setAdapter(cityAdapter)
        binding.originLocation.setOnItemClickListener { parent, view, position, id ->
            isSellectAutocomplete = true
        }

        if (currentDataShop.rajaOngkir != null) {
            binding.originLocation.setText(currentDataShop.rajaOngkir!!.province + ", " + currentDataShop.rajaOngkir!!.city_name)
        }
        if (currentDataShop.postalCodeInput != null) {
            binding.etPostalCode.setText(currentDataShop.postalCodeInput.toString())
        }
    }

    private fun getCityFromAutocomplete(city: String): City? {
        val textCity = city.split(", ")

        val tempCity = listCity.filter {
            it.city_name.contains(textCity[1])
        }

        for (i in tempCity.indices) {
            val matchedCity = tempCity[i].city_name
            if (tempCity[i].city_name == matchedCity) {
                return tempCity[i]
            }
        }
        return null
    }

    private fun getOpenTime() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)
        TimePickerDialog(
            binding.root.context,
            { timePicker, selectedHour, selectedMinute ->
                val finalTimeTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                binding.etStartOpen.setText(finalTimeTime)
            },
            hour,
            minutes,
            true
        ).show()
    }


    private fun getCloseTime() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)
        TimePickerDialog(
            binding.root.context,
            { timePicker, selectedHour, selectedMinute ->
                val finalTimeTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                binding.etEndOpen.setText(finalTimeTime)
            },
            hour,
            minutes,
            true
        ).show()
    }

    private fun setupData() {
        if (getCityFromAutocomplete(binding.originLocation.text.toString()) == null) {
            binding.originLocation.error = "Isi sesuai pilihan"
        }
        if (binding.etNameShop.text.toString().isEmpty()) {
            binding.etNameShop.error = "Harap isi"
        }
        if (binding.etWhatsappNumber.text.toString().isEmpty()) {
            binding.etWhatsappNumber.error = "Harap isi"
        }
        if (binding.etPostalCode.text.toString().isEmpty()) {
            binding.etPostalCode.error = "Harap isi"
        } else {
            if (!(binding.checkBoxTime.isChecked) && binding.etStartOpen.text.toString()
                    .isEmpty() && binding.etEndOpen.text.toString().isEmpty()
            ) {
                Toast.makeText(
                    binding.root.context,
                    "Harap isi waktu buka toko",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val tempCity = getCityFromAutocomplete(binding.originLocation.text.toString())
                var deliveryStatus: Boolean =
                    binding.deliverySpinner.selectedItem.toString() == "Ya"
                var pickUpStatus: Boolean = binding.pickupSpinner.selectedItem.toString() == "Ya"
                var twentyFourHoursStatus = binding.checkBoxTime.isChecked


                val tempAllData = Shop(
                    null,
                    null,
                    binding.etAddressShop.text.toString(),
                    binding.etEndOpen.text.toString(),
                    username,
                    currentDataShop.imgShop,
                    deliveryStatus,
                    binding.switchIsFreeDelivery.isChecked,
                    pickUpStatus,
                    twentyFourHoursStatus,
                    binding.etPostalCode.text.toString(),
                    binding.etNameShop.text.toString(),
                    binding.etShopTelpNumber.text.toString(),
                    binding.etWhatsappNumber.text.toString(),
                    binding.etStartOpen.text.toString(),
                    null,
                    null,
                    null,
                    RajaOngkirAddress(
                        tempCity?.city_id.toString(),
                        tempCity?.city_name.toString(),
                        tempCity?.postal_code.toString(),
                        tempCity?.province.toString(),
                        tempCity?.province_id.toString(),
                        tempCity?.type.toString()
                    )
                )
                runBlocking {
                    viewModel.putShopInfo(token, username, tempAllData)
                    setupObservers()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupUI() {
        imgKey = currentDataShop.imgShop
        Glide.with(binding.root.context)
            .load(Constants.BUCKET_USR_URL + currentDataShop.imgShop)
            .placeholder(R.drawable.loading_animation)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .priority(Priority.HIGH)
            .into(binding.profileImage)
        binding.etNameShop.setText(currentDataShop.nameShop)
        if (currentDataShop.isTwentyFourHours) {
            binding.checkBoxTime.isChecked = true
            binding.rlJamBuka.visibility = View.GONE
            binding.txtJamBuka.visibility = View.GONE
        } else {
            binding.checkBoxTime.isChecked = false
            binding.etStartOpen.setText(currentDataShop.openTime)
            binding.etEndOpen.setText(currentDataShop.closeTime)
        }

        binding.switchIsFreeDelivery.isChecked = currentDataShop.isShopFreeDelivery

        ArrayAdapter.createFromResource(
            binding.root.context,
            R.array.opsi,
            android.R.layout.simple_dropdown_item_1line
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.deliverySpinner.adapter = adapter
            binding.pickupSpinner.adapter = adapter
            binding.freeOngkirSpinner.adapter = adapter
        }

        binding.deliverySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    Log.d("POSITIONDELIVERY", position.toString())
                    if (position == 0) {
                        binding.lnrFreeDelivery.visibility = View.VISIBLE
//                        binding.switchIsFreeDelivery.setOnCheckedChangeListener { buttonView, isChecked ->
//                        }
                    } else if (position == 1) {
                        binding.lnrFreeDelivery.visibility = View.GONE
                    }

                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

            }


        if (currentDataShop.isPickup) {
            binding.pickupSpinner.setSelection(0)
        } else {
            binding.pickupSpinner.setSelection(1)
        }
        if (currentDataShop.isDelivery) {
            binding.deliverySpinner.setSelection(0)
            //tambahkan kondisi ya, tampilkan apakah gratis ongkir
            binding.lnrFreeDelivery.visibility = View.VISIBLE
        } else {
            binding.deliverySpinner.setSelection(1)
            binding.lnrFreeDelivery.visibility = View.GONE
            //tambahkan kondisi tidak
        }
        binding.etAddressShop.setText(currentDataShop.addressShop)
        binding.etWhatsappNumber.setText(currentDataShop.noWhatsappShop)
        binding.etShopTelpNumber.setText(currentDataShop.noTelpSeller)

    }

    fun hideProgress() {
        binding.llProgressBar.progressbar.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    fun showProgress() {
        binding.llProgressBar.progressbar.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }
}