package me.linhvo.ittakestwo.navigation

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import me.linhvo.ittakestwo.R
import me.linhvo.ittakestwo.home.HomeScreen
import me.linhvo.ittakestwo.usersignin.UserSignInScreen
import me.linhvo.ittakestwo.usersignup.UserSignUpScreen

sealed interface TopLevelRoute {
    @get:DrawableRes
    val iconSelected: Int

    @get:DrawableRes
    val iconUnselected: Int
}

data object HomeRoute : TopLevelRoute {
    override val iconSelected = R.drawable.home_selected
    override val iconUnselected = R.drawable.home_unselected
}

data object ChatRoute : TopLevelRoute {
    override val iconSelected = R.drawable.chat_selected
    override val iconUnselected = R.drawable.chat_unselected
}

data object SettingsRoute : TopLevelRoute {
    override val iconSelected = R.drawable.gear_selected
    override val iconUnselected = R.drawable.gear_unselected
}

private data object SignInRoute : NavKey {
}

private data object SignUpRoute : NavKey


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppNavigation() {

    /*TODO: Move state to ViewModel with auth logic*/
    var isSignedIn by rememberSaveable { mutableStateOf(true) }
    val topLevelBackStack = remember { TopLevelBackStack<Any>(HomeRoute) }

    Scaffold { _ ->
        NavDisplay(
            backStack = topLevelBackStack.backStack,
            onBack = { topLevelBackStack.removeLast() },
            entryProvider = entryProvider {
                entry<HomeRoute> {
                    if (isSignedIn) {
                        HomeScreen(onSignOutButtonClicked = dropUnlessResumed {
                            isSignedIn = false
                        })
                    } else {
                        LaunchedEffect(null) {
                            topLevelBackStack.add(SignInRoute)
//                            Log.d("Backstack", topLevelBackStack.backStack.toString())
                        }
                    }
                }
                entry<SignInRoute> {
                    UserSignInScreen(
                        onSignInButtonClicked = dropUnlessResumed {
                            isSignedIn = true
                            topLevelBackStack.removeLast()
//                            Log.d("Backstack", topLevelBackStack.backStack.toString())
                        },
                        onCreateAccountTextClicked = dropUnlessResumed { topLevelBackStack.add(SignUpRoute) })
                }
                entry<SignUpRoute> {
                    UserSignUpScreen(onCreateAccountButtonClicked = dropUnlessResumed {
                        isSignedIn = true
                        topLevelBackStack.backStack.clear()
                        topLevelBackStack.add(HomeRoute)
                    })
                }
            },
            transitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(500)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(500)
                )
            },
            popTransitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(500)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(500)
                )
            },
            predictivePopTransitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(500)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(500)
                )
            }
        )
    }

}

class TopLevelBackStack<T : Any>(startKey: T) {
    private var topLevelStacks: LinkedHashMap<T, SnapshotStateList<T>> = linkedMapOf(
        startKey to mutableStateListOf(startKey)
    )

    var topLevelKey by mutableStateOf(startKey)
        private set

    val backStack = mutableStateListOf(startKey)

    private fun updateBackStack() =
        backStack.apply {
            clear()
            addAll(topLevelStacks.flatMap { it.value })
        }

    fun addTopLevel(key: T) {
        if (topLevelStacks[key] == null) {
            topLevelStacks[key] = mutableStateListOf(key)
        } else {
            topLevelStacks.apply {
                remove(key)?.let {
                    put(key, it)
                }
            }
        }
        topLevelKey = key
        updateBackStack()
    }

    fun add(key: T) {
        topLevelStacks[topLevelKey]?.add(key)
        updateBackStack()
    }

    fun removeLast() {
        val removedKey = topLevelStacks[topLevelKey]?.removeLastOrNull()
        topLevelStacks.remove(removedKey)
        topLevelKey = topLevelStacks.keys.last()
        updateBackStack()
    }
}