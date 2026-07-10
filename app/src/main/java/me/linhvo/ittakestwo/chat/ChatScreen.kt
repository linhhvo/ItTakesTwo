package me.linhvo.ittakestwo.chat

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import me.linhvo.ittakestwo.R
import me.linhvo.ittakestwo.model.Message

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ChatScreen(navigateToHome: () -> Unit) {
    val viewModel: ChatViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            if (uiState.partner != null) {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    ), title = {
                        Text(
                            text = uiState.partner!!.displayName, fontSize = 25.sp, fontWeight = FontWeight.SemiBold
                        )
                    }, navigationIcon = {
                        IconButton(onClick = { navigateToHome() }) {
                            Icon(
                                painter = painterResource(R.drawable.back_icon),
                                contentDescription = "arrow back icon",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }, actions = {
                        Row {
                            IconButton(onClick = {}) {
                                Icon(
                                    painter = painterResource(R.drawable.search_icon),
                                    contentDescription = "search icon",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)

                                )
                            }
                            IconButton(onClick = {}) {
                                Icon(
                                    painter = painterResource(R.drawable.folder_icon),
                                    contentDescription = "file icon",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(onClick = {}) {
                                Icon(
                                    painter = painterResource(R.drawable.calendar_icon),
                                    contentDescription = "calendar icon",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    })
            }
        }) { innerPadding ->
        ChatContent(messages = uiState.chatMessages, modifier = Modifier.padding(innerPadding))
    }
}

@Composable
fun ChatContent(messages: List<Message>, modifier: Modifier = Modifier) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    LazyColumn(
        reverseLayout = true,
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(20.dp)
    ) {
        itemsIndexed(
            items = messages, key = { index, message -> message.id }) { index, currentMessage ->
            val nextMessage = messages.getOrNull(index - 1)
            val prevMessage = messages.getOrNull(index + 1)
            val isPrevMessageBySameSender = prevMessage?.sender == currentMessage.sender

            val currentMessageSentAt = currentMessage.parseDateTime(currentMessage.sentAt)

            Column {
                if (prevMessage == null
                    || currentMessageSentAt.date > prevMessage.parseDateTime(prevMessage.sentAt).date
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = .5f),
                        thickness = 0.5.dp,
                        modifier = Modifier
                            .width((screenWidth * .7).dp)
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 17.dp)
                    )
                    Text(
                        text = "${currentMessageSentAt.day} ${currentMessageSentAt.month.name}, ${currentMessageSentAt.year}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Light,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 3.dp, bottom = 15.dp)
                    )
                }
                if (prevMessage != null && !isPrevMessageBySameSender) {
                    Spacer(modifier = Modifier.height(7.dp))
                }
                Message(currentMessage, isPrevMessageBySameSender)
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun Message(
    message: Message, isPrevMessageBySameSender: Boolean
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
            Text(
                text = "${timestamp.hour}:${timestamp.minute}",
                fontSize = 9.sp,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .alpha(.7f)
                    .offset(y = 5.dp)
            )
            Text(
                text = message.content,
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
                text = "${timestamp.hour}:${timestamp.minute}",
                fontSize = 9.sp,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .alpha(.7f)
                    .offset(y = 5.dp)
            )
        }
    }
}