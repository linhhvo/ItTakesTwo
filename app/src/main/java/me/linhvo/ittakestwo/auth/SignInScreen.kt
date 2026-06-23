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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import me.linhvo.ittakestwo.R

@Composable
fun SignInScreen(onSignInSuccess: () -> Unit, onCreateAccountTextClick: () -> Unit) {
    Scaffold { innerPadding ->
        SignInContent(
            onSignInSuccess = onSignInSuccess,
            onCreateAccountTextClick = onCreateAccountTextClick,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .wrapContentSize(
                    Alignment.Center
                )
        )
    }
}

@Composable
fun SignInContent(
    modifier: Modifier = Modifier,
    onSignInSuccess: () -> Unit,
    onCreateAccountTextClick: () -> Unit
) {
    val passwordTextFieldState = remember { TextFieldState() }

    val signInViewModel: SignInViewModel = viewModel()
    val email = signInViewModel.email.collectAsStateWithLifecycle()
    val signInState = signInViewModel.signInState.collectAsStateWithLifecycle()

    LaunchedEffect(signInState.value) {
        if (signInState.value) {
            onSignInSuccess()
        }
    }

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

        Column(modifier = Modifier.padding(top = 50.dp, bottom = 20.dp)) {
            OutlinedTextField(
                value = email.value,
                leadingIcon = { Icon(painter = painterResource(R.drawable.mail), contentDescription = "mail icon") },
                onValueChange = { signInViewModel.onEmailChange(it) },
                label = { Text(text = "Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.padding(bottom = 10.dp),
            )
            OutlinedSecureTextField(
                state = passwordTextFieldState,
                label = { Text(text = "Password") },
                leadingIcon = {
                    Icon(painter = painterResource(R.drawable.lock), contentDescription = "lock icon")
                }
            )
        }

        val localSoftwareKeyboardController = LocalSoftwareKeyboardController.current
        Button(
            shape = RoundedCornerShape(5.dp),
            onClick = {
                localSoftwareKeyboardController?.hide()
                signInViewModel.onSignInButtonClick(passwordTextFieldState.text)
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