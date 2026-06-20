package me.linhvo.ittakestwo.navigation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey


@Composable
fun AppNavBar(
    modifier: Modifier = Modifier,
    currentRoute: NavKey,
    onNavItemClicked: (Route.BottomNavRoute) -> Unit
) {
    val containerColor = MaterialTheme.colorScheme.onPrimaryContainer
    Row(
        modifier = modifier
            .height(60.dp)
            .width(230.dp)
            .drawBehind {
                drawRoundRect(
                    color = containerColor.copy(alpha = .05f),
                    cornerRadius = CornerRadius(10.dp.toPx()),
                )
                drawRoundRect(
                    color = containerColor.copy(alpha = .2f),
                    cornerRadius = CornerRadius(10.dp.toPx()),
                    style = Stroke(width = 3f)
                )
            }
    ) {
        NAV_ROUTES.forEach { route ->
            val isSelected = route == currentRoute
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavItemClicked(route) },
                icon = {
                    if (isSelected) {
                        Icon(
                            painter = painterResource(route.iconSelected),
                            contentDescription = "selected icon",
                            modifier = Modifier.size(35.dp)
                        )
                    } else {
                        if (route == Route.Chat) {
                            BadgedBox(badge = {
                                Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                                    // TODO: replace with actual unread chat messages
                                    Text(text = "1")
                                }
                            }) {
                                Icon(
                                    painter = painterResource(route.iconUnselected),
                                    contentDescription = "unselected icon",
                                    modifier = Modifier.size(35.dp)
                                )
                            }
                        } else {
                            Icon(
                                painter = painterResource(route.iconUnselected),
                                contentDescription = "unselected icon",
                                modifier = Modifier.size(35.dp)
                            )
                        }
                    }
                },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.secondary,
                    indicatorColor = Color.Transparent
                ),
            )
        }
    }
}