package com.petfindercr.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun createImageUri(context: Context): Uri {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val file = File(context.externalCacheDir, "IMG_$timestamp.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
}
