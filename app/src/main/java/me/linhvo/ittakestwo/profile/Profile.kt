package me.linhvo.ittakestwo.profile

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale.Companion.Crop
import androidx.compose.ui.platform.LocalConfiguration
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
import me.linhvo.ittakestwo.database.model.User

data class Event(
    val iconId: Int,
    val title: String,
    val date: String
)

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun ProfileDialog(
    uploadAvatar: (Uri?) -> Unit,
    onDismissRequest: () -> Unit,
    user: User?,
    partner: User?,
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    var shouldShowAvatarPreview by rememberSaveable { mutableStateOf(false) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedUri = uri
            shouldShowAvatarPreview = true
        }
    }

    if (shouldShowAvatarPreview) {
        if (selectedUri != null) {
            AvatarPreview(
                imageUri = selectedUri,
                onSaveClick = {
                    uploadAvatar(selectedUri)
                    shouldShowAvatarPreview = false
                },
                onCancelClick = {
                    shouldShowAvatarPreview = false
                },
                modifier = Modifier.sizeIn(
                    minWidth = screenWidth - 30.dp,
                    maxWidth = 300.dp,
                    minHeight = screenWidth + 40.dp,
                    maxHeight = 350.dp
                )
            )
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
                    onAvatarClick = { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
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
                    text = user?.initial ?: "",
                    color = textColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }
        } else {
            GlideImage(
                model = user.avatarPath,
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

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun AvatarPreview(
    imageUri: Uri?,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = { onCancelClick() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(10.dp))
        ) {
            GlideImage(
                model = imageUri,
                contentDescription = "user avatar preview",
                contentScale = Crop,
                modifier = Modifier
                    .weight(1f)
                    .padding(20.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 15.dp)
            ) {
                TextButton(onClick = { onSaveClick() }) { Text(text = "Save") }
                TextButton(onClick = { onCancelClick() }) { Text(text = "Cancel") }
            }
        }
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