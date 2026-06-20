package me.linhvo.ittakestwo.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.ContentScale.Companion.Crop
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.linhvo.ittakestwo.R

@Composable
fun HomeScreen(
    onSignOutButtonClicked: () -> Unit
) {
    HomeContent(
        onSignOutButtonClicked = onSignOutButtonClicked,
    )
}

@Composable
fun HomeContent(
    onSignOutButtonClicked: () -> Unit
) {
    val containerColorForeground1 = MaterialTheme.colorScheme.primary
    val containerColorForeground2 = MaterialTheme.colorScheme.primaryContainer

    Image(
        painter = painterResource(R.drawable.home_image),
        contentDescription = null,
        alpha = 0.5f,
        contentScale = Crop,
        modifier = Modifier.fillMaxSize(),
    )

    Box(
        modifier = Modifier
            .padding(vertical = 80.dp, horizontal = 40.dp)
    ) {
        // partner's profile picture
        Box(
            modifier = Modifier
                .offset(x = 40.dp)
                .size(54.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Text(
                text = "H",
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

        // user's profile picture
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Text(
                text = "L",
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
    }


//    Button(onClick = onSignOutButtonClicked, modifier = Modifier.padding(100.dp)) { Text(text = "Sign Out") }
}