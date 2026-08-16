package me.linhvo.ittakestwo.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.linhvo.ittakestwo.R
import me.linhvo.ittakestwo.ui.components.Dialog

@Composable
fun SignInScreen(
    viewModel: SignInViewModel = hiltViewModel(),
    onCreateAccountTextClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.errorMessage != null) {
        Dialog(errorMessage = uiState.errorMessage!!, onDismissRequest = viewModel::resetErrorMessage)
    }

    SignInContent(
        email = uiState.email,
        onEmailChange = viewModel::onEmailChange,
        onSignInButtonClick = viewModel::onSignInButtonClick,
        onCreateAccountTextClick = onCreateAccountTextClick,
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(
                Alignment.Center
            )
    )
}

@Composable
fun SignInContent(
    email: String,
    onEmailChange: (String) -> Unit,
    onSignInButtonClick: (CharSequence) -> Unit,
    onCreateAccountTextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val passwordTextFieldState = remember { TextFieldState() }

    Column(
        modifier = modifier
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .safeDrawingPadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sign In",
            fontSize = 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 5.dp)
        )

        Column(
            modifier = Modifier
                .width(270.dp)
                .padding(top = 50.dp, bottom = 20.dp)
        ) {
            OutlinedTextField(
                value = email,
                leadingIcon = { Icon(painter = painterResource(R.drawable.mail), contentDescription = "mail icon") },
                onValueChange = { onEmailChange(it) },
                label = { Text(text = "Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .padding(bottom = 10.dp),
            )
            OutlinedSecureTextField(
                state = passwordTextFieldState,
                label = { Text(text = "Password") },
                leadingIcon = {
                    Icon(painter = painterResource(R.drawable.lock), contentDescription = "lock icon")
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                onKeyboardAction = { onSignInButtonClick(passwordTextFieldState.text) },
            )
        }

        val keyboardController = LocalSoftwareKeyboardController.current
        Button(
            shape = RoundedCornerShape(5.dp),
            onClick = {
                keyboardController?.hide()
                onSignInButtonClick(passwordTextFieldState.text)
            }
        ) {
            Text(text = "Sign In", fontWeight = FontWeight.SemiBold)
        }

    }
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 40.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create a new account",
            textDecoration = TextDecoration.Underline,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clickable(enabled = true, onClick = onCreateAccountTextClick)
        )
    }
}