package com.mun.bonecci.getlocationcompose

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.mun.bonecci.getlocationcompose.ui.theme.GetLocationComposeTheme

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    /**
     * This function is the entry point of the Android application.
     *
     * @param savedInstanceState The saved state of the application.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up the Compose theme and UI components
        setContent {
            GetLocationComposeTheme {
                // Create a surface container using the background color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // State variables to manage location information and permission result text
                    var locationText by remember { mutableStateOf("No location obtained :(") }
                    var showPermissionResultText by remember { mutableStateOf(false) }
                    var permissionResultText by remember { mutableStateOf("Permission Granted...") }

                    // Request location permission using a Compose function
                    RequestLocationPermissionUsingRememberLauncherForActivityResult(
                        onPermissionGranted = {
                            // Callback when permission is granted
                            showPermissionResultText = true
                            permissionResultText = "Permission Granted..."
                            // Attempt to get the last known user location
                            getLastUserLocation(
                                onGetLastLocationSuccess = {
                                    locationText =
                                        "Location using LAST-LOCATION: LATITUDE: ${it.first}, LONGITUDE: ${it.second}"
                                },
                                onGetLastLocationFailed = { exception ->
                                    showPermissionResultText = true
                                    locationText =
                                        exception.localizedMessage ?: "Error Getting Last Location"
                                },
                                onGetLastLocationIsNull = {
                                    // Attempt to get the current user location
                                    getCurrentLocation(
                                        onGetCurrentLocationSuccess = {
                                            locationText =
                                                "Location using CURRENT-LOCATION: LATITUDE: ${it.first}, LONGITUDE: ${it.second}"
                                        },
                                        onGetCurrentLocationFailed = {
                                            showPermissionResultText = true
                                            locationText =
                                                it.localizedMessage
                                                    ?: "Error Getting Current Location"
                                        }
                                    )
                                }
                            )
                        },
                        onPermissionDenied = {
                            // Callback when permission is denied
                            showPermissionResultText = true
                            permissionResultText = "Permission Denied :("
                        },
                    )

                    // Compose UI layout using a Column
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Display a message indicating the permission request process
                        Text(
                            text = "Requesting location permission...",
                            textAlign = TextAlign.Center
                        )

                        // Display permission result and location information if available
                        if (showPermissionResultText) {
                            Text(text = permissionResultText, textAlign = TextAlign.Center)
                            Text(text = locationText, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }

    /**
     * Retrieves the last known user location asynchronously.
     *
     * @param onGetLastLocationSuccess Callback function invoked when the location is successfully retrieved.
     *        It provides a Pair representing latitude and longitude.
     * @param onGetLastLocationFailed Callback function invoked when an error occurs while retrieving the location.
     *        It provides the Exception that occurred.
     */
    @SuppressLint("MissingPermission")
    private fun getLastUserLocation(
        onGetLastLocationSuccess: (Pair<Double, Double>) -> Unit,
        onGetLastLocationFailed: (Exception) -> Unit,
        onGetLastLocationIsNull: () -> Unit
    ) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        // Check if location permissions are granted
        if (areLocationPermissionsGranted()) {
            // Retrieve the last known location
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        // If location is not null, invoke the success callback with latitude and longitude
                        onGetLastLocationSuccess(Pair(it.latitude, it.longitude))
                    }?.run {
                        onGetLastLocationIsNull()
                    }
                }
                .addOnFailureListener { exception ->
                    // If an error occurs, invoke the failure callback with the exception
                    onGetLastLocationFailed(exception)
                }
        }
    }


    /**
     * Retrieves the current user location asynchronously.
     *
     * @param onGetCurrentLocationSuccess Callback function invoked when the current location is successfully retrieved.
     *        It provides a Pair representing latitude and longitude.
     * @param onGetCurrentLocationFailed Callback function invoked when an error occurs while retrieving the current location.
     *        It provides the Exception that occurred.
     * @param priority Indicates the desired accuracy of the location retrieval. Default is high accuracy.
     *        If set to false, it uses balanced power accuracy.
     */
    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(
        onGetCurrentLocationSuccess: (Pair<Double, Double>) -> Unit,
        onGetCurrentLocationFailed: (Exception) -> Unit,
        priority: Boolean = true
    ) {
        // Determine the accuracy priority based on the 'priority' parameter
        val accuracy = if (priority) Priority.PRIORITY_HIGH_ACCURACY
        else Priority.PRIORITY_BALANCED_POWER_ACCURACY

        // Check if location permissions are granted
        if (areLocationPermissionsGranted()) {
            // Retrieve the current location asynchronously
            fusedLocationProviderClient.getCurrentLocation(
                accuracy, CancellationTokenSource().token,
            ).addOnSuccessListener { location ->
                location?.let {
                    // If location is not null, invoke the success callback with latitude and longitude
                    onGetCurrentLocationSuccess(Pair(it.latitude, it.longitude))
                }?.run {
                    //Location null do something
                }
            }.addOnFailureListener { exception ->
                // If an error occurs, invoke the failure callback with the exception
                onGetCurrentLocationFailed(exception)
            }
        }
    }


    /**
     * Checks if location permissions are granted.
     *
     * @return true if both ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION permissions are granted; false otherwise.
     */
    private fun areLocationPermissionsGranted(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
    }

}