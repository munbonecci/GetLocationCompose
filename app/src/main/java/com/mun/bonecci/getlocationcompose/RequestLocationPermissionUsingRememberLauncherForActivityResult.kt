package com.mun.bonecci.getlocationcompose

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * Composable function to request location permissions and handle different scenarios.
 *
 * @param onPermissionGranted Callback to be executed when all requested permissions are granted.
 * @param onPermissionDenied Callback to be executed when any requested permission is denied.
 */
@Composable
fun RequestLocationPermissionUsingRememberLauncherForActivityResult(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) {
    // 1. Create a stateful launcher using rememberLauncherForActivityResult
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        // 2. Check if all requested permissions are granted
        val arePermissionsGranted = permissionsMap.values.reduce { acc, next ->
            acc && next
        }

        // 3. Invoke the appropriate callback based on the permission result
        if (arePermissionsGranted) {
            onPermissionGranted.invoke()
        } else {
            onPermissionDenied.invoke()
        }
    }

    // 4. Launch the permission request on composition
    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
}