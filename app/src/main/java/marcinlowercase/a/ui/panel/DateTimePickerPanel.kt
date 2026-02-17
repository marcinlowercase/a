package marcinlowercase.a.ui.panel

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import marcinlowercase.a.R
import marcinlowercase.a.core.data_class.JsDateTimeState
import marcinlowercase.a.ui.component.CustomIconButton
import marcinlowercase.a.ui.composition.LocalBrowserSettings
import org.mozilla.geckoview.GeckoSession.PromptDelegate.DateTimePrompt
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.temporal.IsoFields

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerPanel(
    dateTimeState: MutableState<JsDateTimeState?>,
    onDismiss: () -> Unit
) {

    val settingsController = LocalBrowserSettings.current
    val settings = settingsController.current
    
    val state = dateTimeState.value ?: return
    val prompt = state.prompt
    val result = state.result

    // --- 1. INITIALIZATION (Calculate starting values synchronously) ---

    val initialValues = remember(prompt) {
        val defaultVal = prompt.defaultValue
        var date = LocalDate.now()
        var time = LocalTime.now()

        if (!defaultVal.isNullOrBlank()) {
            try {
                when (prompt.type) {
                    DateTimePrompt.Type.TIME -> {
                        time = LocalTime.parse(defaultVal)
                    }
                    DateTimePrompt.Type.DATETIME_LOCAL -> {
                        val parts = defaultVal.split("T")
                        if (parts.isNotEmpty()) date = LocalDate.parse(parts[0])
                        if (parts.size >= 2) time = LocalTime.parse(parts[1])
                    }
                    else -> {
                        // Date, Month, Week
                        date = LocalDate.parse(defaultVal)
                    }
                }
            } catch (_: Exception) {
                // If parsing fails, fallbacks are already set to .now()
            }
        }
        Pair(date, time)
    }

    var step by remember(prompt) {
        mutableIntStateOf(if (prompt.type == DateTimePrompt.Type.TIME) 1 else 0)
    }

    var selectedDate by remember(initialValues) { mutableStateOf(initialValues.first) }
    var selectedTime by remember(initialValues) { mutableStateOf(initialValues.second) }

    // --- Logic Helpers ---
    fun confirm() {
        val finalString = when (prompt.type) {
            DateTimePrompt.Type.DATE -> selectedDate.toString()
            DateTimePrompt.Type.MONTH -> "${selectedDate.year}-${selectedDate.monthValue.toString().padStart(2, '0')}"
            DateTimePrompt.Type.WEEK -> {
                val week = selectedDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
                val year = selectedDate.get(IsoFields.WEEK_BASED_YEAR)
                "${year}-W${week.toString().padStart(2, '0')}"
            }
            DateTimePrompt.Type.TIME -> selectedTime.toString()
            DateTimePrompt.Type.DATETIME_LOCAL -> "${selectedDate}T${selectedTime}"
            else -> ""
        }
        result.complete(prompt.confirm(finalString))
        onDismiss()
    }

    // --- UI CONTAINER ---
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = settings.padding.dp)
            .clip(RoundedCornerShape(settings.cornerRadiusForLayer(1).dp))
            .background(Color.Black)
            .padding(settings.padding.dp)
    ) {
        Column(
            modifier = Modifier.clip(RoundedCornerShape(settings.cornerRadiusForLayer(2).dp)),
            verticalArrangement = Arrangement.spacedBy(settings.padding.dp)
        ) {

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                    }
                },
                label = "DateToTimeAnim"
            ) { currentStep ->

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (currentStep == 0) {
                        // --- DATE PICKER ---
                        // Use ZoneOffset.UTC to ensure millisecond selection matches the date object exactly
                        val datePickerState = rememberDatePickerState(
                            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                        )

                        // Sync state change back to selectedDate
                        LaunchedEffect(datePickerState.selectedDateMillis) {
                            datePickerState.selectedDateMillis?.let {
                                selectedDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                            }
                        }

                        DatePicker(
                            state = datePickerState,
                            colors = DatePickerDefaults.colors(
                                containerColor = Color.Black,
                                titleContentColor = Color.White,
                                headlineContentColor = Color.White,
                                weekdayContentColor = Color.Gray,
                                subheadContentColor = Color.White,
                                yearContentColor = Color.White,
                                currentYearContentColor = Color.White,
                                selectedYearContentColor = Color.Black,
                                selectedYearContainerColor = Color.White,
                                dayContentColor = Color.White,
                                selectedDayContainerColor = Color.White,
                                selectedDayContentColor = Color.Black,
                                todayContentColor = Color.White,
                                todayDateBorderColor = Color.White,
                                dividerColor = Color.Transparent
                            ),
                            showModeToggle = false,
                            title = null, // Removes "Select date"
                            modifier = Modifier
                                .padding(top = settings.padding.dp)
                                .padding(horizontal = settings.padding.dp)
                        )
                    } else {
                        // --- TIME PICKER ---
                        val timePickerState = rememberTimePickerState(
                            initialHour = selectedTime.hour,
                            initialMinute = selectedTime.minute,
                            is24Hour = true
                        )

                        // Sync state continuously
                        LaunchedEffect(timePickerState.hour, timePickerState.minute) {
                            selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        }

                        TimePicker(
                            state = timePickerState,
                            colors = TimePickerDefaults.colors(
                                clockDialColor = Color.DarkGray,
                                clockDialSelectedContentColor = Color.Black,
                                clockDialUnselectedContentColor = Color.White,
                                selectorColor = Color.White,
                                periodSelectorBorderColor = Color.White,
                                periodSelectorSelectedContainerColor = Color.White,
                                periodSelectorSelectedContentColor = Color.Black,
                                periodSelectorUnselectedContentColor = Color.Gray,
                                timeSelectorSelectedContainerColor = Color.White,
                                timeSelectorSelectedContentColor = Color.Black,
                                timeSelectorUnselectedContainerColor = Color.DarkGray,
                                timeSelectorUnselectedContentColor = Color.White
                            ),
                            modifier = Modifier.padding(vertical = settings.padding.dp)
                        )
                    }
                }
            }

            // --- ACTION BUTTONS ---
            val localDescription = remember { mutableStateOf("") }

            Row(horizontalArrangement = Arrangement.spacedBy(settings.padding.dp)) {
                CustomIconButton(
                    layer = 2,
                    modifier = Modifier.weight(1f),
                    onTap = {
                        if (step == 1 && prompt.type == DateTimePrompt.Type.DATETIME_LOCAL) {
                            step = 0
                        } else {
                            result.complete(prompt.dismiss())
                            onDismiss()
                        }
                    },
                    buttonDescription = if (step == 1 && prompt.type == DateTimePrompt.Type.DATETIME_LOCAL) "back" else "cancel",
                    descriptionContent = localDescription,
                    painterId = if (step == 1 && prompt.type == DateTimePrompt.Type.DATETIME_LOCAL) R.drawable.ic_arrow_back else R.drawable.ic_close,
                    isWhite = (step == 1 && prompt.type == DateTimePrompt.Type.DATETIME_LOCAL),
                            useLongPress = false,

                    )

                CustomIconButton(
                    layer = 2,
                    modifier = Modifier.weight(1f),
                    onTap = {
                        if (step == 0 && prompt.type == DateTimePrompt.Type.DATETIME_LOCAL) {
                            step = 1
                        } else {
                            confirm()
                        }
                    },
                    buttonDescription = if (step == 0 && prompt.type == DateTimePrompt.Type.DATETIME_LOCAL) "next" else "confirm",
                    descriptionContent = localDescription,
                    painterId = if (step == 0 && prompt.type == DateTimePrompt.Type.DATETIME_LOCAL) R.drawable.ic_arrow_forward else R.drawable.ic_check,
                    isWhite = true,
                    useLongPress = false,
                )
            }
        }
    }
}