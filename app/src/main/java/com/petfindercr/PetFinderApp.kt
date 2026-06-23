package com.petfindercr

import android.app.Application
import com.petfindercr.utils.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PetFinderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
    }
}
