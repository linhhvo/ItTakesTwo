package me.linhvo.ittakestwo.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.linhvo.ittakestwo.R

@Preview(showSystemUi = true, showBackground = true, device = "id:pixel_10_pro")
@Composable
fun HomeScreen() {
    Scaffold { innerPadding ->
        HomeContent(
//            onSignOutButtonClicked = onSignOutButtonClicked,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        )
    }
}

@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
//                onSignOutButtonClicked: () -> Unit
) {
    val containerColorForeground1 = MaterialTheme.colorScheme.primary
    val containerColorForeground2 = MaterialTheme.colorScheme.primaryContainer
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 30.dp, horizontal = 20.dp)
                .align(Alignment.Start)
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
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(color = MaterialTheme.colorScheme.primary, width = 2.dp, shape = RoundedCornerShape(12.dp))
        ) {
            Image(
                painter = painterResource(R.drawable.home_image),
                contentDescription = null,
                alpha = 0.9f
            )
        }

//        Row(modifier = Modifier.padding(30.dp)) {
//            Text(text = "Some widgets here")
//        }
//        Button(onClick = onSignOutButtonClicked) { Text(text = "Sign Out") }
    }
}