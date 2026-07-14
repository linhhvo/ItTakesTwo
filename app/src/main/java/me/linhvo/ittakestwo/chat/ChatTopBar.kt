package me.linhvo.ittakestwo.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.linhvo.ittakestwo.R
import me.linhvo.ittakestwo.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(partner: User, scrollBehavior: TopAppBarScrollBehavior, navigateToHome: () -> Unit) {
    TopAppBar(
        scrollBehavior = scrollBehavior,
        modifier = Modifier.padding(0.dp),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        title = {
            Text(
                text = partner.displayName, fontSize = 25.sp, fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            IconButton(onClick = { navigateToHome() }) {
                Icon(
                    painter = painterResource(R.drawable.back_icon),
                    contentDescription = "arrow back icon",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        actions = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.padding(end = 15.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.search_icon),
                    contentDescription = "search icon",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(enabled = true, onClick = {/*TODO*/ })

                )
                Icon(
                    painter = painterResource(R.drawable.folder_icon),
                    contentDescription = "file icon",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(enabled = true, onClick = {/*TODO*/ })
                )
                Icon(
                    painter = painterResource(R.drawable.calendar_icon),
                    contentDescription = "calendar icon",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(enabled = true, onClick = {/*TODO*/ })
                )
            }
        },
    )
}