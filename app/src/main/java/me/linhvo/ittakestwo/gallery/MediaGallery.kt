package me.linhvo.ittakestwo.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import me.linhvo.ittakestwo.R
import me.linhvo.ittakestwo.database.model.Attachment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    viewModel: MediaViewModel = hiltViewModel(),
    onBack: () -> Unit,
    openMediaViewer: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if (uiState.shouldShowOverlay) {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
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
                        Text(
                            text = "Shared Media",
                            fontSize = 18.sp,
                        )
                    },
                    colors = topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f)
                    ),
                )
            }
        }) { _ ->
        GalleryContent(uiState.attachmentList, openMediaViewer)
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun GalleryContent(
    attachmentList: List<Attachment>,
    openMediaViewer: (String) -> Unit,
) {
    if (attachmentList.isEmpty()) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surface)
        ) {
            Text(text = "No media to show", color = Color.Gray)
        }
    } else {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(200.dp),
            verticalItemSpacing = 4.dp,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surface)
        ) {
            items(attachmentList, key = { it.id }) { attachment ->
                if (attachment.downloaded) {
                    GlideImage(
                        model = attachment.filePath,
                        contentDescription = "message attachment",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .clickable(
                                enabled = true,
                                onClick = { openMediaViewer(attachment.id) }),
                        failure = placeholder {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(color = Color.Gray)
                            )
                        }
                    )
                } else {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = Color.Gray)
                    )
                }
            }
        }
    }
}