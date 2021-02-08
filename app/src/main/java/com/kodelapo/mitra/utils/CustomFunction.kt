package com.kodelapo.mitra.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import okio.IOException

class CustomFunction {
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
}