package com.universe.audioflare.ui.utils

import androidx.compose.ui.util.fastAny
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import com.universe.audioflare.ui.screens.Screens

val NavController.canNavigateUp: Boolean
    get() = backQueue.count { entry -> entry.destination !is NavGraph } > 1

fun NavController.backToMain() {
    while (canNavigateUp && !Screens.MainScreens.fastAny { it.route == currentBackStackEntry?.destination?.route }) {
        navigateUp()
    }
}