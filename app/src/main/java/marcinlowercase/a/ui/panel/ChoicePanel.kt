package marcinlowercase.a.ui.panel

import marcinlowercase.a.core.data_class.JsChoiceState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState // Added
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect // Added
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import marcinlowercase.a.R
import marcinlowercase.a.ui.component.CustomIconButton
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import org.mozilla.geckoview.GeckoSession

@Composable
fun ChoicePanel(
    choiceState: MutableState<JsChoiceState?>,
    onDismiss: () -> Unit
) {
    val viewModel = LocalBrowserViewModel.current
    val settings = viewModel.browserSettings.collectAsState()

    val prompt = choiceState.value?.prompt
    val result = choiceState.value?.result

    if (prompt == null || result == null) return

    // 1. Create the List State
    val listState = rememberLazyListState()

    // 2. Find the index of the currently selected item
    val initialSelectedIndex = remember(prompt) {
        prompt.choices.indexOfFirst { it.selected }.coerceAtLeast(0)
    }

    // 3. Auto-scroll to that index when the prompt changes or panel opens
    LaunchedEffect(prompt) {
        if (initialSelectedIndex > 0) {
            // scrollToItem is instant. Use animateScrollToItem if you want a glide effect.
            listState.scrollToItem(initialSelectedIndex)
        }
    }

    // Track selections for MULTIPLE type
    var selectedIds by remember {
        mutableStateOf(prompt.choices.filter { it.selected }.map { it.id }.toSet())
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = settings.value.padding.dp)
            .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(1).dp))
            .background(Color.Black)
            .padding(settings.value.padding.dp)
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(2).dp)),
            verticalArrangement = Arrangement.spacedBy(settings.value.padding.dp)
        ) {
            LazyColumn(
                state = listState, // Attach the state here
                modifier = Modifier
                    .heightIn(max = settings.value.maxContainerSizeForLayer(2).dp)
                    .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(2).dp)),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(prompt.choices.size) { index ->
                    val choice = prompt.choices[index]

                    // Logic for highlighting:
                    // In MULTIPLE mode, we use our local state.
                    // In SINGLE mode, we use the pre-defined selected state.
                    val isCurrentlySelected =
                        if (prompt.type == GeckoSession.PromptDelegate.ChoicePrompt.Type.MULTIPLE) {
                            selectedIds.contains(choice.id)
                        } else {
                            choice.selected
                        }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(2).dp))
                            .heightIn(min = settings.value.heightForLayer(2).dp)
                            .background(if (isCurrentlySelected) Color.White.copy(0.5f) else Color.Transparent)
                            .clickable {
                                if (prompt.type == GeckoSession.PromptDelegate.ChoicePrompt.Type.MULTIPLE) {
                                    selectedIds = if (selectedIds.contains(choice.id)) {
                                        selectedIds - choice.id
                                    } else {
                                        selectedIds + choice.id
                                    }
                                } else {
                                    // Single selection: Confirm immediately
                                    result.complete(prompt.confirm(choice.id))
                                    onDismiss()
                                }
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = choice.label,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (prompt.type == GeckoSession.PromptDelegate.ChoicePrompt.Type.MULTIPLE) {
                CustomIconButton(
                    layer = 2,
                    modifier = Modifier.fillMaxWidth(),
                    onTap = {
                        result.complete(prompt.confirm(selectedIds.toTypedArray()))
                        onDismiss()
                    },
                    buttonDescription = "submit",
                    painterId = R.drawable.ic_check,
                    isWhite = true
                )
            }
        }
    }
}