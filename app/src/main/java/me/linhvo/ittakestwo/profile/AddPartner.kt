package me.linhvo.ittakestwo.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun AddPartnerDialog(
    onEmailSubmit: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    var partnerEmail by rememberSaveable { mutableStateOf("") }

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(30.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            Text(text = "Enter partner's email to link accounts")
            OutlinedTextField(
                value = partnerEmail,
                onValueChange = { partnerEmail = it },
                label = { Text(text = "Partner's email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { onEmailSubmit(partnerEmail) }),
                modifier = Modifier.padding(top = 15.dp, bottom = 20.dp),
            )

            val keyboardController = LocalSoftwareKeyboardController.current
            Button(
                modifier = Modifier.align(Alignment.Start),
                shape = RoundedCornerShape(5.dp),
                onClick = {
                    keyboardController?.hide()
                    onEmailSubmit(partnerEmail)
                    onDismissRequest()
                }
            ) {
                Text(text = "Add Partner", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}