package com.aslilokal.mitra.ui.kelola.edit

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
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.aslilokal.mitra.databinding.ActivityEditProductBinding
import com.aslilokal.mitra.model.data.api.ApiHelper
import com.aslilokal.mitra.model.data.api.RetrofitInstance
import com.aslilokal.mitra.model.remote.request.OneProduct
import com.aslilokal.mitra.utils.AslilokalDataStore
import com.aslilokal.mitra.utils.Constants.Companion.BUCKET_PRODUCT_URL
import com.aslilokal.mitra.utils.CustomFunction
import com.aslilokal.mitra.utils.Status
import com.aslilokal.mitra.viewmodel.AslilokalVMProviderFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
import java.util.*


class EditProductActivity : AppCompatActivity() {
    //    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private lateinit var binding: ActivityEditProductBinding
    private lateinit var viewModel: EditViewModel
    private lateinit var datastore : AslilokalDataStore
    private lateinit var foto: MultipartBody.Part

    // Untuk di update
    private lateinit var token: String
    private lateinit var nameProduct: String
    private lateinit var productCategory: String
    private lateinit var priceProduct: String
    private lateinit var productWeight: String
    private lateinit var priceServiceRange: String
    private lateinit var promoPrice: String
    private lateinit var descProduct: String
    private lateinit var imageProduct: String
    private var isAvailable: Boolean? = true

    // Passing data
    private lateinit var idProduct: String
    private lateinit var filePhoto: File
    private var FILE_NAME: String = "imgProductSellerUpdate"
    private val REQUEST_CODE = 13
    private lateinit var imageRequestFile: RequestBody

    companion object {
        private val IMAGE_CHOOSE = 1000
        private val PERMISSION_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)
        datastore = AslilokalDataStore(binding.root.context)
        idProduct = intent.getStringExtra("idProduct")!!

        showProgress()
        setupViewModel()

        runBlocking {
            token = datastore.read("TOKEN").toString()
        }

        setupGetObserver()

        binding.ubahTambah.setOnClickListener {
            if (isEmpty()) {
                Toast.makeText(this, "Ada data yang belum di isi nih...", Toast.LENGTH_SHORT).show()
            } else {
                showProgress()
                putData()
                setupPutProductObserver()
            }
        }

        binding.imageProduct.setOnClickListener {
            onAlertClickImage()
        }
    }

    private fun setupUI() {
        val listData = arrayListOf("Kuliner", "Jasa", "Sembako", "Fashion")
        val tempData = listData.filter { data ->
            data.toLowerCase(Locale.getDefault()) != productCategory
        }

        val finalData: ArrayList<String> = arrayListOf()
        finalData.add(productCategory.capitalize(Locale.getDefault()))

        for (i in tempData.indices) {
            finalData.add(tempData[i])
        }

        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item)
        for (i in finalData.indices) {
            adapter.add(finalData[i])
        }
        binding.productSpinner.adapter = adapter
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(EditViewModel::class.java)
    }

    private fun setupGetObserver() {
        viewModel.getOneProduct(
            token,
            idProduct
        ).observe(this, {
            it.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        hideProgress()
                        when (resource.data?.body()?.success) {
                            true -> {
                                setupData(resource.data.body()!!.result)
                                setupUI()
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

    private fun setupPutProductObserver() {
        var dataEdit = OneProduct(
            0,
            idProduct,
            "",
            descProduct,
            "",
            "",
            isAvailable ?: true,
            "",
            nameProduct,
            priceProduct,
            priceServiceRange,
            productCategory,
            productWeight.toInt(),
            null,
            null,
            promoPrice.toInt()
        )
        viewModel.putOneProduct(
            token,
            idProduct,
            dataEdit
        ).observe(this, {
            it.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        hideProgress()
                        when (resource.data?.body()?.success) {
                            true -> {
                                Toast.makeText(
                                    this,
                                    "Data sukses di ubah",
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

    private fun setupPutImage() {
        showProgress()
        viewModel.putOneImage(
            token,
            foto,
            imageProduct.toRequestBody("text/plain".toMediaTypeOrNull())
        ).observe(this, {
            it.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        hideProgress()
                        when (resource.data?.body()?.success) {
                            true -> {
                                hideProgress()
                                binding.root.context.cacheDir.deleteRecursively()
                                Toast.makeText(
                                    this,
                                    "Produk berhasil di update",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            false -> Toast.makeText(
                                this,
                                "Jaringan kamu lemah, coba ulang yah...",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    Status.ERROR -> {
                        Glide.with(binding.root)
                            .load(BUCKET_PRODUCT_URL + imageProduct)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .skipMemoryCache(true)
                            .priority(Priority.HIGH)
                            .into(binding.imageProduct)
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


    private fun isEmpty(): Boolean {
        return binding.etNameProduct.text.toString()
            .isEmpty() || binding.etHargaProduct.text.toString()
            .isEmpty() || binding.etBeratProduct.text.toString().isEmpty()
    }

    private fun putData() {
        priceServiceRange = ""

        productCategory = binding.productSpinner.selectedItem.toString().toLowerCase()
//        Toast.makeText(this, productCategory, Toast.LENGTH_SHORT).show()
        nameProduct = binding.etNameProduct.text.toString()
        productWeight = binding.etBeratProduct.cleanIntValue.toString()
        priceProduct = binding.etHargaProduct.cleanIntValue.toString()
        descProduct = binding.etDeskripsi.text.toString()
        promoPrice = if (binding.etPromosi.cleanIntValue.toString().isEmpty()) {
            ""
        } else {
            binding.etPromosi.cleanIntValue.toString()
        }

        isAvailable = binding.switchIsAvailable.isChecked
    }


    private fun setupData(response: OneProduct) {
        Glide.with(binding.root)
            .load(BUCKET_PRODUCT_URL + response.imgProduct)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .skipMemoryCache(true)
            .priority(Priority.HIGH)
            .into(binding.imageProduct)

        if (response.promoPrice != 0 || response.promoPrice != null) {
            binding.txtDeletePromotion.visibility = View.VISIBLE
        } else {
            binding.txtDeletePromotion.visibility = View.GONE
        }

        nameProduct = response.nameProduct
        productWeight = response.productWeight.toString()
        priceProduct = response.priceProduct
        descProduct = response.descProduct
        promoPrice = response.promoPrice.toString()
        isAvailable = response.isAvailable
        productCategory = response.productCategory
        imageProduct = response.imgProduct
        var tempEt = EditText(this)


        binding.etBeratProduct.setSeparator(".")
        binding.etBeratProduct.setDelimiter(false)
        binding.etBeratProduct.setDecimals(false)
        binding.etBeratProduct.setSpacing(false)

        binding.etHargaProduct.setCurrency(CurrencySymbols.INDONESIA)
        binding.etHargaProduct.setDelimiter(false)
        binding.etHargaProduct.setDecimals(false)
        binding.etHargaProduct.setSpacing(false)
        binding.etHargaProduct.setSeparator(".")

        binding.etPromosi.setCurrency(CurrencySymbols.INDONESIA)
        binding.etPromosi.setDelimiter(false)
        binding.etPromosi.setDecimals(false)
        binding.etPromosi.setSpacing(false)
        binding.etPromosi.setSeparator(".")


        tempEt.setText(nameProduct)
        binding.etNameProduct.text = tempEt.editableText
        tempEt.setText(productWeight)
        binding.etBeratProduct.text = tempEt.editableText
        tempEt.setText(priceProduct)
        binding.etHargaProduct.text = tempEt.editableText
        tempEt.setText(descProduct)
        binding.etDeskripsi.text = tempEt.editableText
        tempEt.setText(promoPrice)
        binding.etPromosi.text = tempEt.editableText
        binding.switchIsAvailable.isChecked = isAvailable as Boolean

        binding.txtDeletePromotion.setOnClickListener {
            promoPrice = 0.toString()
            tempEt.setText(promoPrice)
            binding.etPromosi.text = tempEt.editableText
            binding.txtDeletePromotion.visibility = View.GONE
        }

        binding.etPromosi.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.etPromosi.cleanIntValue > binding.etHargaProduct.cleanIntValue) {
                    binding.ubahTambah.isEnabled = false
                    binding.ubahTambah.isActivated = false
                    Toast.makeText(
                        binding.root.context,
                        "Harga promosi tidak boleh lebih besar dari harga awal",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (binding.etPromosi.equals("") || binding.etPromosi.cleanIntValue == 0) {
                    binding.ubahTambah.isEnabled = true
                    binding.ubahTambah.isActivated = true
                } else {
                    binding.ubahTambah.isEnabled = true
                    binding.ubahTambah.isActivated = true
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private fun onAlertTakeImage() {
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

    private fun onAlertClickImage() {
        val builder = AlertDialog.Builder(binding.root.context)
        builder.setTitle("Kelola Gambar")
        builder.setMessage("Mau apa?")
        builder.setPositiveButton("Ganti gambar") { dialog, id ->
            onAlertTakeImage()
        }

        builder.setNegativeButton("Lihat gambar") { dialog, id ->
            //Toast.makeText(this, imageProduct, Toast.LENGTH_SHORT).show()
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
            startActivityForResult(takePhotoIntent, REQUEST_CODE)
        } else {
            Toast.makeText(this, "Camera ga bisa dibuka nih", Toast.LENGTH_SHORT).show()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            filePhoto.let { imageFile ->
                lifecycleScope.launch {
                    filePhoto = Compressor.compress(binding.root.context, imageFile)
                    val takenPhoto =
                        CustomFunction().rotateBitmapOrientation(filePhoto.absolutePath)
                    binding.imageProduct.visibility = View.VISIBLE
                    binding.imageProduct.setImageBitmap(takenPhoto)
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
                    binding.imageProduct.visibility = View.VISIBLE
                    binding.imageProduct.setImageURI(data?.data)

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

    private fun getPhotoFile(fileName: String): File {
        val directoryStorage = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", directoryStorage)
    }

    private fun hideProgress() {
        binding.llProgressBar.progressbar.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun showProgress() {
        binding.llProgressBar.progressbar.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }
}