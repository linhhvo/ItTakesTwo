package me.linhvo.ittakestwo.chat

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import me.linhvo.ittakestwo.R
import me.linhvo.ittakestwo.database.model.Attachment
import me.linhvo.ittakestwo.database.model.Message
import me.linhvo.ittakestwo.util.parseDateTimeToLocalTZ

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun Message(
    message: Message,
    attachments: List<Attachment>?
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp

    if (message.isSenderMe) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 5.dp)
        ) {
            val timestamp = parseDateTimeToLocalTZ(message.sentAt!!)
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
                text = "${"%02d".format(timestamp.hour)}:${"%02d".format(timestamp.minute)}",
                lineHeight = TextUnit(value = 1f, type = TextUnitType.Em),
                fontSize = 9.sp,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .alpha(.7f)
            )
            if (message.content.isNotEmpty()) {
                Text(
                    text = message.content,
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
                    screenWidth,
                    attachments,
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
                    text = message.content,
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
                    screenWidth,
                    attachments,
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

            val timestamp = parseDateTimeToLocalTZ(message.sentAt!!)
            Text(
                text = "${"%02d".format(timestamp.hour)}:${"%02d".format(timestamp.minute)}",
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
fun AttachmentGrid(screenWidth: Int, attachments: List<Attachment>, modifier: Modifier = Modifier) {
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
                    modifier = componentModifier.clickable(enabled = true, onClick = {/*TODO*/ }),
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