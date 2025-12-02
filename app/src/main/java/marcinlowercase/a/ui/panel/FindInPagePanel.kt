package marcinlowercase.a.ui.panel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import marcinlowercase.a.R
import marcinlowercase.a.core.data_class.BrowserSettings
import marcinlowercase.a.core.function.buttonSettingsForLayer

@Composable
fun FindInPagePanel(
    isVisible: Boolean,
    searchText: String,
    searchResult: Pair<Int, Int>,
    onSearchTextChanged: (String) -> Unit,
    onFindNext: () -> Unit,
    onFindPrevious: () -> Unit,
    onClose: () -> Unit,
    browserSettings: BrowserSettings,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(
            tween(
                browserSettings.animationSpeedForLayer(1)
            )
        ) + fadeIn(
            tween(
                browserSettings.animationSpeedForLayer(1)
            )
        ),
        exit = shrinkVertically(
            tween(
                browserSettings.animationSpeedForLayer(1)
            )
        ) + fadeOut(
            tween(
                browserSettings.animationSpeedForLayer(1)
            )
        )
    ) {

        Column(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        browserSettings.cornerRadiusForLayer(1).dp
                    )
                )
                .padding(horizontal = browserSettings.padding.dp)
                .padding(top = browserSettings.padding.dp)

        ) {
            TextField(
                value = searchText,
                onValueChange = onSearchTextChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            browserSettings.cornerRadiusForLayer(2).dp
                        )
                    )
                    .height(
                        browserSettings.heightForLayer(2).dp
                    ),
                shape = RoundedCornerShape(
                    browserSettings.cornerRadiusForLayer(2).dp
                ),
                placeholder = { Text("find in page") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Black,
                    unfocusedContainerColor = Color.Black,
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,

                    // 3. This is the key to removing the underline
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                ),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = browserSettings.padding.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(browserSettings.padding.dp)
            ) {

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.buttonSettingsForLayer(
                        2,
                        browserSettings,
                        false,
                    )
                        .weight(1f)
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = "Close",
                        tint = Color.White

                    )
                }
                IconButton(
                    onClick = onFindPrevious,
                    enabled = searchResult.second > 0,
                    modifier = Modifier.buttonSettingsForLayer(
                        2,
                        browserSettings,
                        searchResult.second > 0
                    ).weight(1f)
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_arrow_upward),
                        contentDescription = "Previous",
                        tint = if (searchResult.second > 0) Color.Black else Color.White

                    )
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${searchResult.first}/${searchResult.second}",
                        color = Color.White,

                        )
                }
                IconButton(
                    onClick = onFindNext, enabled = searchResult.second > 0,
                    modifier = Modifier
                        .buttonSettingsForLayer(
                        2,
                        browserSettings,
                        searchResult.second > 0

                    )
                        .weight(1f)
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_arrow_downward),
                        contentDescription = "Next",
                        tint = if (searchResult.second > 0) Color.Black else Color.White
                    )
                }

            }
        }
    }
}