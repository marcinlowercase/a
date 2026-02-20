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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import marcinlowercase.a.R
import marcinlowercase.a.ui.component.CustomIconButton
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import androidx.compose.runtime.collectAsState

@Composable
fun FindInPagePanel(
//    currentRotation: Float,
    isVisible: Boolean,
    searchText: String,
    searchResult: Pair<Int, Int>,
    onSearchTextChanged: (String) -> Unit,
    onFindNext: () -> Unit,
    onFindPrevious: () -> Unit,
    onClose: () -> Unit,
) {
    val viewModel = LocalBrowserViewModel.current
    val settings = viewModel.browserSettings.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(
            tween(
                settings.value.animationSpeedForLayer(1)
            )
        ) + fadeIn(
            tween(
                settings.value.animationSpeedForLayer(1)
            )
        ),
        exit = shrinkVertically(
            tween(
                settings.value.animationSpeedForLayer(1)
            )
        ) + fadeOut(
            tween(
                settings.value.animationSpeedForLayer(1)
            )
        )
    ) {

        Column(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        settings.value.cornerRadiusForLayer(1).dp
                    )
                )
                .padding(horizontal = settings.value.padding.dp)
                .padding(top = settings.value.padding.dp)

        ) {
            TextField(
                value = searchText,
                onValueChange = onSearchTextChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            settings.value.cornerRadiusForLayer(2).dp
                        )
                    )
                    .height(
                        settings.value.heightForLayer(2).dp
                    )
                    .onFocusChanged { focusState ->
                        viewModel.updateUI { it.copy(isFocusOnFindTextField = focusState.isFocused) }
                    },
                shape = RoundedCornerShape(
                    settings.value.cornerRadiusForLayer(2).dp
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
                    .padding(top = settings.value.padding.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(settings.value.padding.dp)
            ) {

                CustomIconButton(
                    layer = 2,
                    modifier = Modifier.weight(1f),
                    onTap = onClose,

                    buttonDescription = "cancel",
                    painterId = R.drawable.ic_arrow_back,
                    isWhite = false,
                )
                CustomIconButton(
                    layer = 2,
                    modifier = Modifier.weight(1f),
                    onTap = onFindNext,

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
                    modifier = Modifier.weight(1f),
                    onTap = onFindPrevious,

                    buttonDescription = "previous",
                    painterId = R.drawable.ic_arrow_upward,
                    isWhite = searchResult.second > 0,
                )
            }
        }
    }
}