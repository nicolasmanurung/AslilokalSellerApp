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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.aslilokal.mitra.R
import com.aslilokal.mitra.databinding.FragmentShopInfoBinding
import com.aslilokal.mitra.model.data.api.ApiHelper
import com.aslilokal.mitra.model.data.api.RetrofitInstance
import com.aslilokal.mitra.ui.account.AccountViewModel
import com.aslilokal.mitra.utils.KodelapoDataStore
import com.aslilokal.mitra.utils.Status
import com.aslilokal.mitra.viewmodel.KodelapoViewModelProviderFactory
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
    private lateinit var datastore: KodelapoDataStore
    private lateinit var tempEt: EditText

    private lateinit var imgShopRequestFile: RequestBody

    private lateinit var fotoShop: MultipartBody.Part

    // Data from
    private lateinit var token: String
    private lateinit var idSellerAccount: RequestBody
    private lateinit var nameShop: RequestBody
    private lateinit var noTelpSeller: RequestBody
    private lateinit var noWhatsappShop: RequestBody
    private lateinit var isPickup: RequestBody
    private lateinit var isDelivery: RequestBody
    private lateinit var freeOngkirLimitKm: RequestBody
    private lateinit var addressShop: RequestBody
    private lateinit var isTwentyFourHours: RequestBody
    private lateinit var openTime: RequestBody
    private lateinit var closeTime: RequestBody
    private lateinit var postalCode: RequestBody

    // Temp Var
    private var tempDelivery: String = ""
    private var tempPickup: String = ""
    private var tempFreeDelivery: String = ""

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
        tempEt = EditText(binding.root.context)
        datastore = KodelapoDataStore(binding.root.context)

        runBlocking {
            token = datastore.read("TOKEN").toString()
            val username = datastore.read("USERNAME").toString()
            idSellerAccount = username.toRequestBody("text/plain".toMediaTypeOrNull())
        }

        setupViewModel()

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
        tempFreeDelivery =
            binding.freeOngkirSpinner.selectedItem.toString().toLowerCase(Locale.getDefault())

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

        tempFreeDelivery = if (tempFreeDelivery == "ya") {
            "true"
        } else {
            "false"
        }

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
            KodelapoViewModelProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(AccountViewModel::class.java)
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
            freeOngkirLimitKm,
            addressShop,
            postalCode,
            isTwentyFourHours,
            openTime,
            closeTime
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
        if (binding.etNameShop.text.toString().isEmpty() || binding.etShopTelpNumber.text.toString()
                .isEmpty() || binding.etWhatsappNumber.text.toString()
                .isEmpty() || binding.etAddressShop.text.toString()
                .isEmpty() || binding.etPostalCode.text.toString()
                .isEmpty() || tempPickup == "" || tempDelivery == "" || tempFreeDelivery == "" || binding.txtFotoShopNameFile.text.toString() == ""
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
                nameShop = binding.etNameShop.text.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                noTelpSeller = binding.etShopTelpNumber.text.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                noWhatsappShop = binding.etWhatsappNumber.text.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                isPickup = tempPickup.toRequestBody("text/plain".toMediaTypeOrNull())
                isDelivery = tempDelivery.toRequestBody("text/plain".toMediaTypeOrNull())
                freeOngkirLimitKm = tempFreeDelivery.toRequestBody("text/plain".toMediaTypeOrNull())
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
                setupObserver()
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