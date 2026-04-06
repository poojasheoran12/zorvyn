package com.example.zorvyn.util

import platform.LocalAuthentication.*
import platform.Foundation.*
import platform.darwin.*

class IosBiometricAuthenticator : BiometricAuthenticator {
    override fun authenticate(
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val context = LAContext()
        val error = null 

        if (context.canEvaluatePolicy(LAPolicyDeviceOwnerAuthenticationWithBiometrics, null)) {
            context.evaluatePolicy(
                LAPolicyDeviceOwnerAuthenticationWithBiometrics,
                localizedReason = subtitle
            ) { success, nsError ->
                dispatch_async(dispatch_get_main_queue()) {
                    if (success) onSuccess()
                    else onError(nsError?.localizedDescription ?: "Authentication failed")
                }
            }
        } else {
            onError("Biometrics not available")
        }
    }
}

actual fun getBiometricAuthenticator(): BiometricAuthenticator {
    return IosBiometricAuthenticator()
}
