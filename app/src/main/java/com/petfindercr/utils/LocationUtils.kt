package com.petfindercr.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@SuppressLint("MissingPermission")
suspend fun getLastKnownLocation(context: Context): Location? {
    val client = LocationServices.getFusedLocationProviderClient(context)
    return suspendCancellableCoroutine { cont ->
        val tokenSource = CancellationTokenSource()
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token)
            .addOnSuccessListener { location -> cont.resume(location) }
            .addOnFailureListener { ex -> cont.resumeWithException(ex) }
        cont.invokeOnCancellation { tokenSource.cancel() }
    }
}
