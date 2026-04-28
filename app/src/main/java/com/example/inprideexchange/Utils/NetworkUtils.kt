package com.example.inprideexchange.Utils


import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object NetworkUtils {

    fun isSlowNetwork(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return true
        val capabilities = cm.getNetworkCapabilities(network) ?: return true

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> false

            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

            else -> true
        }
    }
}