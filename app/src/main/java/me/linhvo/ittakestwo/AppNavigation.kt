package me.linhvo.ittakestwo

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.linhvo.ittakestwo.home.HomeScreen
import me.linhvo.ittakestwo.usersignin.UserSignInScreen
import me.linhvo.ittakestwo.usersignup.UserSignUpScreen

private object AppScreens {
    const val HOME_SCREEN = "home"
    const val SIGNIN_SCREEN = "signin"
    const val SIGNUP_SCREEN = "signup"
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppScreens.SIGNIN_SCREEN
) {
//    val currentNavBackStackEntry by navController.currentBackStackEntryAsState()
//    val currentRoute = currentNavBackStackEntry?.destination?.route ?: startDestination

    NavHost(
        navController = navController, startDestination = startDestination, modifier = modifier
    ) {
        composable(route = AppScreens.HOME_SCREEN) {
            HomeScreen()
        }

        composable(route = AppScreens.SIGNIN_SCREEN) {
            UserSignInScreen(
                onLogInButtonClicked = { navController.navigate(AppScreens.HOME_SCREEN) },
                onCreateAccountTextClicked = { navController.navigate(AppScreens.SIGNUP_SCREEN) })
        }

        composable(route = AppScreens.SIGNUP_SCREEN) {
            UserSignUpScreen(onCreateAccountButtonClicked = { navController.navigate(AppScreens.HOME_SCREEN) })
        }
    }
}