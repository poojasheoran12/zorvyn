package com.example.zorvyn.util

import androidx.fragment.app.FragmentActivity

private lateinit var globalActivity: FragmentActivity

fun setGlobalActivity(activity: FragmentActivity) {
    globalActivity = activity
}

actual fun getBiometricAuthenticator(): BiometricAuthenticator {
    return AndroidBiometricAuthenticator(globalActivity)
}
