package com.aslilokal.mitra.ui.account.verify

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.aslilokal.mitra.R
import com.aslilokal.mitra.databinding.FragmentShopInfoBinding
import com.aslilokal.mitra.model.data.api.ApiHelper
import com.aslilokal.mitra.model.data.api.RetrofitInstance
import com.aslilokal.mitra.model.remote.response.City
import com.aslilokal.mitra.ui.account.AccountViewModel
import com.aslilokal.mitra.utils.AslilokalDataStore
import com.aslilokal.mitra.utils.Constants.Companion.RO_KEY_ID
import com.aslilokal.mitra.utils.ResourcePagination
import com.aslilokal.mitra.utils.Status
import com.aslilokal.mitra.viewmodel.AslilokalVMProviderFactory
import com.aslilokal.mitra.viewmodel.ROViewModel
import id.zelory.compressor.Compressor
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.*

class ShopInfoFragment : Fragment() {
    private var _binding: FragmentShopInfoBinding? = null
    private val binding get() = _binding!!
    private lateinit var shopImg: File
    private lateinit var accountRegistrationActivity: AccountRegistrationActivity
    private var FILE_SHOP = "imgShop"
    private val REQUEST_CODE = 13

    private lateinit var viewModel: AccountViewModel
    private lateinit var ROviewmodel: ROViewModel
    private var isSellectAutocomplete: Boolean? = false
    private lateinit var datastore: AslilokalDataStore
    private lateinit var tempEt: EditText
    private lateinit var listCity: ArrayList<City>

    private lateinit var imgShopRequestFile: RequestBody

    private lateinit var fotoShop: MultipartBody.Part

    // Data form
    private lateinit var token: String
    private lateinit var idSellerAccount: RequestBody
    private lateinit var nameShop: RequestBody
    private lateinit var noTelpSeller: RequestBody
    private lateinit var noWhatsappShop: RequestBody
    private lateinit var isPickup: RequestBody
    private lateinit var isDelivery: RequestBody
    private lateinit var addressShop: RequestBody
    private lateinit var isTwentyFourHours: RequestBody
    private lateinit var openTime: RequestBody
    private lateinit var closeTime: RequestBody
    private lateinit var postalCode: RequestBody

    //RO Data form
    private lateinit var cityId: RequestBody
    private lateinit var provinceId: RequestBody
    private lateinit var province: RequestBody
    private lateinit var cityName: RequestBody
    private lateinit var postalCodeRO: RequestBody

    // Temp Var
    private var tempDelivery: String = ""
    private var tempPickup: String = ""
    //private var tempFreeDelivery: String = ""

    companion object {
        private const val IMAGE_CHOOSE = 1000
        private const val PERMISSION_CODE = 1001
    }
//    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopInfoBinding.inflate(layoutInflater, container, false)
        // Inflate the layout for this fragment
        accountRegistrationActivity = activity as AccountRegistrationActivity
        accountRegistrationActivity.showProgress()

        tempEt = EditText(binding.root.context)
        datastore = AslilokalDataStore(binding.root.context)

        setupROViewmodel()
        setupViewModel()

        runBlocking {
            token = datastore.read("TOKEN").toString()
            idSellerAccount = datastore.read("USERNAME").toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())
            ROviewmodel.getCitiesByRO(RO_KEY_ID)
        }

        setupROObserver()

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

        binding.btnPickImgShop.setOnClickListener {
            onAlertDialog()
        }

        binding.nextFragmentBtn.setOnClickListener {
            alertDialogSubmit()
        }

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

        tempDelivery =
            binding.deliverySpinner.selectedItem.toString().toLowerCase(Locale.getDefault())
        tempPickup = binding.pickupSpinner.selectedItem.toString().toLowerCase(Locale.getDefault())

        //tempFreeDelivery = binding.freeOngkirSpinner.selectedItem.toString().toLowerCase(Locale.getDefault())

        tempDelivery = if (tempDelivery == "ya") {
            "true"
        } else {
            "false"
        }

        tempPickup = if (tempPickup == "ya") {
            "true"
        } else {
            "false"
        }

//        tempFreeDelivery = if (tempFreeDelivery == "ya") {
//            "true"
//        } else {
//            "false"
//        }

        return binding.root
    }

    private fun getOpenTime() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)
        TimePickerDialog(
            binding.root.context,
            { timePicker, selectedHour, selectedMinute ->
                val finalTimeTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                tempEt.setText(finalTimeTime)
                binding.etStartOpen.text = tempEt.editableText
            },
            hour,
            minutes,
            true
        ).show()
    }

    private fun getCityFromAutocomplete(city: String): City? {
        if (isSellectAutocomplete == true) {
            val textCity = city.split(", ")

            val tempCity = listCity.filter {
                it.city_name.contains(textCity[1]) ?: false
            }

            for (i in tempCity.indices) {
                val matchedCity = tempCity[i].city_name ?: ""
                if (tempCity[i].city_name == matchedCity) {
                    return tempCity[i]
                }
            }
        }
        return null
    }

    private fun getCloseTime() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)
        TimePickerDialog(
            binding.root.context,
            { timePicker, selectedHour, selectedMinute ->
                val finalTimeTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                tempEt.setText(finalTimeTime)
                binding.etEndOpen.text = tempEt.editableText
            },
            hour,
            minutes,
            true
        ).show()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            viewModelStore,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(AccountViewModel::class.java)
    }

    private fun setupROViewmodel() {
        ROviewmodel = ViewModelProvider(
            viewModelStore,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.apiRO))
        ).get(ROViewModel::class.java)
    }

    private fun onAlertDialog() {
        //Instantiate builder variable
        val builder = AlertDialog.Builder(binding.root.context)
        // set title
        builder.setTitle("Ambil gambar")
        //set content area
        builder.setMessage("Silahkan pilih gambar produk kamu")
        builder.setPositiveButton(
            "Dari Galery"
        ) { dialog, id ->
            // User clicked Update Now button
            chooseImageGallery()
        }

        builder.setNegativeButton(
            "Dari Kamera"
        ) { dialog, id ->
            // User cancelled the dialog
            chooseFromCamera()
        }
        builder.show()
    }

    private fun setupObserver() {
        viewModel.postShopInfo(
            token,
            fotoShop,
            idSellerAccount,
            nameShop,
            noTelpSeller,
            noWhatsappShop,
            isPickup,
            isDelivery,
            addressShop,
            postalCode,
            isTwentyFourHours,
            openTime,
            closeTime,
            cityId, provinceId, province, cityName, postalCodeRO

        ).observe(viewLifecycleOwner, {
            it.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        when (resource.data?.body()?.success) {
                            true -> {
                                accountRegistrationActivity.hideProgress()
                                accountRegistrationActivity.nextFragment()
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
                        accountRegistrationActivity.hideProgress()
                        Toast.makeText(binding.root.context, resource.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        })
    }

    private fun setupROObserver() {
        ROviewmodel.citiesResults.observe(viewLifecycleOwner, { response ->
            when (response) {
                is ResourcePagination.Success -> {
                    response.data.let { cityResponse ->
                        accountRegistrationActivity.hideProgress()
                        initSpinner(cityResponse?.rajaongkir?.results ?: return@observe)
                    }
                }

                is ResourcePagination.Loading -> {
                    accountRegistrationActivity.showProgress()
                }

                is ResourcePagination.Error -> {
//                    hideProgress()
                    Toast.makeText(
                        binding.root.context,
                        response.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

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
    }

    fun alertDialogSubmit() {
        val builder = AlertDialog.Builder(binding.root.context)
        // set title
        builder.setTitle("Kirim Data Diri?")
        //set content area
        builder.setMessage("Pastikan data kamu sudah benar")
        builder.setPositiveButton(
            "Kirim"
        ) { dialog, id ->
            // User clicked Update Now button
            setupData()
        }

        builder.setNegativeButton(
            "Kembali"
        ) { dialog, id ->
            // User cancelled the dialog

        }
        builder.show()
    }

    private fun setupData() {
        if (getCityFromAutocomplete(binding.originLocation.text.toString()) == null) {
            binding.originLocation.error = "Isi sesuai pilihan"
        }
        if (binding.etNameShop.text.toString().isEmpty() || binding.etShopTelpNumber.text.toString()
                .isEmpty() || binding.etWhatsappNumber.text.toString()
                .isEmpty() || binding.etAddressShop.text.toString()
                .isEmpty() || binding.etPostalCode.text.toString()
                .isEmpty() || tempPickup == "" || tempDelivery == "" || binding.txtFotoShopNameFile.text.toString() == ""
        ) {
            Toast.makeText(
                binding.root.context,
                "Harap isi data yang masih kosong",
                Toast.LENGTH_SHORT
            ).show()
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
                Log.d("TEMPCITY", tempCity.toString())
                cityId =
                    tempCity?.city_id.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                provinceId =
                    tempCity?.province_id.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                province =
                    tempCity?.province.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                cityName =
                    tempCity?.city_name.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                postalCodeRO =
                    tempCity?.postal_code.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                nameShop = binding.etNameShop.text.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                noTelpSeller = binding.etShopTelpNumber.text.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                noWhatsappShop = binding.etWhatsappNumber.text.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                isPickup = tempPickup.toRequestBody("text/plain".toMediaTypeOrNull())
                isDelivery = tempDelivery.toRequestBody("text/plain".toMediaTypeOrNull())

                //freeOngkirLimitKm = tempFreeDelivery.toRequestBody("text/plain".toMediaTypeOrNull())

                addressShop = binding.etAddressShop.text.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                postalCode = binding.etPostalCode.text.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                // check box time
                var tempIsTwentyFourHour = binding.checkBoxTime.isChecked
                isTwentyFourHours =
                    tempIsTwentyFourHour.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                openTime = binding.etStartOpen.text.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                closeTime = binding.etEndOpen.text.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())

                imgShopRequestFile = shopImg.asRequestBody("image/jpg".toMediaTypeOrNull())
                fotoShop = MultipartBody.Part.createFormData(
                    FILE_SHOP,
                    shopImg.name,
                    imgShopRequestFile
                )
                if (tempCity != null) {
                    setupObserver()
                } else {
                    binding.originLocation.error = "Harap isi sesuai pilihan"
                }
            }
        }
    }

    private fun chooseImageGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PermissionChecker.checkSelfPermission(
                    binding.root.context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PermissionChecker.PERMISSION_DENIED || PermissionChecker.checkSelfPermission(
                    binding.root.context, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PermissionChecker.PERMISSION_DENIED
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
        shopImg = getPhotoFile(FILE_SHOP)
        val providerFile = FileProvider.getUriForFile(
            binding.root.context,
            "com.aslilokal.mitra.fileprovider",
            shopImg
        )
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerFile)
        if (takePhotoIntent.resolveActivity(binding.root.context.packageManager) != null) {
            startActivityForResult(takePhotoIntent, REQUEST_CODE)
        } else {
            Toast.makeText(activity, "Camera ga bisa dibuka nih", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getPhotoFile(fileName: String): File {
        val directoryStorage =
            binding.root.context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", directoryStorage)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            shopImg.let { imageFile ->
                lifecycleScope.launch {
                    shopImg = Compressor.compress(binding.root.context, imageFile)
                    binding.txtFotoShopNameFile.text = imageFile.name.toString()
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
        if (requestCode == IMAGE_CHOOSE && resultCode == Activity.RESULT_OK) {
            shopImg = File(getFileUri(data?.data))
            shopImg.let { imageFile ->
                lifecycleScope.launch {
                    shopImg = Compressor.compress(binding.root.context, imageFile)
                    binding.txtFotoShopNameFile.text = imageFile.name.toString()
                }
            }
        }
    }

    private fun getFileUri(uri: Uri?): String {
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? =
            uri?.let { result ->
                binding.root.context.contentResolver.query(
                    result,
                    filePathColumn,
                    null,
                    null,
                    null
                )
            }
        cursor?.moveToFirst()
        val columnIndex: Int = cursor!!.getColumnIndexOrThrow(filePathColumn[0])
        val path: String = cursor.getString(columnIndex)
        cursor.close()
        return path
    }
}