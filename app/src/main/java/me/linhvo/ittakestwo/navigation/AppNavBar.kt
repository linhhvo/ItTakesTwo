package me.linhvo.ittakestwo.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource

@Composable
fun AppNavBar(
    modifier: Modifier = Modifier,
    topLevelBackStack: TopLevelBackStack<Any>,
    topLevelScreens: List<TopLevelScreen>,
) {
    NavigationBar {
        topLevelScreens.forEach { topLevelScreen ->
            val isSelected = topLevelScreen == topLevelBackStack.topLevelKey
            NavigationBarItem(
                selected = isSelected,
                onClick = {},
                icon = { Icon(painter = painterResource(topLevelScreen.iconId), "null") }
            )
        }
    }
}