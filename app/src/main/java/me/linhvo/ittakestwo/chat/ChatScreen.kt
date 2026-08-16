package me.linhvo.ittakestwo.chat

import android.Manifest
import android.os.Build
import android.widget.photopicker.EmbeddedPhotoPickerFeatureInfo
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.photopicker.compose.EmbeddedPhotoPicker
import androidx.photopicker.compose.ExperimentalPhotoPickerComposeApi
import androidx.photopicker.compose.rememberEmbeddedPhotoPickerState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.linhvo.ittakestwo.database.model.Attachment
import me.linhvo.ittakestwo.database.model.Message
import me.linhvo.ittakestwo.ui.components.Dialog
import me.linhvo.ittakestwo.util.parseDateTimeToLocalTZ

@RequiresExtension(extension = Build.VERSION_CODES.UPSIDE_DOWN_CAKE, version = 23)
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPhotoPickerComposeApi::class, ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel = hiltViewModel(), navigateToHome: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val activity = LocalActivity.current
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val shouldPromptAgain = activity?.let {
            !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)
        } ?: true
        if (isGranted) {
            viewModel.updateNotiPermissionResponse()
        } else if (!shouldPromptAgain) {
            viewModel.updateNotiPermissionResponse()
        }
    }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )
    val scope = rememberCoroutineScope()

    val mediaPickerInfo =
        EmbeddedPhotoPickerFeatureInfo.Builder().setMaxSelectionLimit(10).setOrderedSelection(true).build()

    val mediaPickerState = rememberEmbeddedPhotoPickerState(
        onSelectionComplete = { scope.launch { scaffoldState.bottomSheetState.hide() } },
        onUriPermissionGranted = { viewModel.onFileSelection(context, it) },
        onUriPermissionRevoked = { viewModel.onFileDeselection(it) }
    )

    val bottomPadding = if (!scaffoldState.bottomSheetState.isVisible && WindowInsets.isImeVisible) {
        WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    } else if (!scaffoldState.bottomSheetState.isVisible && !WindowInsets.isImeVisible) {
        WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
    } else {
        0.dp
    }

    BackHandler(enabled = scaffoldState.bottomSheetState.isVisible) {
        scope.launch {
            scaffoldState.bottomSheetState.hide()
        }
    }

    if (uiState.errorMessage != null) {
        Dialog(errorMessage = uiState.errorMessage!!, onDismissRequest = viewModel::resetErrorMessage)
    }

    BottomSheetScaffold(
        topBar = {
            ChatTopBar(
                partner = uiState.partner,
                navigateToHome = navigateToHome
            )
        },
        scaffoldState = scaffoldState,
        sheetPeekHeight = if (scaffoldState.bottomSheetState.isVisible) 350.dp else 0.dp,
        sheetMaxWidth = LocalConfiguration.current.screenWidthDp.dp,
        sheetContent = {
            EmbeddedPhotoPicker(
                state = mediaPickerState,
                embeddedPhotoPickerFeatureInfo = mediaPickerInfo,
                modifier = Modifier.fillMaxWidth()
            )
        },
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = bottomPadding)
                .padding(innerPadding)
        ) {
            if (uiState.showNotiPermissionRequest) {
                NotificationPermissionAlert(onAllowClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                })
            }
            ChatContent(
                messages = uiState.chatMessages,
                downloadAttachments = viewModel::downloadAttachments,
                markAsRead = viewModel::markAsRead,
                modifier = Modifier.weight(1f)
            )
            InputBar(
                userInput = uiState.userInput,
                onInputChange = viewModel::onInputChange,
                sendMessage = {
                    viewModel.sendMessage()
                    scope.launch {
                        mediaPickerState.deselectUris(mediaPickerState.selectedMedia.toList())
                    }
                },
                modifier = Modifier.padding(bottom = 10.dp),
                toggleMediaPicker = {
                    scope.launch {
                        if (scaffoldState.bottomSheetState.isVisible) {
                            scaffoldState.bottomSheetState.hide()
                            mediaPickerState.deselectUris(mediaPickerState.selectedMedia.toList())
                        } else {
                            scaffoldState.bottomSheetState.partialExpand()
                        }
                    }
                },
                hideMediaPicker = {
                    scope.launch {
                        if (scaffoldState.bottomSheetState.isVisible) {
                            scaffoldState.bottomSheetState.hide()
                            mediaPickerState.deselectUris(mediaPickerState.selectedMedia.toList())
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun ChatContent(
    messages: Map<Message, List<Attachment>?>,
    downloadAttachments: (List<Attachment>?) -> Unit,
    markAsRead: () -> Unit,
    modifier: Modifier = Modifier
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val listState = rememberLazyListState()

    val messageList = messages.keys.toList()

    LaunchedEffect(messages.size) {
        if (listState.firstVisibleItemIndex <= 1) {
            listState.animateScrollToItem(0)
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .map { index -> index == 0 }
            .distinctUntilChanged()
            .filter { it }
            .collect { markAsRead() }
    }

    LazyColumn(
        state = listState,
        reverseLayout = true,
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(top = 0.dp, start = 20.dp, end = 20.dp, bottom = 10.dp)
    ) {
        itemsIndexed(
            items = messageList, key = { _, message -> message.id }) { index, currentMessage ->
            val prevMessage = messageList.getOrNull(index + 1)
            val isPrevMessageBySameSender = prevMessage?.senderId == currentMessage.senderId

            val currentMessageSentAt = parseDateTimeToLocalTZ(currentMessage.sentAt!!)

            LaunchedEffect(messages[currentMessage]) {
                if (currentMessage.attachments) {
                    downloadAttachments(messages[currentMessage])
                }
            }

            Column {
                if (prevMessage == null
                    || currentMessageSentAt.date > parseDateTimeToLocalTZ(prevMessage.sentAt!!).date
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
                Message(
                    currentMessage,
                    messages[currentMessage]
                )
            }
        }
    }
}