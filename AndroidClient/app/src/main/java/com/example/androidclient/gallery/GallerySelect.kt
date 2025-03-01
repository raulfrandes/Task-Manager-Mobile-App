package com.example.androidclient.gallery

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import com.example.androidclient.util.RequirePermissions
import com.google.accompanist.permissions.ExperimentalPermissionsApi

@ExperimentalPermissionsApi
@Composable
fun GalleryScreen(
    modifier: Modifier,
    onImageUri: (Uri) -> Unit = { }
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            onImageUri(uri ?: EMPTY_IMAGE_URI)
        }
    )

    @Composable
    fun LaunchGallery() {
        SideEffect {
            launcher.launch("image/*")
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        RequirePermissions(
            permissions = listOf(Manifest.permission.ACCESS_MEDIA_LOCATION),
            modifier = modifier
        ) {
            LaunchGallery()
        }
    } else {
        LaunchGallery()
    }
}

val EMPTY_IMAGE_URI: Uri = Uri.parse("file://dev/null")