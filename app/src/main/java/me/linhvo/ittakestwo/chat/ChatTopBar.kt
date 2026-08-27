package me.linhvo.ittakestwo.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale.Companion.Crop
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import me.linhvo.ittakestwo.R
import me.linhvo.ittakestwo.database.model.User

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun ChatTopBar(
    partner: User?,
    navigateToHome: () -> Unit,
    openMediaGallery: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    TopAppBar(
        modifier = Modifier.padding(0.dp),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (partner?.avatarPath != null) {
                    GlideImage(
                        model = partner.avatarPath,
                        contentDescription = "user avatar",
                        contentScale = Crop,
                        modifier = Modifier
                            .size(35.dp)
                            .clip(CircleShape)
                    )
                }

                Text(
                    text = partner?.displayName ?: "No partner", fontSize = 20.sp, fontWeight = FontWeight.SemiBold
                )
            }
        },

        navigationIcon = {
            IconButton(onClick = {
                keyboardController?.hide()
                navigateToHome()
            }) {
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
//                Icon(
//                    painter = painterResource(R.drawable.search_icon),
//                    contentDescription = "search icon",
//                    tint = MaterialTheme.colorScheme.onSurface,
//                    modifier = Modifier
//                        .size(20.dp)
//                        .clickable(enabled = true, onClick = {/*TODO*/ })
//
//                )
                Icon(
                    painter = painterResource(R.drawable.folder_icon),
                    contentDescription = "file icon",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(enabled = true, onClick = { openMediaGallery() })
                )
//                Icon(
//                    painter = painterResource(R.drawable.calendar_icon),
//                    contentDescription = "calendar icon",
//                    tint = MaterialTheme.colorScheme.onSurface,
//                    modifier = Modifier
//                        .size(20.dp)
//                        .clickable(enabled = true, onClick = {/*TODO*/ })
//                )
            }
        },
    )
}