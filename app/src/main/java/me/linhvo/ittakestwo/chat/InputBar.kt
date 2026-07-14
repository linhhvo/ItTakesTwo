package me.linhvo.ittakestwo.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import me.linhvo.ittakestwo.R

@Composable
fun InputBar(
    userInput: String,
    onInputChange: (String) -> Unit,
    sendMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val screenHeight = LocalConfiguration.current.screenHeightDp

    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(15.dp),
        modifier = modifier
            .heightIn(min = 45.dp, max = (screenHeight * .3).dp)
            .width((screenWidth * .9).dp)
            .background(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = RoundedCornerShape(8.dp))
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(45.dp)
                .padding(start = 15.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.file_icon),
                contentDescription = "file icon",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .height(24.dp)
                    .clickable(
                        enabled = true,
                        onClick = {/*TODO*/ },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() })
            )
            Icon(
                painter = painterResource(R.drawable.cat_icon),
                contentDescription = "emoji icon",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .height(30.dp)
                    .clickable(
                        enabled = true,
                        onClick = {/*TODO*/ },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() })
            )
        }

        BasicTextField(
            value = userInput,
            onValueChange = { onInputChange(it) },
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            textStyle = LocalTextStyle.current.copy(
                color = MaterialTheme.colorScheme.onSurface, lineHeight = TextUnit(
                    1.3f,
                    TextUnitType.Em
                )
            ),
            decorationBox = { innerTextField ->
                Row(
                    Modifier
                        .background(Color.Transparent)
                        .padding(vertical = 10.dp)
                ) {
                    innerTextField()
                }
            },
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        )

        val keyboardController = LocalSoftwareKeyboardController.current

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(45.dp)) {
            Icon(
                painter = painterResource(R.drawable.send_icon),
                contentDescription = "send icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(end = 15.dp)
                    .clickable(
                        enabled = true,
                        onClick = {
                            keyboardController?.hide()
                            sendMessage()
                        },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() })
            )
        }
    }
}