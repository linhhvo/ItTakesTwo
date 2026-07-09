package me.linhvo.ittakestwo.chat

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
                    ),
                    title = {
                        Text(
                            text = uiState.partner!!.displayName,
                            fontSize = 25.sp,
                            fontWeight = FontWeight.SemiBold
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
                    }
                )
            }
        }
    ) { innerPadding ->
        ChatContent(uiState.chatMessages, modifier = Modifier.padding(innerPadding))
    }
}

@Composable
fun ChatContent(messages: List<Message>, modifier: Modifier = Modifier) {
    LazyColumn(
        reverseLayout = true,
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(20.dp)
    ) {
        items(
            items = messages,
            key = { message -> message.id }
        ) { message ->
            Log.d("debug_message", message.toString())
            Message(message)
        }
    }
}

@Composable
fun Message(message: Message) {
    val screenWidth = LocalConfiguration.current.screenWidthDp

    if (message.isSenderMe) {
        Column(horizontalAlignment = Alignment.End) {
            Text(text = message.sender)

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 7.dp)
            ) {
                Text(
                    text = "21:17",
                    fontSize = 8.sp,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .alpha(.7f)
                )
                Text(
                    text = message.content,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = RoundedCornerShape(
                                topStart = 8.dp,
                                topEnd = 8.dp,
                                bottomStart = 8.dp,
                                bottomEnd = 0.dp
                            )
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .weight(1f, fill = false)
                        .widthIn(max = (screenWidth * 0.7).dp)
                )
            }
        }
    } else {
        Column(horizontalAlignment = Alignment.Start) {
            Text(text = message.sender)
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 7.dp)
            ) {
                Text(
                    text = message.content,
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.secondary,
                            shape = RoundedCornerShape(
                                topStart = 8.dp,
                                topEnd = 8.dp,
                                bottomStart = 0.dp,
                                bottomEnd = 8.dp
                            )
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .weight(1f, fill = false)
                        .widthIn(max = (screenWidth * 0.7).dp)

                )
                Text(
                    text = "21:17",
                    fontSize = 8.sp,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .alpha(.7f)
                )
            }
        }
    }
}