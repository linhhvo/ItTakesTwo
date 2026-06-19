package me.linhvo.ittakestwo.navigation

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.metadata
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import me.linhvo.ittakestwo.chat.ChatScreen
import me.linhvo.ittakestwo.home.HomeScreen
import me.linhvo.ittakestwo.settings.SettingsScreen
import me.linhvo.ittakestwo.usersignin.UserSignInScreen
import me.linhvo.ittakestwo.usersignup.UserSignUpScreen

val NAV_ROUTES: List<Route.BottomNavRoute> =
    listOf(Route.Home, Route.Chat, Route.Settings)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppNavigation() {

    /*TODO: Move state to ViewModel with auth logic*/
    var isSignedIn by rememberSaveable { mutableStateOf(false) }
    val startRoute = Route.Home
    val backStack = rememberNavBackStack(startRoute)

    Scaffold(bottomBar = {
        if (backStack.last() is Route.BottomNavRoute && backStack.last() != Route.Chat) {
            AppNavBar(
                currentRoute = backStack.last(),
                onNavItemClicked = {
                    backStack.add(it)
                    Log.d("Backstack", backStack.toList().toString())
                }
            )
        }
    }) { _ ->
        NavDisplay(
            backStack = backStack,
            onBack = {
                if (backStack.last() is Route.BottomNavRoute) {
                    backStack.clear()
                    backStack.add(Route.Home)
                    Log.d("Backstack", backStack.toList().toString())
                } else {
                    backStack.removeLastOrNull()
                    Log.d("Backstack", backStack.toList().toString())
                }
            },
            entryProvider = entryProvider {
                entry<Route.SignIn>(metadata = metadata {
                    put(NavDisplay.PopTransitionKey) {
                        EnterTransition.None togetherWith slideOutVertically(
                            targetOffsetY = { -it }, animationSpec = tween(1000)
                        )
                    }
                }) {
                    UserSignInScreen(onSignInButtonClicked = dropUnlessResumed {
                        isSignedIn = true
                        backStack.removeLast()
                        Log.d("Backstack", backStack.toList().toString())
                    }, onCreateAccountTextClicked = dropUnlessResumed {
                        backStack.add(Route.SignUp)
                        Log.d("Backstack", backStack.toList().toString())
                    })
                }
                entry<Route.SignUp>(metadata = metadata {
                    put(NavDisplay.PopTransitionKey) {
                        EnterTransition.None togetherWith slideOutVertically(
                            targetOffsetY = { -it }, animationSpec = tween(1000)
                        )
                    }
                }) {
                    UserSignUpScreen(onCreateAccountButtonClicked = dropUnlessResumed {
                        isSignedIn = true
                        backStack.clear()
                        backStack.add(Route.Home)
                        Log.d("Backstack", backStack.toList().toString())
                    })
                }
                entry<Route.Home> {
                    if (isSignedIn) {
                        HomeScreen(onSignOutButtonClicked = dropUnlessResumed {
                            isSignedIn = false
                        })
                    } else {
                        LaunchedEffect(null) {
                            backStack.add(Route.SignIn)
//                            Log.d("Backstack", backStack.toString())
                        }
                    }
                }
                entry<Route.Chat> {
                    ChatScreen()
                }
                entry<Route.Settings> {
                    SettingsScreen()

                }
            }, transitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { it }, animationSpec = tween(500)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { -it }, animationSpec = tween(500)
                )
            }, popTransitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { -it }, animationSpec = tween(500)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { it }, animationSpec = tween(500)
                )
            }, predictivePopTransitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { -it }, animationSpec = tween(500)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { it }, animationSpec = tween(500)
                )
            })
    }
}