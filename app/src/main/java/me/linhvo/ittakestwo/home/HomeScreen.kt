package me.linhvo.ittakestwo.home

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import me.linhvo.ittakestwo.model.User
import me.linhvo.ittakestwo.profile.AddPartnerDialog
import me.linhvo.ittakestwo.profile.ProfileDialog
import me.linhvo.ittakestwo.ui.components.Dialog

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.getBackground()
    }

    if (uiState.errorMessage != null) {
        Dialog(errorMessage = uiState.errorMessage!!, onDismissRequest = viewModel::resetErrorMessage)
    }

    if (uiState.shouldShowProfile) {
        if (uiState.partner == null) {
            AddPartnerDialog(onEmailSubmit = viewModel::addPartner, onDismissRequest = viewModel::closeProfile)
        } else {
            ProfileDialog(
                onDismissRequest = viewModel::closeProfile,
                user = uiState.user,
                partner = uiState.partner,
                uploadAvatar = viewModel::uploadAvatar
            )
        }
    }
    HomeContent(
        user = uiState.user,
        partner = uiState.partner,
        backgroundImageUrl = uiState.backgroundImageUrl,
        signOut = viewModel::signOut,
        onProfileClick = viewModel::openProfile,
    )
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun HomeContent(
    user: User?,
    partner: User?,
    backgroundImageUrl: String?,
    signOut: () -> Unit,
    onProfileClick: () -> Unit,
) {

    val containerColorForeground1 = MaterialTheme.colorScheme.primary
    val containerColorForeground2 = MaterialTheme.colorScheme.primaryContainer
    val backgroundColor = MaterialTheme.colorScheme.surface

    if (backgroundImageUrl != null) {
        GlideImage(
            model = backgroundImageUrl,
            contentDescription = "background image",
            contentScale = Crop,
            alpha = 0.5f,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = backgroundColor)
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 80.dp, horizontal = 40.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(100.dp)
                .clickable(enabled = true, onClick = { onProfileClick() })
        ) {
            if (partner?.avatarFile == null) {
                Box(
                    modifier = Modifier
                        .offset(x = 40.dp)
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(backgroundColor)
                ) {
                    Text(
                        text = partner?.getInitial() ?: "",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .drawBehind {
                                drawCircle(color = containerColorForeground2, radius = 25.dp.toPx())
                            }
                    )
                }
            } else {
                Log.d("debug_profile", partner.getAvatarUrl().toString())
                GlideImage(
                    model = partner.getAvatarUrl(),
                    contentDescription = "user avatar",
                    contentScale = Crop,
                    modifier = Modifier
                        .offset(x = 40.dp)
                        .size(55.dp)
                        .clip(CircleShape)
                        .border(width = 1.5.dp, color = MaterialTheme.colorScheme.background, shape = CircleShape)
                )
            }

            // user's profile picture
            if (user?.avatarFile == null) {
                Box(
                    modifier = Modifier
                        .size(55.dp)
                        .clip(CircleShape)
                        .background(backgroundColor)
                ) {
                    Text(
                        text = user?.getInitial() ?: "",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .drawBehind {
                                drawCircle(color = containerColorForeground1, radius = 25.dp.toPx())
                            }
                    )
                }
            } else {
                GlideImage(
                    model = user.getAvatarUrl(),
                    contentDescription = "user avatar",
                    contentScale = Crop,
                    modifier = Modifier
                        .size(55.dp)
                        .clip(CircleShape)
                        .border(width = 1.5.dp, color = MaterialTheme.colorScheme.background, shape = CircleShape)
                )
            }
        }

        Button(onClick = { signOut() }) { Text(text = "Sign Out") }
    }
}