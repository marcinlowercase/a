package marcinlowercase.a.ui.panel

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import marcinlowercase.a.R
import marcinlowercase.a.core.constant.drm_access_permission
import marcinlowercase.a.core.constant.generic_location_permission
import marcinlowercase.a.core.constant.generic_notification_permission
import marcinlowercase.a.core.constant.google_drive_access_permission
import marcinlowercase.a.core.constant.local_file_storage_permission
import marcinlowercase.a.core.constant.persistent_storage_permission
import marcinlowercase.a.core.function.toDomain
import marcinlowercase.a.ui.component.CustomIconButton
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import kotlin.math.roundToInt

private enum class TabDataPanelView {
    MAIN,
    //    HISTORY,
    PERMISSIONS
}

@Composable
fun TabDataPanel(
    isTabDataPanelVisible: Boolean,
    onDismiss: () -> Unit,
    onPermissionToggle: (domain: String?, permission: String, isGranted: Boolean) -> Unit,
    onClearSiteData: () -> Unit,
    onCloseTab: () -> Unit,
) {
    val viewModel = LocalBrowserViewModel.current
    val browserSettings = viewModel.browserSettings.collectAsState()

    // 1. ADD THIS LINE: Collect uiState so Compose tracks when inspectingTabId changes!
    val uiState by viewModel.uiState.collectAsState()

    var currentView by remember { mutableStateOf(TabDataPanelView.PERMISSIONS) }
    val currentTab = viewModel.tabs.find { it.id == uiState.inspectingTabId }
    val domain = currentTab?.currentURL?.toDomain()
    val settings = viewModel.siteSettings[domain]

    AnimatedVisibility(
        visible = isTabDataPanelVisible,
        enter = fadeIn(tween(browserSettings.value.animationSpeed.roundToInt())) + expandVertically(
            expandFrom = Alignment.Bottom
        ),
        exit = shrinkVertically(tween(browserSettings.value.animationSpeedForLayer(1))) + fadeOut(
            tween(browserSettings.value.animationSpeedForLayer(1))
        )
    ) {
        Box(
            modifier = Modifier.clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .clickable(enabled = false, onClick = {})
                    .padding(top = browserSettings.value.padding.dp)
                    .padding(horizontal = browserSettings.value.padding.dp)
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
                    .clip(RoundedCornerShape(browserSettings.value.cornerRadiusForLayer(2).dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(tween(browserSettings.value.animationSpeedForLayer(1)))
                ) {


                    val isStillHaveOptions = (settings != null && settings.permissionDecisions.isNotEmpty())

                    when (currentView) {
                        TabDataPanelView.MAIN -> {
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = browserSettings.value.padding.dp)
                                    .padding(top = if (isStillHaveOptions) browserSettings.value.padding.dp else 0.dp),
                                verticalArrangement = Arrangement.spacedBy(browserSettings.value.padding.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (settings != null && settings.permissionDecisions.isNotEmpty()) {
                                    CustomIconButton(
                                        layer = 3,
                                        modifier = Modifier.fillMaxWidth(),
                                        onTap = { currentView = TabDataPanelView.PERMISSIONS },
                                        buttonDescription = stringResource(R.string.desc_permission_list),
                                        painterId = R.drawable.ic_shield_toggle,
                                        isWhite = false,
                                    )
                                }
                            }
                        }

                        TabDataPanelView.PERMISSIONS -> {
                            if (settings != null && settings.permissionDecisions.isNotEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(
                                            browserSettings.value.cornerRadiusForLayer(2).dp
                                        ))
                                        .border(browserSettings.value.padding.dp / 2, MaterialTheme.colorScheme.onSurface,RoundedCornerShape(
                                            browserSettings.value.cornerRadiusForLayer(2).dp
                                        ))
//                                        .padding(horizontal = browserSettings.value.padding.dp)
//                                        .padding(top = browserSettings.value.padding.dp)
                                        .padding(browserSettings.value.padding.dp)
                                    ,
                                    verticalArrangement = Arrangement.spacedBy(browserSettings.value.padding.dp)
                                ) {
                                    settings.permissionDecisions.entries.toList().chunked(4).reversed().forEach { rowPermissions ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(browserSettings.value.padding.dp)
                                        ) {
                                            rowPermissions.forEach { (permission, isGranted) ->
                                                val (allowIcon, denyIcon, nameResId) = when (permission) {
                                                    generic_location_permission -> Triple(R.drawable.ic_location_on, R.drawable.ic_location_off, R.string.desc_permission_location)
                                                    google_drive_access_permission -> Triple(R.drawable.ic_drive_access_allow, R.drawable.ic_drive_access_deny, R.string.desc_permission_drive_access)
                                                    local_file_storage_permission -> Triple(R.drawable.ic_write_file, R.drawable.ic_write_file_deny, R.string.desc_permission_save_file_to_device)

                                                    Manifest.permission.CAMERA -> Triple(R.drawable.ic_camera_on, R.drawable.ic_camera_off, R.string.desc_permission_camera)
                                                    Manifest.permission.RECORD_AUDIO -> Triple(R.drawable.ic_mic_on, R.drawable.ic_mic_off, R.string.desc_permission_microphone)
                                                    Manifest.permission.POST_NOTIFICATIONS, generic_notification_permission -> Triple(R.drawable.ic_notifications, R.drawable.ic_notifications_off, R.string.desc_permission_notifications)
                                                    persistent_storage_permission -> Triple(R.drawable.ic_persistent_storage, R.drawable.ic_persistent_storage_deny, R.string.desc_permission_storage)
                                                    drm_access_permission -> Triple(R.drawable.ic_media_output, R.drawable.ic_media_output_off, R.string.desc_permission_drm)
                                                    else -> Triple(R.drawable.ic_bug, R.drawable.ic_bug, R.string.desc_permission_unknown)
                                                }

                                                CustomIconButton(
                                                    layer = 3,
                                                    modifier = Modifier.weight(1f),
                                                    onTap = { onPermissionToggle(domain, permission, !isGranted) },
                                                    buttonDescription = stringResource(nameResId),
                                                    painterId = if (isGranted) allowIcon else denyIcon,
                                                    isWhite = isGranted,
                                                )
                                            }

                                            // Fills remaining spots on incomplete rows so buttons stay 1/4th width
                                            repeat(4 - rowPermissions.size) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }                    }
                }

                // action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(browserSettings.value.padding.dp),
                    horizontalArrangement = Arrangement.spacedBy(browserSettings.value.padding.dp)
                ) {

                    // 4. Update this to use `currentTab` instead of the non-observed getter
                    CustomIconButton(
                        layer = 3,
                        modifier = Modifier.weight(1f),
                        onTap = onClearSiteData,
                        buttonDescription = stringResource(R.string.desc_clear_site_data),
                        painterId = R.drawable.ic_database_off
                    )

                    CustomIconButton(
                        layer = 3,
                        modifier = Modifier.weight(1f),
                        onTap = { viewModel.duplicateInspectedTab() },
                        buttonDescription = stringResource(R.string.desc_duplicate_tab),
                        painterId = R.drawable.ic_tab_duplicate
                    )

                    CustomIconButton(
                        layer = 3,
                        modifier = Modifier.weight(1f),
                        onTap = onCloseTab,
                        buttonDescription = stringResource(R.string.desc_close_tab),
                        painterId = R.drawable.ic_tab_close
                    )

                    CustomIconButton(
                        layer = 3,
                        modifier = Modifier.weight(1f),
                        onTap = { viewModel.moveInspectedTabToNextProfile() },
                        buttonDescription = stringResource(R.string.desc_move_tab_to_next_profile),
                        painterId = R.drawable.ic_tab_move
                    )
                }
            }
        }
    }
}