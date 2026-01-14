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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import marcinlowercase.a.R
import marcinlowercase.a.core.data_class.BrowserSettings
import marcinlowercase.a.ui.component.CustomIconButton

@Composable
fun FindInPagePanel(
    isVisible: Boolean,
    searchText: String,
    searchResult: Pair<Int, Int>,
    onSearchTextChanged: (String) -> Unit,
    onFindNext: () -> Unit,
    onFindPrevious: () -> Unit,
    onClose: () -> Unit,
    browserSettings: MutableState<BrowserSettings>,
    descriptionContent: MutableState<String>
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(
            tween(
                browserSettings.value.animationSpeedForLayer(1)
            )
        ) + fadeIn(
            tween(
                browserSettings.value.animationSpeedForLayer(1)
            )
        ),
        exit = shrinkVertically(
            tween(
                browserSettings.value.animationSpeedForLayer(1)
            )
        ) + fadeOut(
            tween(
                browserSettings.value.animationSpeedForLayer(1)
            )
        )
    ) {

        Column(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        browserSettings.value.cornerRadiusForLayer(1).dp
                    )
                )
                .padding(horizontal = browserSettings.value.padding.dp)
                .padding(top = browserSettings.value.padding.dp)

        ) {
            TextField(
                value = searchText,
                onValueChange = onSearchTextChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            browserSettings.value.cornerRadiusForLayer(2).dp
                        )
                    )
                    .height(
                        browserSettings.value.heightForLayer(2).dp
                    ),
                shape = RoundedCornerShape(
                    browserSettings.value.cornerRadiusForLayer(2).dp
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
                    .padding(top = browserSettings.value.padding.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(browserSettings.value.padding.dp)
            ) {

                CustomIconButton(
                     layer = 2,
                    browserSettings = browserSettings,
                    modifier = Modifier.weight(1f),
                    onTap = onClose,
                    descriptionContent = descriptionContent,
                    buttonDescription = "cancel",
                    painterId = R.drawable.ic_arrow_back,
                    isWhite = false,
                )
                CustomIconButton(
                    layer = 2,
                    browserSettings = browserSettings,
                    modifier = Modifier.weight(1f),
                    onTap = onFindNext,
                    descriptionContent = descriptionContent,
                    buttonDescription = "next",
                    painterId = R.drawable.ic_arrow_downward,
                    isWhite = searchResult.second > 0,
                )



                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${searchResult.first}/${searchResult.second}",
                        color = Color.White,

                        )
                }
                CustomIconButton(
                    layer = 2,
                    browserSettings = browserSettings,
                    modifier = Modifier.weight(1f),
                    onTap = onFindPrevious,
                    descriptionContent = descriptionContent,
                    buttonDescription = "previous",
                    painterId = R.drawable.ic_arrow_upward,
                    isWhite = searchResult.second > 0,
                )
            }
        }
    }
}