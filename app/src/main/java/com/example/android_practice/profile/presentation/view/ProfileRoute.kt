package com.example.android_practice.profile.presentation.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import com.example.android_practice.profile.presentation.platform.ResumeDocumentOpener

@Composable
fun ProfileRoute(entry: NavBackStackEntry, factory: ViewModelProvider.Factory, onEdit: () -> Unit) {
    val vm: ProfileViewModel = viewModel(viewModelStoreOwner = entry, factory = factory)
    val state by vm.uiState.collectAsStateWithLifecycle()
    val resumeState by vm.resumeState.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val lifecycleState by lifecycle.currentStateFlow.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val documentOpener = remember(context) { ResumeDocumentOpener(context) }

    LaunchedEffect(resumeState, lifecycleState) {
        if (!lifecycleState.isAtLeast(Lifecycle.State.RESUMED)) return@LaunchedEffect
        val document = vm.takeResumeToOpen() ?: return@LaunchedEffect
        documentOpener.open(document)?.let(vm::resumeOpeningFailed)
    }

    ProfileScreen(state, onRetry = vm::retry, onEdit = onEdit, onResume = vm::openResume,
        resumeState = resumeState, onCancelResume = vm::cancelResume)
}
