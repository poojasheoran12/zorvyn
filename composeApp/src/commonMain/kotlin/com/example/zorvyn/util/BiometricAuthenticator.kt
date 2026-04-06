package com.example.zorvyn.util

interface BiometricAuthenticator {
    fun authenticate(
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )
}

expect fun getBiometricAuthenticator(): BiometricAuthenticator
