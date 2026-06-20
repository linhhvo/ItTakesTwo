package me.linhvo.ittakestwo.navigation

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
@Preview(showSystemUi = true)
@Composable
fun AppNavigation() {

    /*TODO: Move state to ViewModel with auth logic*/
    var isSignedIn by rememberSaveable { mutableStateOf(true) }
    val startRoute = Route.Home
    val backStack = rememberNavBackStack(startRoute)

    Scaffold { _ ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
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
                                targetOffsetY = { -it }, animationSpec = tween(500)
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
                                targetOffsetY = { -it }, animationSpec = tween(500)
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
                    entry<Route.Home>(metadata = metadata {
                        put(NavDisplay.TransitionKey) {
                            slideInHorizontally(
                                initialOffsetX = { -it }, animationSpec = tween(500)
                            ) togetherWith slideOutHorizontally(
                                targetOffsetX = { it }, animationSpec = tween(500)
                            )
                        }
                    }) {
                        if (isSignedIn) {
                            HomeScreen(
                                onSignOutButtonClicked = dropUnlessResumed {
                                    isSignedIn = false
                                }
                            )
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

            if (backStack.last() is Route.BottomNavRoute && backStack.last() != Route.Chat) {
                AppNavBar(
                    modifier = Modifier.padding(bottom = 30.dp),
                    currentRoute = backStack.last(),
                    onNavItemClicked = {
                        backStack.add(it)
                        Log.d("Backstack", backStack.toList().toString())
                    }
                )
            }
        }
    }
}