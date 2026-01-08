package marcinlowercase.a.ui.panel

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import marcinlowercase.a.R
import marcinlowercase.a.core.data_class.BrowserSettings
import marcinlowercase.a.core.data_class.OptionItem
import marcinlowercase.a.core.enum_class.SearchEngine
import marcinlowercase.a.ui.component.CustomIconButton
import kotlin.collections.chunked
import kotlin.collections.forEach
import kotlin.math.roundToInt

enum class SettingPanelView {
    MAIN,
    CORNER_RADIUS,
    PADDING,
    ANIMATION_SPEED,
    CURSOR_CONTAINER_SIZE,
    CURSOR_TRACKING_SPEED,
    BACK_SQUARE_OPACITY,
    DEFAULT_URL,
    INFO,
    CLOSED_TAB_HISTORY_SIZE,
    MAX_LIST_HEIGHT,
    SEARCH_ENGINE,
    SINGLE_LINE_HEIGHT

}

@Composable
fun SliderSetting(
    browserSettings: MutableState<BrowserSettings>,
    updateBrowserSettingsForSpecificValue: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onBackClick: () -> Unit,
    textFieldValueFun: (String) -> String,
    storeValueFun: (String) -> Float = {digits ->
        textFieldValueFun(digits).toFloatOrNull() ?: 0f
    },
    afterDecimal: Boolean = true,
    iconID: Int,
    digitCount: Int = 4,
    currentSettingOriginalValue: Float,
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var digits by remember {
        mutableStateOf(
            ((currentSettingOriginalValue * if (afterDecimal) 100f else 1f).roundToInt())
                .toString()
                .padStart(digitCount, '0')

        )
    }


    // 1. The raw digits are the single source of truth.
    // Initialize it from the global browserSettings.value.


    val sliderValue = (digits.toIntOrNull() ?: 0) / if (afterDecimal) 100f else 1f


    val commitTextFieldValue = {
        val parsedValue = storeValueFun(digits)
        val coercedValue = parsedValue.coerceIn(valueRange)

        // Update the global settings with the coerced value.
        updateBrowserSettingsForSpecificValue(coercedValue)

        // CRUCIAL: Update the 'digits' state based on the coerced value.
        // This forces the TextField to display the corrected number (e.g., "60.00").
        digits = ((coercedValue * if (afterDecimal) 100 else 1).roundToInt())
            .toString()
            .padStart(digitCount, '0')
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.Black.copy(alpha = 0.3f),
                shape = RoundedCornerShape(
                    browserSettings.value.cornerRadiusForLayer(2).dp
                )
            )
            .padding(browserSettings.value.padding.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    browserSettings.value.heightForLayer( 3).dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Back button to return to the main settings view

            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            browserSettings.value.cornerRadiusForLayer(3).dp
                        )
                    )
                    .fillMaxHeight()
                    .background(Color.White)
                    .defaultMinSize(
                        minWidth = browserSettings.value.heightForLayer(3).dp
                    )


            ) {
                Icon(
                    painter = painterResource(id = if(browserSettings.value.isFirstAppLoad) R.drawable.ic_check else R.drawable.ic_arrow_back),
                    contentDescription = "Back to Settings",
                    tint = Color.Black
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center // 2. Center the content of the Box
            ) {
                BasicTextField(
                    value = textFieldValueFun(digits),
                    onValueChange = {},
                    modifier = Modifier

                        .onKeyEvent { event ->
                            if (event.type == KeyEventType.KeyUp) {
                                // --- THIS IS THE CORRECTED LOGIC ---

                                // 1. Get the unicode character as an Int.
                                val unicodeChar = event.nativeKeyEvent.unicodeChar

                                // 2. Check if it's a valid character (not 0) and then convert it to a Char.
                                if (unicodeChar != 0) {
                                    val typedChar = unicodeChar.toChar()

                                    // 3. Now, safely call digitToIntOrNull() on the Char.
                                    val digit = typedChar.digitToIntOrNull()

                                    if (digit != null) {
                                        // Append new digit and keep the last 4 characters.
                                        digits = (digits + digit.toString()).takeLast(digitCount)
                                        return@onKeyEvent true // Event handled
                                    }
                                }
                                // --- END OF CORRECTION ---

                                // Check for the Backspace key
                                if (event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DEL) {
                                    digits = ("0$digits").take(digitCount)
                                    return@onKeyEvent true // Event handled
                                }
                            }
                            false // Event not handled
                        }
                        .onFocusChanged {

                            commitTextFieldValue()
                        },
                    cursorBrush = SolidColor(Color.Transparent),

                    textStyle = LocalTextStyle.current.copy(
                        color = Color.White,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number, // Show the number pad
                        imeAction = ImeAction.Done // Show a "Done" button
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {

//                            updateBrowserSettingsForSpecificValue(sliderValue.coerceIn(valueRange))
                            commitTextFieldValue()
                            // When the user presses "Done", hide the keyboard and clear focus.
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                    ),
                    singleLine = true
                )
            }
            IconButton(
                onClick = { },
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            browserSettings.value.cornerRadiusForLayer(3).dp
                        )
                    )
                    .fillMaxHeight()
                    .defaultMinSize(
                        minWidth = browserSettings.value.heightForLayer(3).dp
                    )


            ) {
                Icon(
                    painter = painterResource(id = iconID),
                    contentDescription = "Back to Settings",
                    tint = Color.White
                )
            }
        }

        Slider(
            value = sliderValue,
            onValueChange = { newSliderValue ->

                val finalValue = newSliderValue.coerceIn(valueRange)

                // 2. Update the digits string based on this final, clean value.
                digits = ((finalValue * if (afterDecimal) 100 else 1).roundToInt())
                    .toString()
                    .padStart(digitCount, '0')

                // 3. Immediately pass the NEW, CORRECT finalValue to your update function.
                updateBrowserSettingsForSpecificValue(finalValue)
            },
            valueRange = valueRange,
            steps = steps,

            modifier = Modifier
                .fillMaxWidth()
                .padding(top = browserSettings.value.padding.dp)
                .height(
                    browserSettings.value.heightForLayer(3).dp
                )
                .padding(browserSettings.value.padding.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.Black,
                inactiveTickColor = Color.White,
                activeTickColor = Color.Black,


                )
        )

    }
}
@Composable
fun TextSetting(
    browserSettings: MutableState<BrowserSettings>,
    updateBrowserSettingsForSpecificValue: (String) -> Unit, // Takes a String now
    onBackClick: () -> Unit,
    iconID: Int,
    currentSettingOriginalValue: String,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // State to hold the text being edited.
    var textValue by remember { mutableStateOf(currentSettingOriginalValue) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.Black.copy(alpha = 0.3f),
                shape = RoundedCornerShape(
                    browserSettings.value.cornerRadiusForLayer(2).dp
                )
            )
            .padding(browserSettings.value.padding.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- TOP ROW ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    browserSettings.value.heightForLayer(3).dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button (same as before)
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            browserSettings.value.cornerRadiusForLayer(3).dp
                        )
                    )
                    .fillMaxHeight()
                    .background(Color.White)
                    .defaultMinSize(
                        minWidth = browserSettings.value.heightForLayer(3).dp
                    )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "Back to Settings",
                    tint = Color.Black
                )
            }

            // --- SPACER (as requested) ---
            Spacer(modifier = Modifier.weight(1f))

            // Right Icon (same as before)
            IconButton(
                onClick = { /* No action */ },
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            browserSettings.value.cornerRadiusForLayer(3).dp
                        )
                    )
                    .fillMaxHeight()
                    .defaultMinSize(
                        minWidth = browserSettings.value.heightForLayer(3).dp
                    )
            ) {
                Icon(
                    painter = painterResource(id = iconID),
                    contentDescription = "Setting Icon",
                    tint = Color.White
                )
            }
        }

        // --- OutlinedTextField (replaces Slider) ---
        TextField(
            value = textValue,
            onValueChange = { textValue = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = browserSettings.value.padding.dp)
                .height(
                    browserSettings.value.heightForLayer(3).dp
                ),
            shape = RoundedCornerShape(
                browserSettings.value.cornerRadiusForLayer(3).dp
            ),
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
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri, // Good for URLs
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    updateBrowserSettingsForSpecificValue(textValue)
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            ),
            singleLine = true
        )
    }
}
@Composable
fun SettingsPanel(
    isSettingCornerRadius: MutableState<Boolean>,
    descriptionContent: MutableState<String>,
    backgroundColor: MutableState<Color>,
    isSettingsPanelVisible: MutableState<Boolean>,
    browserSettings: MutableState<BrowserSettings>,
    updateBrowserSettings: (BrowserSettings) -> Unit,
    confirmationPopup: (String, () -> Unit, () -> Unit) -> Unit,
    resetBrowserSettings: () -> Unit,
    targetSetting: SettingPanelView = SettingPanelView.MAIN,
) {

    var currentView by remember { mutableStateOf(targetSetting) }

    // This state will hold the current value of the slider.
//    var sliderValue by remember { mutableStateOf(browserSettings.value.deviceCornerRadius) }

    LaunchedEffect(currentView) {
        if (currentView == SettingPanelView.CORNER_RADIUS) {
            backgroundColor.value = Color.Red
            isSettingCornerRadius.value = true
            browserSettings.value = browserSettings.value.copy(isFullscreenMode = true)
        } else {
            backgroundColor.value = Color.Black
            if (isSettingCornerRadius.value) browserSettings.value = browserSettings.value.copy(isFullscreenMode = false)
            isSettingCornerRadius.value = false
            Log.e("FULLSCRFEEN", "update <- currentView changed${browserSettings.value.isFullscreenMode}")


        }

    }

    // Effect to reset the view and slider value when the panel is hidden
    LaunchedEffect(isSettingsPanelVisible.value) {
        if (!isSettingsPanelVisible.value) {
            delay(browserSettings.value.animationSpeed.toLong()) // Wait for exit animation
            currentView = SettingPanelView.MAIN
        }
    }

    // Placeholder options for the settings panel
    val allSettingsOptions = remember(browserSettings.value) {
        listOf(
            OptionItem(
                R.drawable.ic_adjust_corner_radius,
                "corner radius",
                false
            ) {
                currentView = SettingPanelView.CORNER_RADIUS
            },

            OptionItem(R.drawable.ic_link, "default url") {
                currentView = SettingPanelView.DEFAULT_URL
            },

            OptionItem(R.drawable.ic_padding, "padding") {
                currentView = SettingPanelView.PADDING
            },
            OptionItem(R.drawable.ic_search, "search engine") {
                currentView = SettingPanelView.SEARCH_ENGINE
            },
            OptionItem(R.drawable.ic_expand, "min height") {
                currentView = SettingPanelView.SINGLE_LINE_HEIGHT

            },
            OptionItem(R.drawable.ic_animation, "animation speed") {
                currentView = SettingPanelView.ANIMATION_SPEED

            },
            OptionItem(R.drawable.ic_cursor_size, "cursor size") {
                currentView = SettingPanelView.CURSOR_CONTAINER_SIZE
            },
            OptionItem(R.drawable.ic_cursor_speed, "cursor speed") {
                currentView = SettingPanelView.CURSOR_TRACKING_SPEED
            },
            OptionItem(R.drawable.ic_manage_history, "history size") {
                currentView = SettingPanelView.CLOSED_TAB_HISTORY_SIZE
            },
            OptionItem(R.drawable.ic_opacity, "back square idle opacity") {
                currentView = SettingPanelView.BACK_SQUARE_OPACITY
            },
            OptionItem(R.drawable.ic_max_list_height, "max list height") {
                currentView = SettingPanelView.MAX_LIST_HEIGHT
            },

            OptionItem(R.drawable.ic_reset_settings, "reset settings", false) {

                confirmationPopup(
                    "reset all settings?",
                    {
                        resetBrowserSettings()
                        isSettingsPanelVisible.value = false
                    },
                    {}
                )
            },

            OptionItem(R.drawable.ic_info, "info", false) {
                currentView = SettingPanelView.INFO
            },
        )
    }

    val optionPages = remember(allSettingsOptions) {
        allSettingsOptions.chunked(4)
    }
    val pagerState = rememberPagerState(pageCount = { optionPages.size })

    AnimatedVisibility(
        visible = isSettingsPanelVisible.value,
        enter = expandVertically(tween(browserSettings.value.animationSpeedForLayer(1))),
        exit = shrinkVertically(tween(if ( browserSettings.value.isFirstAppLoad) browserSettings.value.animationSpeedForLayer(0 )* 6 else browserSettings.value.animationSpeedForLayer(1)))
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = browserSettings.value.padding.dp)
                .padding(top = browserSettings.value.padding.dp)
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        browserSettings.value.cornerRadiusForLayer(2).dp
                    )
                )
                .animateContentSize(
                    tween(
                        browserSettings.value.animationSpeedForLayer(1)
                    )
                )
//
        ) {

            when (currentView) {

                SettingPanelView.MAIN -> {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth()
                    ) { pageIndex ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color.Black.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(
                                        browserSettings.value.cornerRadiusForLayer(2).dp
                                    )
                                )
//                                .padding(horizontal = browserSettings.value.padding.dp /2)
                            ,
                            horizontalArrangement = Arrangement.spacedBy(browserSettings.value.padding.dp)
                        ) {
                            val pageOptions = optionPages[pageIndex]
                            pageOptions.forEach { option ->

                                CustomIconButton(
                                    layer = 2,
                                    browserSettings = browserSettings,
                                    modifier = Modifier.weight(1f),
                                    onTap = option.onClick,
                                    descriptionContent = descriptionContent,
                                    buttonDescription = option.contentDescription,
                                    painterId = option.iconRes,
                                    isWhite = option.enabled,
                                )

                            }
//                            repeat(4 - pageOptions.size) {
//                                Spacer(modifier = Modifier.weight(1f))
//                            }
                        }
                    }
                }

                SettingPanelView.CORNER_RADIUS -> {

                    SliderSetting(
                        browserSettings = browserSettings,
                        updateBrowserSettingsForSpecificValue = { newValue ->
                            updateBrowserSettings(
                                browserSettings.value.copy(deviceCornerRadius = newValue)
                            )
                        },
                        onBackClick = { if (!browserSettings.value.isFirstAppLoad) currentView = SettingPanelView.MAIN else isSettingsPanelVisible.value = false },
                        valueRange = 0f..60f,
                        steps = 5999,
                        currentSettingOriginalValue = browserSettings.value.deviceCornerRadius,
                        textFieldValueFun = { src ->
                            src.take(2) + "." + src.substring(2, 4)
                        },
                        iconID = R.drawable.ic_adjust_corner_radius,
                    )
                }

                SettingPanelView.ANIMATION_SPEED -> {

                    SliderSetting(
                        browserSettings = browserSettings,
                        updateBrowserSettingsForSpecificValue = { newValue ->

                            updateBrowserSettings(
                                browserSettings.value.copy(animationSpeed = newValue)
                            )
                        },
                        onBackClick = { if (!browserSettings.value.isFirstAppLoad) currentView = SettingPanelView.MAIN else isSettingsPanelVisible.value = false },
                        valueRange = 0f..1000f,
                        steps = 999,
                        currentSettingOriginalValue = browserSettings.value.animationSpeed,
                        textFieldValueFun = { src ->
                            src
                        },
                        afterDecimal = false,
                        iconID = R.drawable.ic_animation
                    )
                }
                SettingPanelView.SINGLE_LINE_HEIGHT -> {

                    SliderSetting(
                        browserSettings = browserSettings,
                        updateBrowserSettingsForSpecificValue = { newValue ->

                            updateBrowserSettings(
                                browserSettings.value.copy(singleLineHeight = newValue)
                            )
                        },
                        onBackClick = { if (!browserSettings.value.isFirstAppLoad) currentView = SettingPanelView.MAIN else isSettingsPanelVisible.value = false },
                        valueRange = 65f..140f,
                        steps = 74,
                        currentSettingOriginalValue = browserSettings.value.singleLineHeight,
                        textFieldValueFun = { src ->
                            src
                        },
                        afterDecimal = false,
                        iconID = R.drawable.ic_expand
                    )
                }

                SettingPanelView.PADDING -> {

                    SliderSetting(
                        browserSettings = browserSettings,
                        updateBrowserSettingsForSpecificValue = { newValue ->

                            updateBrowserSettings(
                                browserSettings.value.copy(padding = newValue)
                            )
                        },
                        onBackClick = { if (!browserSettings.value.isFirstAppLoad) currentView = SettingPanelView.MAIN else isSettingsPanelVisible.value = false },
                        valueRange = 3f..11f,
                        steps = 7,
                        currentSettingOriginalValue = browserSettings.value.padding,
                        textFieldValueFun = { src ->
                            src
                        },
                        afterDecimal = false,
                        iconID = R.drawable.ic_padding,
                        digitCount = 2,
                    )
                }


                SettingPanelView.CURSOR_CONTAINER_SIZE -> {
                    SliderSetting(
                        browserSettings = browserSettings,
                        updateBrowserSettingsForSpecificValue = { newValue ->

                            updateBrowserSettings(
                                browserSettings.value.copy(cursorContainerSize = newValue)
                            )
                        },
                        onBackClick = { if (!browserSettings.value.isFirstAppLoad) currentView = SettingPanelView.MAIN else isSettingsPanelVisible.value = false },
                        valueRange = 20f..70f,
                        steps = 49,
                        currentSettingOriginalValue = browserSettings.value.cursorContainerSize,
                        textFieldValueFun = { src ->
                            src
                        },
                        afterDecimal = false,
                        iconID = R.drawable.ic_cursor_size,
                        digitCount = 2,
                    )
                }

                SettingPanelView.CURSOR_TRACKING_SPEED -> {
                    SliderSetting(
                        browserSettings = browserSettings,
                        updateBrowserSettingsForSpecificValue = { newValue ->

                            updateBrowserSettings(
                                browserSettings.value.copy(cursorTrackingSpeed = newValue)
                            )
                        },
                        onBackClick = { if (!browserSettings.value.isFirstAppLoad) currentView = SettingPanelView.MAIN else isSettingsPanelVisible.value = false },
                        valueRange = 0.5f..2f,
                        steps = 29,
                        currentSettingOriginalValue = browserSettings.value.cursorTrackingSpeed,
                        textFieldValueFun = { src ->
                            src[1] + "." + src.substring(2, 4)
                        },
                        afterDecimal = true,
                        iconID = R.drawable.ic_cursor_speed,
                        digitCount = 4,
                    )
                }

                SettingPanelView.DEFAULT_URL -> {
                    TextSetting(
                        browserSettings = browserSettings,
                        updateBrowserSettingsForSpecificValue = { newValue ->
                            updateBrowserSettings(browserSettings.value.copy(defaultUrl = newValue))
                        },
                        onBackClick = { if (!browserSettings.value.isFirstAppLoad) currentView = SettingPanelView.MAIN else isSettingsPanelVisible.value = false },
                        iconID = R.drawable.ic_link,
                        currentSettingOriginalValue = browserSettings.value.defaultUrl
                    )
                }

                SettingPanelView.INFO -> {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(
                                    browserSettings.value.heightForLayer(2).dp
                                ),
                        contentAlignment = Alignment.Center
                    ) {

                        Text("make by marcinlowercase")
                    }
                }

                SettingPanelView.CLOSED_TAB_HISTORY_SIZE -> {
                    SliderSetting(
                        browserSettings = browserSettings,
                        updateBrowserSettingsForSpecificValue = { newValue ->
                            updateBrowserSettings(
                                browserSettings.value.copy(closedTabHistorySize = newValue)
                            )
                        },
                        onBackClick = { if (!browserSettings.value.isFirstAppLoad) currentView = SettingPanelView.MAIN else isSettingsPanelVisible.value = false },
                        valueRange = 0f..30f, // A sensible range for this setting
                        steps = 29, // (30 / 1) - 1
                        currentSettingOriginalValue = browserSettings.value.closedTabHistorySize,
                        textFieldValueFun = { src -> src },
                        afterDecimal = false, // We are dealing with whole numbers
                        iconID = R.drawable.ic_history,
                        digitCount = 2 // Allow up to 99
                    )
                }

                SettingPanelView.BACK_SQUARE_OPACITY -> {
                    SliderSetting(
                        browserSettings = browserSettings,
                        updateBrowserSettingsForSpecificValue = { newValue ->
                            updateBrowserSettings(
                                browserSettings.value.copy(backSquareIdleOpacity = newValue)
                            )
                        },
                        onBackClick = { if (!browserSettings.value.isFirstAppLoad) currentView = SettingPanelView.MAIN else isSettingsPanelVisible.value = false },
                        valueRange = 0f..1f, // 0% to 100%
                        steps = 19, // 5% increments
                        currentSettingOriginalValue = browserSettings.value.backSquareIdleOpacity,

                        textFieldValueFun = { src ->
                            // src is "0020" -> "0.20"
                            src[1] + "." + src.substring(2, 4)
                        },
                        iconID = R.drawable.ic_opacity,
                        digitCount = 4,
                        afterDecimal = true
                    )
                }
                SettingPanelView.MAX_LIST_HEIGHT -> {
                    SliderSetting(
                        browserSettings = browserSettings,
                        updateBrowserSettingsForSpecificValue = { newValue ->
                            updateBrowserSettings(
                                browserSettings.value.copy(maxListHeight = newValue)
                            )
                        },
                        onBackClick = {  currentView = SettingPanelView.MAIN },
                        valueRange = 0f..10f,
                        steps = 99,
                        currentSettingOriginalValue = browserSettings.value.maxListHeight,

                        textFieldValueFun = { src ->
                            src.take(2) + "." + src.substring(2, 4)
                        },
                        iconID = R.drawable.ic_max_list_height,
                        digitCount = 4,
                        afterDecimal = true
                    )
                }
                SettingPanelView.SEARCH_ENGINE -> {
                    SliderSetting(
                        browserSettings = browserSettings,
                        updateBrowserSettingsForSpecificValue = { newValue ->
                            updateBrowserSettings(
                                browserSettings.value.copy(searchEngine = newValue.toInt())
                            )
                        },
                        onBackClick = {  currentView = SettingPanelView.MAIN },
                        valueRange = 0f..SearchEngine.entries.lastIndex.toFloat(),
                        steps = SearchEngine.entries.lastIndex - 1,
                        currentSettingOriginalValue = browserSettings.value.searchEngine.toFloat(),

                        textFieldValueFun = { src ->
//                            src.take(2) + "." + src.substring(2, 4)
                            SearchEngine.entries[src[1].digitToInt()].title
                        },
                        storeValueFun = { src ->
                            src[1].digitToInt().toFloat()
                        },
                        iconID = R.drawable.ic_search,
                        digitCount = 4,
                        afterDecimal = true
                    )
                }

            }

        }
    }
}