package me.linhvo.ittakestwo.profile

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale.Companion.Crop
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import me.linhvo.ittakestwo.R
import me.linhvo.ittakestwo.model.User

data class Event(
    val iconId: Int,
    val title: String,
    val date: String
)

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun ProfileDialog(
    onDismissRequest: () -> Unit,
    user: User?,
    partner: User?,
    uploadAvatar: (Context, Uri) -> Unit,
) {
//    val eventList = listOf(Event(1, "wedding", "10-12-2020"))
    val eventList = emptyList<Event>()

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val context = LocalContext.current

    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            Log.d("debug", uri.toString())
            uploadAvatar(context, uri)

        } else {
            Log.d("debug", "no media selected")
        }
    }

    Dialog(
        onDismissRequest = { onDismissRequest() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Column(
            modifier = Modifier
                .width(screenWidth - 30.dp)
                .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
                .padding(20.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                val profileWidth = screenWidth - 30.dp - 40.dp - 44.dp
                ProfileCard(
                    containerWidth = profileWidth,
                    user = user,
                    containerColor = MaterialTheme.colorScheme.primary,
                    textColor = MaterialTheme.colorScheme.onPrimary,
                    onAvatarClick = { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)) }
                )
                Icon(
                    painter = painterResource(R.drawable.middle_icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(10.dp)
                )
                ProfileCard(
                    containerWidth = profileWidth,
                    user = partner,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            if (eventList.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(top = 35.dp, start = 15.dp, end = 15.dp, bottom = 15.dp)
                ) {
                    eventList.forEach {
                        EventCard(it.title, it.date, it.iconId)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ProfileCard(
    containerWidth: Dp,
    user: User?,
    containerColor: Color,
    textColor: Color,
    onAvatarClick: () -> Unit = {}
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .size((containerWidth / 2) - 5.dp)
//            .border(
//                width = 1.dp,
//                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
//            )
            .padding(10.dp)
    ) {
        if (user?.avatarFile == null) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(containerColor)
                    .clickable(enabled = true, onClick = { onAvatarClick() })
            ) {
                Text(
                    text = user?.getInitial() ?: "",
                    color = textColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }
        } else {
            GlideImage(
                model = user.getAvatarUrl(),
                contentDescription = "user avatar",
                contentScale = Crop,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(enabled = true, onClick = { onAvatarClick() })
            )
        }

        Text(text = user?.displayName ?: "", fontSize = 18.sp, modifier = Modifier.padding(top = 10.dp))
    }
}

@Composable
fun EventCard(
    title: String,
    date: String,
    iconId: Int?
) {
    Row {
//        Icon()
        Column {
            Text(text = title, fontWeight = FontWeight.SemiBold)
            Text(text = date)
        }
    }
}