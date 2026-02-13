package marcinlowercase.a.ui.panel

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
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
import marcinlowercase.a.core.data_class.BrowserSettings
import marcinlowercase.a.core.data_class.JsDateTimeState
import marcinlowercase.a.ui.component.CustomIconButton
import org.mozilla.geckoview.GeckoSession.PromptDelegate.DateTimePrompt
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.IsoFields

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerPanel(
    dateTimeState: MutableState<JsDateTimeState?>,
    browserSettings: MutableState<BrowserSettings>,
    descriptionContent: MutableState<String>,
    onDismiss: () -> Unit
) {
    val state = dateTimeState.value ?: return
    val prompt = state.prompt
    val result = state.result

    // 0 = Date, 1 = Time
    var step by remember { mutableIntStateOf(0) }

    // Selections
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }

    // --- Logic Helpers ---
    fun confirm() {
        val finalString = when (prompt.type) {
            DateTimePrompt.Type.DATE -> selectedDate?.toString() ?: ""
            DateTimePrompt.Type.MONTH -> selectedDate?.let { "${it.year}-${it.monthValue.toString().padStart(2, '0')}" } ?: ""
            DateTimePrompt.Type.WEEK -> selectedDate?.let {
                val week = it.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
                val year = it.get(IsoFields.WEEK_BASED_YEAR)
                "${year}-W${week.toString().padStart(2, '0')}"
            } ?: ""
            DateTimePrompt.Type.TIME -> selectedTime?.toString() ?: ""
            DateTimePrompt.Type.DATETIME_LOCAL -> {
                if (selectedDate != null && selectedTime != null) "${selectedDate}T${selectedTime}" else ""
            }
            else -> ""
        }
        result.complete(prompt.confirm(finalString))
        onDismiss()
    }

// --- INITIALIZATION ---
    LaunchedEffect(prompt) {
        if (prompt.type == DateTimePrompt.Type.TIME) {
            step = 1
        } else {
            step = 0
        }

        // 1. Capture the value locally to avoid "Smart cast impossible" error
        val defaultVal = prompt.defaultValue

        // 2. Use the local variable 'defaultVal' instead of 'prompt.defaultValue'
        if (!defaultVal.isNullOrBlank()) {
            try {
                if (prompt.type == DateTimePrompt.Type.TIME) {
                    selectedTime = LocalTime.parse(defaultVal)
                } else if (prompt.type == DateTimePrompt.Type.DATETIME_LOCAL) {
                    // Split "2023-10-25T14:30"
                    val parts = defaultVal.split("T")
                    if (parts.size == 2) {
                        selectedDate = LocalDate.parse(parts[0])
                        selectedTime = LocalTime.parse(parts[1])
                    }
                } else {
                    // Date, Month, Week
                    selectedDate = LocalDate.parse(defaultVal)
                }
            } catch (_: Exception) { }
        }
    }
    // --- UI CONTAINER ---
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = browserSettings.value.padding.dp)
            .clip(RoundedCornerShape(browserSettings.value.cornerRadiusForLayer(1).dp))
            .background(Color.Black)
            .padding(browserSettings.value.padding.dp)
    ) {
        Column(
            modifier = Modifier.clip(RoundedCornerShape(browserSettings.value.cornerRadiusForLayer(2).dp)),
            verticalArrangement = Arrangement.spacedBy(browserSettings.value.padding.dp)
        ) {

            // Use AnimatedContent to slide between Calendar and Clock
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(slideOutHorizontally { width -> -width } + fadeOut())
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(slideOutHorizontally { width -> width } + fadeOut())
                    }
                },
                label = "DateToTimeAnim"
            ) { currentStep ->

                Column(
                    modifier = Modifier
                        // Make it scrollable because DatePicker is tall
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (currentStep == 0) {
                        // --- DATE PICKER ---
                        val datePickerState = rememberDatePickerState(
                            initialSelectedDateMillis = selectedDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
                        )

                        // Sync state change
                        LaunchedEffect(datePickerState.selectedDateMillis) {
                            datePickerState.selectedDateMillis?.let {
                                selectedDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
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
                                selectedYearContentColor = Color.White,
                                selectedYearContainerColor = Color.DarkGray,
                                dayContentColor = Color.White,
                                selectedDayContainerColor = Color.White,
                                selectedDayContentColor = Color.Black,
                                todayContentColor = Color.White,
                                todayDateBorderColor = Color.White
                            ),
                            showModeToggle = false // Remove the pencil icon to keep it clean
                        )
                    } else {
                        // --- TIME PICKER ---
                        val timePickerState = rememberTimePickerState(
                            initialHour = selectedTime?.hour ?: 12,
                            initialMinute = selectedTime?.minute ?: 0,
                            is24Hour = true
                        )

                        // Sync state
                        selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)

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
                            )
                        )
                    }
                }
            }

            // --- ACTION BUTTONS ---
            Row(horizontalArrangement = Arrangement.spacedBy(browserSettings.value.padding.dp)) {
                // Left Button (Back or Cancel)
                CustomIconButton(
                    layer = 2,
                    browserSettings = browserSettings,
                    modifier = Modifier.weight(1f),
                    onTap = {
                        if (step == 1 && prompt.type == DateTimePrompt.Type.DATETIME_LOCAL) {
                            step = 0 // Go back to Date
                        } else {
                            result.complete(prompt.dismiss())
                            onDismiss()
                        }
                    },
                    buttonDescription = if (step == 1 && prompt.type == DateTimePrompt.Type.DATETIME_LOCAL) "back" else "cancel",
                    descriptionContent = descriptionContent,
                    painterId = if (step == 1 && prompt.type == DateTimePrompt.Type.DATETIME_LOCAL) R.drawable.ic_arrow_back else R.drawable.ic_close,
                    isWhite = (step == 1 && prompt.type == DateTimePrompt.Type.DATETIME_LOCAL)
                )

                // Right Button (Next or Confirm)
                CustomIconButton(
                    layer = 2,
                    browserSettings = browserSettings,
                    modifier = Modifier.weight(1f),
                    onTap = {
                        if (step == 0 && prompt.type == DateTimePrompt.Type.DATETIME_LOCAL) {
                            step = 1 // Go to Time
                        } else {
                            confirm() // Finish
                        }
                    },
                    buttonDescription = if (step == 0 && prompt.type == DateTimePrompt.Type.DATETIME_LOCAL) "next" else "confirm",
                    descriptionContent = descriptionContent,
                    painterId = if (step == 0 && prompt.type == DateTimePrompt.Type.DATETIME_LOCAL) R.drawable.ic_arrow_forward else R.drawable.ic_check,
                    isWhite = true
                )
            }
        }
    }
}