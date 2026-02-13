package marcinlowercase.a.ui.panel

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import marcinlowercase.a.core.data_class.JsDateTimeState
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
    onDismiss: () -> Unit
) {
    val state = dateTimeState.value ?: return
    val prompt = state.prompt
    val result = state.result

    // 0 = Date Picker, 1 = Time Picker
    var step by remember { mutableIntStateOf(0) }

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }

    // Logic to format the data exactly how HTML expects it
    fun confirm() {
        val finalString = when (prompt.type) {
            DateTimePrompt.Type.DATE -> selectedDate?.toString() ?: "" // "yyyy-MM-dd"

            DateTimePrompt.Type.MONTH -> {
                // "yyyy-MM"
                selectedDate?.let { "${it.year}-${it.monthValue.toString().padStart(2, '0')}" } ?: ""
            }

            DateTimePrompt.Type.WEEK -> {
                // "yyyy-Www" (e.g., 2023-W05)
                selectedDate?.let {
                    val week = it.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
                    val year = it.get(IsoFields.WEEK_BASED_YEAR)
                    "${year}-W${week.toString().padStart(2, '0')}"
                } ?: ""
            }

            DateTimePrompt.Type.TIME -> selectedTime?.toString() ?: "" // "HH:mm"

            DateTimePrompt.Type.DATETIME_LOCAL -> {
                // "yyyy-MM-ddTHH:mm"
                if (selectedDate != null && selectedTime != null) {
                    "${selectedDate}T${selectedTime}"
                } else ""
            }

            else -> ""
        }

        result.complete(prompt.confirm(finalString))
        onDismiss()
    }

    fun cancel() {
        result.complete(prompt.dismiss())
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
    // --- STEP 0: DATE PICKER ---
    // Used for: DATE, MONTH, WEEK, DATETIME_LOCAL
    if (step == 0) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
                ?: System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { cancel() },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    }

                    // If it is DATETIME_LOCAL, we need to go to Step 1 (Time).
                    // For DATE, MONTH, WEEK, we are done.
                    if (prompt.type == DateTimePrompt.Type.DATETIME_LOCAL) {
                        step = 1
                    } else {
                        confirm()
                    }
                }) { Text("OK", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { cancel() }) { Text("Cancel", color = Color.White) }
            },
            colors = DatePickerDefaults.colors(
                containerColor = Color.Black,
                yearContentColor = Color.White,
                navigationContentColor = Color.White,
                headlineContentColor = Color.White,
                selectedDayContainerColor = Color.White,
                selectedDayContentColor = Color.Black,
                todayContentColor = Color.White,
                dayContentColor = Color.White,
                currentYearContentColor = Color.White,
                weekdayContentColor = Color.Gray
            )
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // --- STEP 1: TIME PICKER ---
    // Used for: TIME, DATETIME_LOCAL
    if (step == 1) {
        val initialHour = selectedTime?.hour ?: 12
        val initialMinute = selectedTime?.minute ?: 0
        val timePickerState = rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { cancel() },
            containerColor = Color.Black,
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = Color.DarkGray,
                            clockDialSelectedContentColor = Color.Black,
                            clockDialUnselectedContentColor = Color.White,
                            selectorColor = Color.White,
                            periodSelectorBorderColor = Color.White,
                            periodSelectorSelectedContainerColor = Color.White.copy(0.3f),
                            periodSelectorUnselectedContainerColor = Color.Transparent,
                            periodSelectorSelectedContentColor = Color.White,
                            periodSelectorUnselectedContentColor = Color.Gray,
                            timeSelectorSelectedContainerColor = Color.White.copy(0.3f),
                            timeSelectorUnselectedContainerColor = Color.Transparent,
                            timeSelectorSelectedContentColor = Color.White,
                            timeSelectorUnselectedContentColor = Color.Gray
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    confirm()
                }) { Text("OK", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = {
                    // If this was a combined prompt, Back goes to Date.
                    // If this was just a Time prompt, Back cancels.
                    if (prompt.type == DateTimePrompt.Type.DATETIME_LOCAL) {
                        step = 0
                    } else {
                        cancel()
                    }
                }) { Text("Back", color = Color.White) }
            }
        )
    }
}