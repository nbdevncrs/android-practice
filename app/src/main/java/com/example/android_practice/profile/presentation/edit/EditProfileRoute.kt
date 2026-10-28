package com.example.android_practice.profile.presentation.edit

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry

@Composable
fun EditProfileRoute(entry: NavBackStackEntry, factory: ViewModelProvider.Factory, onBack: () -> Unit) {
    val vm: EditProfileViewModel = viewModel(viewModelStoreOwner = entry, factory = factory)
    val state by vm.uiState.collectAsStateWithLifecycle()
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        vm.selectPhoto(uri?.toString())
    }
    val camera = rememberLauncherForActivityResult(TakeProfilePicture()) { success -> vm.cameraResult(success) }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onBack()
    }
    LaunchedEffect(state.cameraToLaunch) {
        val uri = state.cameraToLaunch ?: return@LaunchedEffect
        vm.cameraLaunched()
        try {
            camera.launch(uri.toUri())
        } catch (_: ActivityNotFoundException) {
            vm.cameraUnavailable()
        } catch (_: SecurityException) {
            vm.cameraUnavailable()
        }
    }

    EditProfileScreen(
        state = state,
        onBack = onBack,
        onFullNameChange = vm::setFullName,
        onResumeUrlChange = vm::setResumeUrl,
        onDone = vm::save,
        onRetryLoading = vm::retryLoading,
        onGallery = {
            try {
                gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } catch (_: ActivityNotFoundException) {
                vm.photoSelectionFailed()
            } catch (_: SecurityException) {
                vm.photoSelectionFailed()
            }
        },
        onCamera = vm::prepareCamera,
    )
}

private class TakeProfilePicture : ActivityResultContracts.TakePicture() {
    override fun createIntent(context: Context, input: Uri): Intent = super.createIntent(context, input).apply {
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        clipData = ClipData.newRawUri("avatar", input)
    }
}
