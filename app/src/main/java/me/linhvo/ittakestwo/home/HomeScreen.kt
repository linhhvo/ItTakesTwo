package me.linhvo.ittakestwo.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun HomeScreen(onSignOutButtonClicked: () -> Unit) {
    Scaffold { innerPadding ->
        Home(
            onSignOutButtonClicked = onSignOutButtonClicked,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
        )
    }
}

@Composable
fun Home(modifier: Modifier = Modifier, onSignOutButtonClicked: () -> Unit) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Home screen")
        Button(onClick = onSignOutButtonClicked) { Text(text = "Sign Out") }
    }
}