package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.InputStream

object ImageUtils {

    fun uriToBitmap(context: Context, uri: Uri, maxDimension: Int = 1600): Bitmap? {
        return try {
            // First decode bounds
            var inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // Calculate inSampleSize
            val (width, height) = options.outWidth to options.outHeight
            var sampleSize = 1
            if (width > maxDimension || height > maxDimension) {
                val halfWidth = width / 2
                val halfHeight = height / 2
                while ((halfWidth / sampleSize) >= maxDimension && (halfHeight / sampleSize) >= maxDimension) {
                    sampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            inputStream = context.contentResolver.openInputStream(uri)
            val rawBitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            inputStream?.close()

            if (rawBitmap == null) return null

            // Read EXIF orientation
            val exifStream = context.contentResolver.openInputStream(uri)
            val orientation = exifStream?.let {
                val exif = ExifInterface(it)
                val orient = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                it.close()
                orient
            } ?: ExifInterface.ORIENTATION_NORMAL

            rotateBitmap(rawBitmap, orientation)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return bitmap
        }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated != bitmap) {
            bitmap.recycle()
        }
        return rotated
    }

    fun bitmapToJpegBytes(bitmap: Bitmap, quality: Int = 85): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        return stream.toByteArray()
    }

    fun bitmapToBase64(bitmap: Bitmap, quality: Int = 85): String {
        val bytes = bitmapToJpegBytes(bitmap, quality)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun createMultipartFromBitmap(bitmap: Bitmap, partName: String = "file"): MultipartBody.Part {
        val bytes = bitmapToJpegBytes(bitmap, 85)
        val requestBody = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, bytes.size)
        return MultipartBody.Part.createFormData(partName, "input_image.jpg", requestBody)
    }
}
