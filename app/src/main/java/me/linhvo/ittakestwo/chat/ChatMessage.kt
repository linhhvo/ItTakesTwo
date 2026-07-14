package me.linhvo.ittakestwo.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.linhvo.ittakestwo.R
import me.linhvo.ittakestwo.model.Message

@Composable
fun Message(
    message: Message
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
            val timestamp = message.parseDateTime(message.sentAt)
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
    } else {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 5.dp)
        ) {
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

            val timestamp = message.parseDateTime(message.sentAt)
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