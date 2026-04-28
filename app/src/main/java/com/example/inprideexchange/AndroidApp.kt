package com.example.inprideexchange

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AndroidApp : Application(){
    override fun onTerminate() {
        super.onTerminate()
        Log.d("AndroidApp", "onTerminate: Called")
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.d("AndroidApp", "onLowMemory: Called")
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >0){
            Log.d("AndroidApp", "onTrimMemory: Called $level")
        }else{
            Log.d("AndroidApp", "onTrimMemory: Called at $level")
        }
    }
}
