package com.example.kodomo_album.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ImageUtils {
    
    private const val MAX_IMAGE_SIZE = 1080
    private const val COMPRESSION_QUALITY = 85
    
    fun compressImage(context: Context, imageUri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            val rotatedBitmap = rotateImageIfRequired(context, bitmap, imageUri)
            val resizedBitmap = resizeBitmap(rotatedBitmap, MAX_IMAGE_SIZE)
            
            val outputFile = createTempFile(context, "compressed_image", ".jpg")
            saveBitmapToFile(resizedBitmap, outputFile, COMPRESSION_QUALITY)
            
            resizedBitmap.recycle()
            if (rotatedBitmap != bitmap) {
                rotatedBitmap.recycle()
            }
            bitmap.recycle()
            
            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun rotateImageIfRequired(context: Context, img: Bitmap, selectedImage: Uri): Bitmap {
        val input = context.contentResolver.openInputStream(selectedImage)
        val ei = if (input != null) ExifInterface(input) else return img
        
        return when (ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270f)
            else -> img
        }
    }
    
    private fun rotateImage(img: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree)
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }
    
    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= maxSize && height <= maxSize) {
            return bitmap
        }
        
        val scale = if (width > height) {
            maxSize.toFloat() / width
        } else {
            maxSize.toFloat() / height
        }
        
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    private fun saveBitmapToFile(bitmap: Bitmap, file: File, quality: Int) {
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    
    private fun createTempFile(context: Context, prefix: String, suffix: String): File {
        return File.createTempFile(prefix, suffix, context.cacheDir)
    }
    
    fun generateThumbnail(context: Context, imageUri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            val thumbnailBitmap = resizeBitmap(bitmap, 200)
            val outputFile = createTempFile(context, "thumbnail", ".jpg")
            saveBitmapToFile(thumbnailBitmap, outputFile, 70)
            
            thumbnailBitmap.recycle()
            bitmap.recycle()
            
            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}