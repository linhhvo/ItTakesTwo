package me.linhvo.ittakestwo.navigation

import androidx.annotation.DrawableRes
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import me.linhvo.ittakestwo.R

val NAV_ROUTES: List<Route.BottomNavRoute> =
    listOf(Route.Home, Route.Chat, Route.Settings)

sealed interface Route : NavKey {
    sealed interface BottomNavRoute : Route {
        @get:DrawableRes
        val iconSelected: Int

        @get:DrawableRes
        val iconUnselected: Int
    }

    @Serializable
    data object Home : BottomNavRoute {
        override val iconSelected = R.drawable.home_selected
        override val iconUnselected = R.drawable.home_unselected
    }

    @Serializable
    data object Chat : BottomNavRoute {
        override val iconSelected = R.drawable.chat_selected
        override val iconUnselected = R.drawable.chat_unselected
    }

    @Serializable
    data object Settings : BottomNavRoute {
        override val iconSelected = R.drawable.gear_selected
        override val iconUnselected = R.drawable.gear_unselected
    }

    @Serializable
    data object SignIn : Route

    @Serializable
    data object SignUp : Route
}