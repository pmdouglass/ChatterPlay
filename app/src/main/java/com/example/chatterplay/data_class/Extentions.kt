package com.example.chatterplay.data_class

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import io.ktor.utils.io.errors.IOException
import java.io.ByteArrayOutputStream
import java.io.InputStream

@Throws(IOException::class)
fun Uri.uriToByteArray(context: Context) =
    context.contentResolver.openInputStream(this)?.use { it.buffered().readBytes() }

fun Uri.toByteArray(context: Context): ByteArray {
    val inputStream: InputStream? = context.contentResolver.openInputStream(this)
    val bitmap = BitmapFactory.decodeStream(inputStream)
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
    return byteArrayOutputStream.toByteArray()
}