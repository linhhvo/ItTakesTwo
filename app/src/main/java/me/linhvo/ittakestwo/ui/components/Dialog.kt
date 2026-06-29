package me.linhvo.ittakestwo.ui.components

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun Dialog(
    errorMessage: String,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        text = { Text(text = errorMessage) },
        confirmButton = {
            TextButton(onClick = onDismissRequest) { Text("Close") }
        },
        shape = RoundedCornerShape(corner = CornerSize(5.dp))
    )
}