package me.linhvo.ittakestwo.navigation

import android.annotation.SuppressLint
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.jan.supabase.auth.status.SessionStatus
import me.linhvo.ittakestwo.auth.SignInScreen
import me.linhvo.ittakestwo.auth.SignUpScreen
import me.linhvo.ittakestwo.chat.ChatScreen
import me.linhvo.ittakestwo.gallery.GalleryScreen
import me.linhvo.ittakestwo.gallery.ViewerScreen
import me.linhvo.ittakestwo.home.HomeScreen
import me.linhvo.ittakestwo.settings.SettingsScreen

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppNavigation(viewModel: NavViewModel = hiltViewModel()) {
    val backStack = rememberNavBackStack(Route.Start)
    val sessionStatus by viewModel.sessionStatus.collectAsStateWithLifecycle()
    val unreadMessageCount by viewModel.unreadMessageCount.collectAsStateWithLifecycle()

    LaunchedEffect(sessionStatus) {
        when (sessionStatus) {
            is SessionStatus.Authenticated -> {
                viewModel.initializeData()
                if (backStack.last() is Route.InitialRoute) {
                    backStack.clear()
                    backStack.add(Route.Home)
                }
            }

            is SessionStatus.Initializing -> {
                //TODO: add loading screen
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
                    entry<Route.Start> {
                        //TODO: add app start screen
                    }
                    entry<Route.Home> {
                        HomeScreen()
                    }
                    entry<Route.SignIn> {
                        SignInScreen(
                            onCreateAccountTextClick = dropUnlessResumed {
                                backStack.add(Route.SignUp)
                            })
                    }
                    entry<Route.SignUp> {
                        SignUpScreen()
                    }

                    entry<Route.Chat> {
                        ChatScreen(
                            navigateToHome = dropUnlessResumed {
                                backStack.clear()
                                backStack.add(Route.Home)
                            },
                            openMediaGallery = {
                                backStack.add(Route.MediaGallery)
                            },
                            openMediaViewer = {
                                backStack.add(Route.MediaViewer(attachmentId = it))
                            }
                        )
                    }

                    entry<Route.MediaViewer> { key ->
                        ViewerScreen(
                            targetAttachmentId = key.attachmentId,
                            onBack = { backStack.removeLastOrNull() })
                    }

                    entry<Route.MediaGallery> {
                        GalleryScreen(
                            onBack = { backStack.removeLastOrNull() },
                            openMediaViewer = { backStack.add(Route.MediaViewer(attachmentId = it)) }
                        )
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
                    unreadMessageCount = unreadMessageCount,
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