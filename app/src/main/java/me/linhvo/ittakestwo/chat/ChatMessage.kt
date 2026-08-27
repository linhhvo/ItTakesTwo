package me.linhvo.ittakestwo.chat

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import me.linhvo.ittakestwo.R
import me.linhvo.ittakestwo.database.model.Attachment
import me.linhvo.ittakestwo.database.model.Message

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun Message(
    message: Message,
    metadata: MessageUiMetadata?,
    attachments: List<Attachment>?,
    openMediaViewer: (String) -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp

    val timestamp = metadata?.formattedSentAt

    if (message.isSenderMe) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 5.dp)
        ) {
            val iconTint =
                if (message.readAt != null) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.tertiaryContainer
            Icon(
                painter = painterResource(R.drawable.read_icon),
                contentDescription = "read icon",
                tint = iconTint,
                modifier = Modifier
                    .size(12.dp)
                    .padding(end = 3.dp)
            )
            Text(
                text = "${"%02d".format(timestamp?.hour)}:${"%02d".format(timestamp?.minute)}",
                lineHeight = TextUnit(value = 1f, type = TextUnitType.Em),
                fontSize = 9.sp,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .alpha(.7f)
            )
            if (message.content.isNotEmpty()) {
                Text(
                    text = styledMessageContent(metadata?.urlPositions ?: emptyList(), message.content),
                    lineHeight = TextUnit(value = 1.3f, type = TextUnitType.Em),
                    letterSpacing = 0.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(
                                topStart = 9.dp, topEnd = 9.dp, bottomStart = 9.dp, bottomEnd = 0.dp
                            )
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .weight(1f, fill = false)
                        .widthIn(max = (screenWidth * 0.75).dp)
                )
            }

            if (message.attachments && !attachments.isNullOrEmpty()) {
                AttachmentGrid(
                    attachments,
                    openMediaViewer,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(
                                topStart = 9.dp, topEnd = 9.dp, bottomStart = 9.dp, bottomEnd = 0.dp
                            )
                        )
                        .padding(4.dp)
                        .weight(1f, fill = false)
                )
            }
        }
    } else {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 5.dp)
        ) {
            if (message.content.isNotEmpty()) {
                Text(
                    text = styledMessageContent(metadata?.urlPositions ?: emptyList(), message.content),
                    color = MaterialTheme.colorScheme.onSecondary,
                    lineHeight = TextUnit(value = 1.3f, type = TextUnitType.Em),
                    letterSpacing = 0.sp,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.secondary, shape = RoundedCornerShape(
                                topStart = 9.dp, topEnd = 9.dp, bottomStart = 0.dp, bottomEnd = 9.dp
                            )
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .weight(1f, fill = false)
                        .widthIn(max = (screenWidth * 0.75).dp)
                )
            }

            if (message.attachments && !attachments.isNullOrEmpty()) {
                AttachmentGrid(
                    attachments,
                    openMediaViewer,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.secondary, shape = RoundedCornerShape(
                                topStart = 9.dp, topEnd = 9.dp, bottomStart = 0.dp, bottomEnd = 9.dp
                            )
                        )
                        .padding(4.dp)
                        .weight(1f, fill = false)
                )
            }

            Text(
                text = "${"%02d".format(timestamp?.hour)}:${"%02d".format(timestamp?.minute)}",
                fontSize = 9.sp,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .alpha(.7f)
                    .offset(y = 5.dp)
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun AttachmentGrid(
    attachments: List<Attachment>,
    openMediaViewer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        maxItemsInEachRow = 3,
        modifier = modifier
    ) {
        val componentModifier = if (attachments.size < 3) Modifier
            .widthIn(max = (screenWidth * .6).dp)
            .fillMaxWidth(1 / attachments.size.toFloat())
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
        else Modifier
            .fillMaxWidth(0.3f)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))

        attachments.forEach { attachment ->
            if (attachment.downloaded) {
                GlideImage(
                    model = attachment.filePath,
                    contentDescription = "message attachment",
                    contentScale = ContentScale.Crop,
                    modifier = componentModifier.clickable(
                        enabled = true,
                        onClick = { openMediaViewer(attachment.id) }),
                    failure = placeholder {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(color = Color.Gray)
                        )
                    }
                )
            } else {
                Spacer(
                    modifier = componentModifier.background(color = Color.Gray)
                )
            }
        }
    }
}

@Composable
fun styledMessageContent(
    urlPositions: List<Pair<Int, Int>>,
    originalContent: String
): AnnotatedString {
    val context = LocalContext.current
    if (!urlPositions.isEmpty()) {
        return buildAnnotatedString {
            var start = 0
            urlPositions.forEach { urlPos ->
                val validUrl = if (originalContent.substring(urlPos.first, urlPos.second)
                        .startsWith("http://") || originalContent.substring(urlPos.first, urlPos.second)
                        .startsWith("https://")
                ) {
                    originalContent.substring(urlPos.first, urlPos.second)
                } else "https://" + originalContent.substring(urlPos.first, urlPos.second)

                append(originalContent.substring(start, urlPos.first))
                withLink(
                    LinkAnnotation.Url(
                        validUrl,
                        TextLinkStyles(style = SpanStyle(textDecoration = TextDecoration.Underline))
                    ) {
                        val intent =
                            Intent(Intent.ACTION_VIEW, validUrl.toUri())
                        try {
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            context.startActivity(Intent.createChooser(intent, "Select app to open with"))
                        }
                    }
                ) {
                    append(originalContent.substring(urlPos.first, urlPos.second))
                }
                start = urlPos.second
            }
            append(originalContent.substring(start, originalContent.length))
        }
    } else {
        return buildAnnotatedString { append(originalContent) }
    }
}