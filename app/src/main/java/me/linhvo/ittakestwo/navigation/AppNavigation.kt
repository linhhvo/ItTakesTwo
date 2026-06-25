package me.linhvo.ittakestwo.navigation

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.jan.supabase.auth.status.SessionStatus
import me.linhvo.ittakestwo.auth.SignInScreen
import me.linhvo.ittakestwo.auth.SignUpScreen
import me.linhvo.ittakestwo.chat.ChatScreen
import me.linhvo.ittakestwo.home.HomeScreen
import me.linhvo.ittakestwo.settings.SettingsScreen

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
//@Preview(showSystemUi = true)
@Composable
fun AppNavigation() {

    val startRoute = Route.Home
    val backStack = rememberNavBackStack(startRoute)

    val navViewModel: NavViewModel = viewModel()
    val sessionStatus by navViewModel.sessionStatus.collectAsStateWithLifecycle()

    LaunchedEffect(sessionStatus) {
        when (sessionStatus) {
            is SessionStatus.Authenticated -> {
                backStack.clear()
                backStack.add(Route.Home)
            }

            else -> {
                backStack.clear()
                backStack.add(Route.SignIn)
            }
        }
    }

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
                    entry<Route.Home> {
                        Log.d("backstack -- Home", backStack.toList().toString())
                        HomeScreen()
                    }
                    entry<Route.SignIn> {
                        Log.d("backstack -- Signin", backStack.toList().toString())
                        SignInScreen(
                            onCreateAccountTextClick = dropUnlessResumed {
                                backStack.add(Route.SignUp)
                            })
                    }
                    entry<Route.SignUp> {
                        Log.d("backstack -- Signup", backStack.toList().toString())
                        SignUpScreen()
                    }

                    entry<Route.Chat> {
                        Log.d("backstack -- chat", backStack.toList().toString())
                        ChatScreen()
                    }
                    entry<Route.Settings> {
                        Log.d("backstack -- settings", backStack.toList().toString())
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
                        if (it != Route.Home) {
                            backStack.add(it)
                        } else {
                            backStack.clear()
                            backStack.add(Route.Home)
                        }
                    }
                )
            }
        }
    }
}