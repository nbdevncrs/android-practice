package com.example.android_practice.profile.presentation.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.android_practice.profile.presentation.edit.EditProfileRoute
import com.example.android_practice.profile.presentation.edit.EditProfileViewModel
import com.example.android_practice.profile.presentation.view.ProfileRoute
import com.example.android_practice.profile.presentation.view.ProfileViewModel

object ProfileNavigation {
    const val ROUTE = "profile_graph"
    internal const val VIEW = "profile"
    internal const val EDIT = "edit_profile"

    fun isEditor(destination: NavDestination?): Boolean = destination?.route == EDIT
}

fun NavGraphBuilder.profileGraph(navController: NavHostController, dependencies: ProfileDependencies) {
    val profileFactory = ProfileViewModel.factory(dependencies.profiles, dependencies.resumes)
    val editFactory = EditProfileViewModel.factory(
        repository = dependencies.profiles,
        avatars = dependencies.avatars,
        cleanupScope = dependencies.cleanupScope,
        discardUnusedAvatar = dependencies.discardUnusedAvatar,
        saveProfileAndScheduleReminder = dependencies.saveProfileAndScheduleReminder,
    )

    navigation(startDestination = ProfileNavigation.VIEW, route = ProfileNavigation.ROUTE) {
        composable(ProfileNavigation.VIEW) { entry ->
            ProfileRoute(entry, profileFactory, onEdit = {
                navController.navigate(ProfileNavigation.EDIT) { launchSingleTop = true }
            })
        }
        composable(ProfileNavigation.EDIT) { entry ->
            EditProfileRoute(entry, editFactory, dependencies.reminderAccess, onBack = {
                if (navController.currentBackStackEntry == entry) navController.popBackStack()
            })
        }
    }
}
