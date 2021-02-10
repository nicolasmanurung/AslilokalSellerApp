package com.kodelapo.mitra.ui.kelola.tambah

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.kodelapo.mitra.R
import com.kodelapo.mitra.databinding.ActivityTambahProductBinding
import com.kodelapo.mitra.model.data.api.ApiHelper
import com.kodelapo.mitra.model.data.api.RetrofitInstance
import com.kodelapo.mitra.utils.CustomFunction
import com.kodelapo.mitra.utils.KodelapoDataStore
import com.kodelapo.mitra.utils.Status
import com.kodelapo.mitra.viewmodel.KodelapoViewModelProviderFactory
import id.zelory.compressor.Compressor
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.abhinay.input.CurrencySymbols
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File


class TambahProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTambahProductBinding
    private var dataStore = KodelapoDataStore(this)
    private lateinit var filePhoto: File
    private var FILE_NAME: String? = "imgProduct"
    private val REQUEST_CODE = 13
    private lateinit var imageRequestFile: RequestBody
    private lateinit var viewModel: TambahProductViewModel

    // Untuk di upload
    private lateinit var foto: MultipartBody.Part
    private lateinit var productCategory: RequestBody
    private lateinit var token: String
    private lateinit var idSellerAccount: RequestBody
    private lateinit var nameProduct: RequestBody
    private lateinit var priceProduct: RequestBody
    private lateinit var productWeight: RequestBody
    private lateinit var descProduct: RequestBody
    private lateinit var isAvailable: RequestBody
    private lateinit var promoPrice: RequestBody


    companion object {
        private val IMAGE_CHOOSE = 1000
        private val PERMISSION_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTambahProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ArrayAdapter.createFromResource(
            this,
            R.array.jenis_product,
            android.R.layout.simple_dropdown_item_1line
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.productSpinner.adapter = adapter
        }

        binding.etBeratProduct.setSeparator(".")
        binding.etBeratProduct.setDelimiter(false)
        binding.etBeratProduct.setDecimals(false)
        binding.etBeratProduct.setSpacing(false)

        binding.etHargaProduct.setCurrency(CurrencySymbols.INDONESIA)
        binding.etHargaProduct.setDelimiter(false)
        binding.etHargaProduct.setDecimals(false)
        binding.etHargaProduct.setSpacing(false)
        binding.etHargaProduct.setSeparator(".")

        hideProgress()
        binding.button.setOnClickListener {
            onAlertDialog()
        }

        runBlocking {
            val username = dataStore.read("USERNAME").toString()
            token = dataStore.read("TOKEN").toString()
            idSellerAccount = username.toRequestBody("text/plain".toMediaTypeOrNull())
        }

        binding.buttonTambah.setOnClickListener {
            setupData()
        }
        setupViewModel()
    }

    private fun setupData() {
        productCategory =
            binding.productSpinner.selectedItem.toString().toLowerCase()
                .toRequestBody("text/plain".toMediaTypeOrNull())
        if (binding.etNameProduct.text.toString()
                .isEmpty() || binding.etHargaProduct.text.toString()
                .isEmpty() || binding.etBeratProduct.text.toString()
                .isEmpty() || binding.gambarProduct.drawable == null
        ) {
            Toast.makeText(this, "Silahkan isi data yang kosong yah", Toast.LENGTH_SHORT).show()
        } else {
            nameProduct = binding.etNameProduct.text.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())
            priceProduct =
                binding.etHargaProduct.cleanIntValue.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())
            productWeight =
                binding.etBeratProduct.cleanIntValue.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())
            descProduct =
                binding.etDeskripsi.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            isAvailable = "true".toRequestBody("text/plain".toMediaTypeOrNull())
            promoPrice = "".toRequestBody("text/plain".toMediaTypeOrNull())

            imageRequestFile = filePhoto.asRequestBody("image/jpg".toMediaTypeOrNull())
            foto = MultipartBody.Part.createFormData(
                FILE_NAME.toString(),
                filePhoto.name,
                imageRequestFile
            )
            setupObservers()
            showProgress()
        }
    }


    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            KodelapoViewModelProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(TambahProductViewModel::class.java)
    }

    private fun setupObservers() {
        viewModel.postProduct(
            token,
            foto,
            idSellerAccount,
            nameProduct,
            productCategory,
            priceProduct,
            productWeight,
            descProduct,
            isAvailable,
            promoPrice
        ).observe(this, {
            it.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        hideProgress()
                        when (resource.data?.body()?.success) {
                            true -> {
                                Toast.makeText(
                                    this,
                                    "Produk berhasil di tambahkan",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onBackPressed()
                                finish()
                            }
                            false -> Toast.makeText(
                                this,
                                "Jaringan kamu lemah, coba ulang yah...",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    Status.ERROR -> {
                        hideProgress()
                        Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                    }
                    Status.LOADING -> {
                        showProgress()
                    }
                }
            }
        })
    }

    fun onAlertDialog() {
        //Instantiate builder variable
        val builder = AlertDialog.Builder(binding.root.context)
        // set title
        builder.setTitle("Ambil gambar")
        //set content area
        builder.setMessage("Silahkan pilih gambar produk kamu")
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

    @SuppressLint("QueryPermissionsNeeded")
    private fun chooseFromCamera() {
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        filePhoto = getPhotoFile(FILE_NAME.toString())
        val providerFile =
            FileProvider.getUriForFile(this, "com.kodelapo.mitra.fileprovider", filePhoto)
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerFile)
        if (takePhotoIntent.resolveActivity(this.packageManager) != null) {
            startActivityForResult(takePhotoIntent, REQUEST_CODE)
        } else {
            Toast.makeText(this, "Camera ga bisa dibuka nih", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getPhotoFile(fileName: String): File {
        val directoryStorage = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", directoryStorage)
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
                    Toast.makeText(this, "Izin di tolak", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            //val takenPhoto = BitmapFactory.decodeFile(filePhoto.absolutePath)
            filePhoto.let { imageFile ->
                lifecycleScope.launch {
                    filePhoto = Compressor.compress(binding.root.context, imageFile)
                    val takenPhoto =
                        CustomFunction().rotateBitmapOrientation(filePhoto.absolutePath)
//            var part = MultipartBody.Part.createFormData("pic", filePhoto.absolutePath, )
                    binding.gambarProduct.visibility = View.VISIBLE
                    binding.gambarProduct.setImageBitmap(takenPhoto)
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
                    binding.gambarProduct.visibility = View.VISIBLE
                    binding.gambarProduct.setImageURI(data?.data)
                }
            }
        }
    }

    private fun getFileUri(uri: Uri?): String? {
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? =
            uri?.let { contentResolver.query(it, filePathColumn, null, null, null) }
        cursor?.moveToFirst()
        val columnIndex: Int = cursor!!.getColumnIndex(filePathColumn[0])
        val path: String = cursor.getString(columnIndex)
        cursor.close()
        return path
    }

    private fun hideProgress() {
        binding.llProgressBar.progressbar.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private fun showProgress() {
        binding.llProgressBar.progressbar.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }
}