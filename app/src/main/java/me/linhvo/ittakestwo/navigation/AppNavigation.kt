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

sealed interface TopLevelScreen {
    @get:DrawableRes
    val iconId: Int
}

private data object HomeScreen : TopLevelScreen {
    override val iconId = R.drawable.home
}

private data object ChatScreen : TopLevelScreen {
    override val iconId = R.drawable.chat
}

private data object SettingsScreen : TopLevelScreen {
    override val iconId = R.drawable.gear
}

private data object SignInScreen : NavKey {
}

private data object SignUpScreen : NavKey

private val TOP_LEVEL_SCREENS: List<TopLevelScreen> = listOf(HomeScreen, ChatScreen, SettingsScreen)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppNavigation() {

    var isSignedIn by rememberSaveable { mutableStateOf(false) }
    val topLevelBackStack = remember { TopLevelBackStack<Any>(HomeScreen) }

    Scaffold(
        bottomBar = {
            if (isSignedIn) {
                AppNavBar(
                    topLevelBackStack = topLevelBackStack,
                    topLevelScreens = TOP_LEVEL_SCREENS
                )
            }
        }
    ) { _ ->
        NavDisplay(
            backStack = topLevelBackStack.backStack,
            onBack = { topLevelBackStack.removeLast() },
            entryProvider = entryProvider {
                entry<HomeScreen> {
                    if (isSignedIn) {
                        HomeScreen(onSignOutButtonClicked = dropUnlessResumed {
                            isSignedIn = false
                        })
                    } else {
                        LaunchedEffect(null) {
                            topLevelBackStack.add(SignInScreen)
//                            Log.d("Backstack", topLevelBackStack.backStack.toString())
                        }
                    }
                }
                entry<SignInScreen> {
                    UserSignInScreen(
                        onSignInButtonClicked = dropUnlessResumed {
                            isSignedIn = true
                            topLevelBackStack.removeLast()
//                            Log.d("Backstack", topLevelBackStack.backStack.toString())
                        },
                        onCreateAccountTextClicked = dropUnlessResumed { topLevelBackStack.add(SignUpScreen) })
                }
                entry<SignUpScreen> {
                    UserSignUpScreen(onCreateAccountButtonClicked = dropUnlessResumed {
                        isSignedIn = true
                        topLevelBackStack.backStack.clear()
                        topLevelBackStack.add(HomeScreen)
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