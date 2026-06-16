package me.linhvo.ittakestwo

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable
import me.linhvo.ittakestwo.home.HomeScreen
import me.linhvo.ittakestwo.usersignin.UserSignInScreen
import me.linhvo.ittakestwo.usersignup.UserSignUpScreen

@Serializable
private data object HomeScreen : NavKey

@Serializable
private data object SignInScreen : NavKey

@Serializable
private data object SignUpScreen : NavKey

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
) {

    val backStack = rememberNavBackStack(HomeScreen)
    var isSignedIn by rememberSaveable { mutableStateOf(false) }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<HomeScreen> {
                if (isSignedIn) {
                    HomeScreen()
                } else {
                    backStack.add(SignInScreen)
                }
            }
            entry<SignInScreen> {
                UserSignInScreen(
                    onSignInButtonClicked = dropUnlessResumed {
                        isSignedIn = true
                        backStack.removeLastOrNull()
                    },
                    onCreateAccountTextClicked = dropUnlessResumed { backStack.add(SignUpScreen) })
            }
            entry<SignUpScreen> {
                UserSignUpScreen(onCreateAccountButtonClicked = dropUnlessResumed {
                    isSignedIn = true
                    backStack.clear()
                    backStack.add(HomeScreen)
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