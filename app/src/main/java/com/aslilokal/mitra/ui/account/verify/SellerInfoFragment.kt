package com.aslilokal.mitra.ui.account.verify

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker.PERMISSION_DENIED
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.aslilokal.mitra.databinding.FragmentSellerInfoBinding
import com.aslilokal.mitra.model.data.api.ApiHelper
import com.aslilokal.mitra.model.data.api.RetrofitInstance
import com.aslilokal.mitra.ui.account.AccountViewModel
import com.aslilokal.mitra.utils.AslilokalDataStore
import com.aslilokal.mitra.utils.Status
import com.aslilokal.mitra.viewmodel.AslilokalVMProviderFactory
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

class SellerInfoFragment : Fragment() {

    private var _binding: FragmentSellerInfoBinding? = null
    private val binding get() = _binding!!
    private lateinit var selfPhoto: File
    private lateinit var ktpPhoto: File
    private var FILE_SELF = "imgSelfSeller"
    private val FILE_KTP = "ktpImgSeller"
    private val REQUEST_CODE = 13

    private lateinit var viewModel: AccountViewModel
    private lateinit var dataStore: AslilokalDataStore

    private lateinit var imgSelfRequestFile: RequestBody
    private lateinit var imgKtpRequestFile: RequestBody

    private lateinit var fotoSelf: MultipartBody.Part
    private lateinit var fotoKtp: MultipartBody.Part

    // Data form
    private lateinit var token: String
    private lateinit var idSellerAccount: RequestBody
    private lateinit var namaLengkap: RequestBody
    private lateinit var ktpNumber: RequestBody
    private lateinit var nomorTelepon: RequestBody
    private lateinit var alamatDomisili: RequestBody
    private lateinit var tanggalLahir: RequestBody
    private lateinit var ovoNumber: RequestBody
    private lateinit var danaNumber: RequestBody
    private lateinit var gopayNumber: RequestBody

    private lateinit var accountRegistrationActivity: AccountRegistrationActivity

    //Status IMG pick
    private var STATUS_IMG_PICK = "statusimgpick"

    companion object {
        private const val IMAGE_CHOOSE = 1000
        private const val PERMISSION_CODE = 1001
    }
//    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSellerInfoBinding.inflate(inflater, container, false)
        accountRegistrationActivity = activity as AccountRegistrationActivity
        dataStore = AslilokalDataStore(binding.root.context)
        setupViewModel()


        runBlocking {
            token = dataStore.read("TOKEN").toString()
            val username = dataStore.read("USERNAME").toString()
            idSellerAccount = username.toRequestBody("text/plain".toMediaTypeOrNull())
        }

        accountRegistrationActivity.hideProgress()

        binding.btnPickKtp.setOnClickListener {
            STATUS_IMG_PICK = "imgKtp"
            onAlertDialog()
        }

        binding.btnPickSelf.setOnClickListener {
            STATUS_IMG_PICK = "imgSelf"
            onAlertDialog()
        }

        binding.nextFragmentBtn.setOnClickListener {
            alertDialogSubmit()
        }

        binding.birthDate.setOnClickListener {
            getDate()
        }

        return binding.root
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            viewModelStore,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(AccountViewModel::class.java)
    }

    private fun setupObserver() {
        viewModel.postSellerInfo(
            token,
            fotoSelf,
            fotoKtp,
            idSellerAccount,
            namaLengkap,
            ktpNumber,
            nomorTelepon,
            alamatDomisili,
            tanggalLahir,
            ovoNumber,
            danaNumber,
            gopayNumber
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

                    Status.ERROR -> {
                        accountRegistrationActivity.hideProgress()
                        Toast.makeText(binding.root.context, resource.message, Toast.LENGTH_SHORT)
                            .show()
                    }

                    Status.LOADING -> {
                        accountRegistrationActivity.showProgress()
                    }
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun getDate() {
        val callendar = Calendar.getInstance()
        val year = callendar.get(Calendar.YEAR)
        val month = callendar.get(Calendar.MONTH)
        val day = callendar.get(Calendar.DAY_OF_MONTH)

        val date = DatePickerDialog(
            binding.root.context,
            { view, year, month, dayOfMonth ->
                val finalMonth = month + 1
                binding.txtBirthDate.text = "$dayOfMonth/$finalMonth/$year"
            },
            year,
            month,
            day
        )
        date.show()
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
        if (binding.etNameSeller.text.toString().isEmpty() || binding.etNomorKtp.text.toString()
                .isEmpty() || binding.etAddressSeller.text.toString()
                .isEmpty() || binding.txtBirthDate.text.toString() == "00/00/0000" || binding.txtFotoKtpNameFile.text.toString() == "Foto KTP" ||
            binding.txtFotoSelfNameFile.text.toString() == ""

        ) {
            Toast.makeText(activity, "Silahkan isi data yang kosong yah", Toast.LENGTH_SHORT).show()
        } else {
            if (binding.etOvoNumber.text.toString()
                    .isEmpty() && binding.etDanaNumber.text.toString()
                    .isEmpty() && binding.etGopayNumber.text.toString().isEmpty()
            ) {
                binding.etOvoNumber.error = "Harap isi salah satu"
                binding.etDanaNumber.error = "Harap isi salah satu"
                binding.etGopayNumber.error = "Harap isi salah satu"
            } else {
                //setup data
                namaLengkap = binding.etNameSeller.text.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())

                ktpNumber = binding.etNomorKtp.text.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())

                nomorTelepon = binding.etNoTelp.text.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())

                alamatDomisili = binding.etAddressSeller.text.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())

                tanggalLahir = binding.txtBirthDate.text.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())

                ovoNumber = binding.etOvoNumber.text.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())

                danaNumber = binding.etDanaNumber.text.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())

                gopayNumber = binding.etGopayNumber.text.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())

                imgSelfRequestFile = selfPhoto.asRequestBody("image/jpg".toMediaTypeOrNull())
                fotoSelf = MultipartBody.Part.createFormData(
                    FILE_SELF,
                    selfPhoto.name,
                    imgSelfRequestFile
                )

                imgKtpRequestFile = ktpPhoto.asRequestBody("image/jpg".toMediaTypeOrNull())
                fotoKtp = MultipartBody.Part.createFormData(
                    FILE_KTP,
                    ktpPhoto.name,
                    imgKtpRequestFile
                )
                setupObserver()
            }
        }
    }

    private fun chooseImageGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(
                    binding.root.context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PERMISSION_DENIED || checkSelfPermission(
                    binding.root.context, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PERMISSION_DENIED
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
        when (STATUS_IMG_PICK) {
            "imgSelf" -> {
                selfPhoto = getPhotoFile(FILE_SELF)
                val providerFile =
                    FileProvider.getUriForFile(
                        binding.root.context,
                        "com.aslilokal.mitra.fileprovider",
                        selfPhoto
                    )
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerFile)
            }

            "imgKtp" -> {
                ktpPhoto = getPhotoFile(FILE_SELF)
                val providerFile =
                    FileProvider.getUriForFile(
                        binding.root.context,
                        "com.aslilokal.mitra.fileprovider",
                        ktpPhoto
                    )
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerFile)
            }
        }
        Toast.makeText(activity, "Choose From Camera", Toast.LENGTH_SHORT).show()
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
        val imgStatus = STATUS_IMG_PICK
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            when (imgStatus) {
                "imgSelf" -> {
                    selfPhoto.let { imageFile ->
                        lifecycleScope.launch {
                            selfPhoto = Compressor.compress(binding.root.context, imageFile)
                            binding.txtFotoSelfNameFile.text = imageFile.name.toString()
                        }
                    }
                }
                "imgKtp" -> {
                    ktpPhoto.let { imageFile ->
                        lifecycleScope.launch {
                            ktpPhoto = Compressor.compress(binding.root.context, imageFile)
                            binding.txtFotoKtpNameFile.text = imageFile.name.toString()
                        }
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
        if (requestCode == IMAGE_CHOOSE && resultCode == RESULT_OK) {
            when (imgStatus) {
                "imgSelf" -> {
                    selfPhoto = File(getFileUri(data?.data))
                    selfPhoto.let { imageFile ->
                        lifecycleScope.launch {
                            selfPhoto = Compressor.compress(binding.root.context, imageFile)
                            binding.txtFotoSelfNameFile.text = imageFile.name.toString()
                        }
                    }
                }
                "imgKtp" -> {
                    ktpPhoto = File(getFileUri(data?.data))
                    ktpPhoto.let { imageFile ->
                        lifecycleScope.launch {
                            ktpPhoto = Compressor.compress(binding.root.context, imageFile)
                            binding.txtFotoKtpNameFile.text = imageFile.name.toString()
                        }
                    }
                }
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (grantResults.isNotEmpty() && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        chooseImageGallery()
                    }
                } else {
                    Toast.makeText(activity, "Izin di tolak", Toast.LENGTH_SHORT).show()
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