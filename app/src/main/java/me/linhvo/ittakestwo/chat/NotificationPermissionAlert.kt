package me.linhvo.ittakestwo.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

@Composable
fun NotificationPermissionAlert(onAllowClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.surfaceContainerHighest)
            .fillMaxWidth()
            .padding(horizontal = 15.dp, vertical = 5.dp)
    ) {
        Text(text = "Get notified when you receive a message", fontWeight = FontWeight.Light)
        Text(
            text = "Allow",
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.tertiary,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable(enabled = true, onClick = { onAllowClick() })
        )
    }
}