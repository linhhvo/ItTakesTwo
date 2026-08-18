package me.linhvo.ittakestwo.gallery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlinx.datetime.number
import me.linhvo.ittakestwo.R
import me.linhvo.ittakestwo.database.model.Attachment
import me.linhvo.ittakestwo.util.parseDateTimeToLocalTZ

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerScreen(
    viewModel: MediaViewModel = hiltViewModel(),
    attachmentId: String,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentMedia = uiState.attachment

    LaunchedEffect(attachmentId) {
        viewModel.getAttachment(attachmentId)
    }

    Scaffold(
        topBar = {
            if (uiState.shouldShowOverlay) {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { onBack() }) {
                            Icon(
                                painter = painterResource(R.drawable.back_icon),
                                contentDescription = "arrow back icon",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    title = {
                        Column {
                            Text(text = uiState.senderName ?: "", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                            if (currentMedia?.createdAt != null) {
                                val timestamp = parseDateTimeToLocalTZ(currentMedia.createdAt)
                                Text(
                                    text = "${timestamp.day}-${"%02d".format(timestamp.month.number)}-${timestamp.year}" +
                                            " at ${"%02d".format(timestamp.hour)}" +
                                            ":${"%02d".format(timestamp.minute)}",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    },
                    colors = topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f)
                    ),
                    actions = {
                        IconButton(onClick = {/*TODO*/ }, modifier = Modifier.padding(end = 10.dp)) {
                            Icon(
                                painter = painterResource(R.drawable.download_icon),
                                contentDescription = "download icon",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                )
            }
        },
    ) { _ ->
        if (currentMedia != null) {
            ViewerContent(
                attachment = currentMedia,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        enabled = true,
                        onClick = { viewModel.toggleOverlay() },
                        indication = null,
                        interactionSource = null
                    )
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ViewerContent(attachment: Attachment?, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        if (attachment != null) {
            GlideImage(
                model = attachment.filePath,
                contentDescription = attachment.fileName,
                contentScale = ContentScale.Fit,
                modifier = modifier
            )
        }
    }
}