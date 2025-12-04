package com.example.tides.screens.noLocationScreen

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun NoLocationScreen(onPermissionGranted: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity

    var showRationaleDialog by remember { mutableStateOf(false) }
    var permissionDeniedAgain by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->

                if (event == Lifecycle.Event.ON_RESUME) {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        onPermissionGranted()
                    }
                }
            }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted: Boolean ->
                if (isGranted) {
                    onPermissionGranted()
                }

                permissionDeniedAgain = true
            },
        )

    fun openAppSettings() {
        val intent =
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null),
            )
        context.startActivity(intent)
    }

    if (showRationaleDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Location Permission Needed") },
            text = {
                Text(
                    "This app needs location access to provide weather and tide data for your current area. Please grant the permission.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        permissionDeniedAgain = true
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    },
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { }) { Text("Cancel") }
            },
        )
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "Location Access Required",
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "This app relies on your location to function. Please grant permission to continue.",
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))
        if (permissionDeniedAgain) {
            Text("You will have to open settings and grand permission manually")
            Button(onClick = { openAppSettings() }) {
                Text("Open Settings")
            }
        } else {
            Button(
                onClick = {
                    if (activity != null) {
                        when {
                            ActivityCompat.shouldShowRequestPermissionRationale(
                                activity,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                            ) -> {
                            }

                            else -> {
                                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                println("Permission granted")
                            }
                        }
                    }
                },
            ) {
                Text("Grant Location Access")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NoLocationScreenPreview() {
    NoLocationScreen(onPermissionGranted = {})
}
