package me.linhvo.ittakestwo.navigation

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.metadata
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import me.linhvo.ittakestwo.auth.SignInScreen
import me.linhvo.ittakestwo.auth.SignUpScreen
import me.linhvo.ittakestwo.chat.ChatScreen
import me.linhvo.ittakestwo.home.HomeScreen
import me.linhvo.ittakestwo.settings.SettingsScreen

val NAV_ROUTES: List<Route.BottomNavRoute> =
    listOf(Route.Home, Route.Chat, Route.Settings)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
//@Preview(showSystemUi = true)
@Composable
fun AppNavigation() {

    val startRoute = Route.Home
    val backStack = rememberNavBackStack(startRoute)

    val navViewModel: NavViewModel = viewModel()
    val isSignedIn by navViewModel.isSignedIn.collectAsStateWithLifecycle()

    Scaffold { _ ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            NavDisplay(
                backStack = backStack,
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                onBack = {
                    if (backStack.last() is Route.BottomNavRoute) {
                        backStack.clear()
                        backStack.add(Route.Home)
                    } else {
                        backStack.removeLastOrNull()
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
                        SignInScreen(onSignInSuccess = dropUnlessResumed {
                            navViewModel.userSignIn()
                            backStack.removeLast()
                        }, onCreateAccountTextClick = dropUnlessResumed {
                            backStack.add(Route.SignUp)
                        })
                    }
                    entry<Route.SignUp>(metadata = metadata {
                        put(NavDisplay.PopTransitionKey) {
                            EnterTransition.None togetherWith slideOutVertically(
                                targetOffsetY = { -it }, animationSpec = tween(500)
                            )
                        }
                    }) {
                        SignUpScreen(onSignUpSuccess = dropUnlessResumed {
                            navViewModel.userSignIn()
                            backStack.clear()
                            backStack.add(Route.Home)
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
                                    navViewModel.userSignOut()
                                }
                            )
                        } else {
                            LaunchedEffect(null) {
                                backStack.add(Route.SignIn)
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
                    }
                )
            }
        }
    }
}