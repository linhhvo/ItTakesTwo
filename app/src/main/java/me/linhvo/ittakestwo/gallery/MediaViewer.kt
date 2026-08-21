package me.linhvo.ittakestwo.gallery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlinx.datetime.number
import me.linhvo.ittakestwo.R
import me.linhvo.ittakestwo.database.model.Attachment
import me.linhvo.ittakestwo.util.parseDateTimeToLocalTZ
import kotlin.math.max
import kotlin.math.min

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
                toggleOverlay = viewModel::toggleOverlay,
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
fun ViewerContent(attachment: Attachment?, toggleOverlay: () -> Unit, modifier: Modifier = Modifier) {
    var scale by remember { mutableFloatStateOf(1f) }
    var size by remember { mutableStateOf(Size.Zero) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val state = rememberTransformableState { centroid, zoomChange, offsetChange, _ ->
        val oldScale = scale
        val newScale = min(max(scale * zoomChange, 1f), 15f)

        val effectiveCentroid = centroid.takeIf { it.isSpecified } ?: size.center

        offset = (offset + effectiveCentroid / oldScale) - (effectiveCentroid / newScale + offsetChange / oldScale)
        scale = newScale
    }

    if (attachment != null) {
        GlideImage(
            model = attachment.filePath,
            contentDescription = attachment.fileName,
            contentScale = ContentScale.Fit,
            modifier = modifier
                .onSizeChanged { size = it.toSize() }
                .transformable(state = state)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = -offset.x * scale,
                    translationY = -offset.y * scale,
                    transformOrigin = TransformOrigin(0f, 0f)
                )
        )
    }
}