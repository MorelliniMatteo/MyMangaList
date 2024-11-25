package com.example.mymangalist.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore

/**
 * Saves a Bitmap as a URI in the MediaStore.
 * @param context The context to access the ContentResolver.
 * @param bitmap The bitmap to save.
 * @return The URI of the saved image, or null if an error occurred.
 */
fun saveBitmapAsUri(context: Context, bitmap: Bitmap): Uri? {
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    }

    val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    uri?.let {
        context.contentResolver.openOutputStream(it)?.use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        }
    }
    return uri
}
