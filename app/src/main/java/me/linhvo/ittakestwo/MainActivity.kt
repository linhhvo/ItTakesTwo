package me.linhvo.ittakestwo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicSecureTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.AppTheme
import com.example.compose.backgroundDark
import com.example.compose.primaryDark

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UserRegistration()
        }
    }
}

@Composable
fun UserRegistrationView(modifier: Modifier = Modifier) {
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val state = remember { TextFieldState() }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
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
                value = displayName,
                leadingIcon = { Icon(painter = painterResource(R.drawable.person), contentDescription = "mail icon") },
                onValueChange = { displayName = it },
                label = { Text(text = "Name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.padding(bottom = 10.dp),
            )
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
                label = {Text(text="Password")},
                leadingIcon = {
                    Icon(painter = painterResource(R.drawable.lock), contentDescription = "lock icon")
                }
            )
        }
        Button(
            shape = RoundedCornerShape(5.dp),
            onClick = {/*TODO*/ }) {
            Text(text = "Create an account", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun UserRegistration() {
    AppTheme() {
        Scaffold { innerPadding ->
            UserRegistrationView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .wrapContentSize(Alignment.Center)
            )

        }
    }
}