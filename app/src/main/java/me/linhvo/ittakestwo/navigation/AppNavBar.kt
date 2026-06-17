package me.linhvo.ittakestwo.navigation

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.compose.AppTheme

val TOP_LEVEL_SCREENS: List<TopLevelRoute> = listOf(HomeRoute, ChatRoute, SettingsRoute)

@Composable
fun AppNavBar(
    currentRoute: TopLevelRoute,
) {
    NavigationBar(
        containerColor = Color.Transparent,
        modifier = Modifier.height(70.dp)
    ) {
        TOP_LEVEL_SCREENS.forEach { screen ->
            val isSelected = screen == currentRoute
            NavigationBarItem(
                selected = isSelected,
                onClick = {/*TODO*/ },
                icon = {
                    if (isSelected) {
                        Icon(
                            painter = painterResource(screen.iconSelected),
                            contentDescription = "selected icon",
                            modifier = Modifier.size(35.dp)
                        )
                    } else {
                        if (screen == ChatRoute) {
                            BadgedBox(badge = {
                                Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                                    /* TODO: replace with actual unread chat messages */
                                    Text(text = "1")
                                }
                            }) {
                                Icon(
                                    painter = painterResource(screen.iconUnselected),
                                    contentDescription = "unselected icon",
                                    modifier = Modifier.size(35.dp)
                                )
                            }
                        } else {
                            Icon(
                                painter = painterResource(screen.iconUnselected),
                                contentDescription = "unselected icon",
                                modifier = Modifier.size(35.dp)
                            )
                        }
                    }
                },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primaryContainer,
                    indicatorColor = Color.Transparent
                ),
            )
        }
    }
}

@Preview
@Composable
fun BarPreview() {
    AppTheme(darkTheme = true, dynamicColor = false) {
        AppNavBar(HomeRoute)
    }
}