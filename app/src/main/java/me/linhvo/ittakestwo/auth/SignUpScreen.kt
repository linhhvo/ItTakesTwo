package me.linhvo.ittakestwo.auth

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import me.linhvo.ittakestwo.R

@Composable
fun SignUpScreen(onSignUpSuccess: () -> Unit) {
    Scaffold { innerPadding ->
        SignUpContent(
            onSignUpSuccess = onSignUpSuccess,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .wrapContentSize(Alignment.Center)
        )
    }
}

@Composable
fun SignUpContent(modifier: Modifier = Modifier, onSignUpSuccess: () -> Unit) {
    val passwordTextFieldState = remember { TextFieldState() }

    val signUpViewModel: SignUpViewModel = viewModel()
    val displayName = signUpViewModel.displayName.collectAsStateWithLifecycle()
    val email = signUpViewModel.email.collectAsStateWithLifecycle()
    val signUpState = signUpViewModel.signUpState.collectAsStateWithLifecycle()

    LaunchedEffect(signUpState.value) {
        if (signUpState.value) {
            onSignUpSuccess()
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
            text = "Getting Started",
            fontSize = 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 5.dp)
        )
        Text(text = "Create an account to continue", fontSize = 16.sp)
        Column(modifier = Modifier.padding(top = 50.dp, bottom = 20.dp)) {
            OutlinedTextField(
                value = displayName.value,
                leadingIcon = { Icon(painter = painterResource(R.drawable.person), contentDescription = "mail icon") },
                onValueChange = { signUpViewModel.onDisplayNameChange(it) },
                label = { Text(text = "Name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.padding(bottom = 10.dp),
            )
            OutlinedTextField(
                value = email.value,
                leadingIcon = { Icon(painter = painterResource(R.drawable.mail), contentDescription = "mail icon") },
                onValueChange = { signUpViewModel.onEmailChange(it) },
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
                signUpViewModel.onSignUpButtonClick(passwordTextFieldState.text)
            }
        ) {
            Text(text = "Create an account", fontWeight = FontWeight.SemiBold)
        }
    }
}