package me.linhvo.ittakestwo.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import me.linhvo.ittakestwo.model.Message

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navigateToHome: () -> Unit) {
    val viewModel: ChatViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(reverseLayout = true)

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = ScaffoldDefaults
            .contentWindowInsets
            .exclude(WindowInsets.ime),
        topBar = {
            if (uiState.partner != null) {
                ChatTopBar(
                    partner = uiState.partner!!,
                    scrollBehavior = scrollBehavior,
                    navigateToHome = navigateToHome
                )
            }
        }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ChatContent(
                messages = uiState.chatMessages,
                modifier = Modifier.weight(1f)
            )
            InputBar(
                userInput = uiState.userInput,
                onInputChange = viewModel::onInputChange,
                modifier = Modifier.padding(bottom = 10.dp)

            )
        }
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
            .padding(top = 0.dp, start = 20.dp, end = 20.dp, bottom = 10.dp)
    ) {
        itemsIndexed(
            items = messages, key = { _, message -> message.id }) { index, currentMessage ->
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
                Message(currentMessage)
            }
        }
    }
}