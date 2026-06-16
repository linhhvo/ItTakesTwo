package me.linhvo.ittakestwo.usersignin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.linhvo.ittakestwo.R

//@Preview(showBackground = true, showSystemUi = true)
@Composable
fun UserSignInScreen(onSignInButtonClicked: () -> Unit, onCreateAccountTextClicked: () -> Unit) {
    Scaffold { innerPadding ->
        UserSignIn(
            onLogInButtonClicked = onSignInButtonClicked,
            onCreateAccountTextClicked = onCreateAccountTextClicked,
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
fun UserSignIn(
    modifier: Modifier = Modifier,
    onLogInButtonClicked: () -> Unit,
    onCreateAccountTextClicked: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val state = remember { TextFieldState() }

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
                value = email,
                leadingIcon = { Icon(painter = painterResource(R.drawable.mail), contentDescription = "mail icon") },
                onValueChange = { email = it },
                label = { Text(text = "Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.padding(bottom = 10.dp),
            )
            OutlinedSecureTextField(
                state = state,
                label = { Text(text = "Password") },
                leadingIcon = {
                    Icon(painter = painterResource(R.drawable.lock), contentDescription = "lock icon")
                }
            )
        }

        Button(
            shape = RoundedCornerShape(5.dp),
            onClick = onLogInButtonClicked
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
                .clickable(enabled = true, onClick = onCreateAccountTextClicked)
        )
    }
}