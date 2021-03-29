package com.aslilokal.mitra.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import com.parassidhu.simpledate.toDateTimeYYInDigits
import okio.IOException
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class CustomFunction {
    private lateinit var simpleDateFormat: SimpleDateFormat
    fun rotateBitmapOrientation(photoFilePath: String?): Bitmap? {
        // Create and configure BitmapFactory
        val bounds = BitmapFactory.Options()
        bounds.inJustDecodeBounds = true
        BitmapFactory.decodeFile(photoFilePath, bounds)
        val opts = BitmapFactory.Options()
        val bm = BitmapFactory.decodeFile(photoFilePath, opts)
        // Read EXIF Data
        var exif: ExifInterface? = null
        try {
            exif = photoFilePath?.let { ExifInterface(it) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val orientString: String? = exif?.getAttribute(ExifInterface.TAG_ORIENTATION)
        val orientation = orientString?.toInt() ?: ExifInterface.ORIENTATION_NORMAL
        var rotationAngle = 0
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270
        // Rotate Bitmap
        val matrix = Matrix()
        matrix.setRotate(rotationAngle.toFloat(), bm.width.toFloat() / 2, bm.height.toFloat() / 2)
        // Return result
        return Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true)
    }

    fun isoTimeToDateMonth(date: String): String {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:s.S'Z'", Locale.getDefault())
        val date: Date = format.parse(date)
        return date.toDateTimeYYInDigits()
    }

    fun isoTimeToDate(date: String): String {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:s.S'Z'", Locale.getDefault())
        val date: Date = format.parse(date)
        return date.toDateAndMonth()
    }

    fun isoTimeToSlashNormalDate(date: String): String {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:s.S'Z'", Locale.getDefault())
        val date: Date = format.parse(date)
        return date.toDateMonthYear()
    }

    fun normalDateToIsoTime(date: String): String{
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date: Date = format.parse(date)
        return date.toIsoTime()
    }

//    fun formatRupiah(number: Double): String? {
//        val localeID = Locale("in", "ID")
//        val formatRupiah: NumberFormat = NumberFormat.getCurrencyInstance(localeID)
//        return formatRupiah.format(number)
//    }

    fun formatRupiah(number: Double): String? {
        val localeID = Locale("in", "ID")
        val formatRupiah: NumberFormat = NumberFormat.getCurrencyInstance(localeID)
        var finalNumber = formatRupiah.format(number)
        return finalNumber.replace(",00", "")
    }

    fun formateMonthAndYear(date: String): String {
        val format = SimpleDateFormat("M/yyyy", Locale.getDefault())
        val date: Date = format.parse(date)
        return date.toMonthAndYear()
    }

    private fun Date?.toMonthAndYear(): String {
        val pattern = "MMM yyyy"
        return dateAsString(this, pattern)
    }

    private fun Date?.toDateAndMonth(): String {
        val pattern = "dd/MM"
        return dateAsString(this, pattern)
    }

    private fun Date?.toDateMonthYear(): String {
        val pattern = "dd-MM-yyyy"
        return dateAsString(this, pattern)
    }

    private fun Date?.toIsoTime(): String{
        val pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
        return dateAsString(this, pattern)
    }
// ---------------------------------------------------------

    private fun dateAsString(date: Date?, pattern: String): String {
        simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        if (date != null)
            return simpleDateFormat.format(date)
        else
            throw NullPointerException("Date can't be null")
    }

}