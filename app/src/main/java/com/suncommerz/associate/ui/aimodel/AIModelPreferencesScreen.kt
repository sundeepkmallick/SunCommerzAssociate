package com.suncommerz.associate.ui.aimodel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.suncommerz.associate.ai.AIBackend
import com.suncommerz.associate.ai.download.ModelDownloadStatus

@Composable
fun AIModelPreferencesScreen(
    viewModel: AIModelPreferencesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Choose which AI backend AI Pickup should prefer. If it is unavailable or fails to start, the app safely uses Firebase ADK.",
                style = MaterialTheme.typography.bodyMedium,
            )

            BackendPreferenceCard(
                title = "Firebase ADK",
                description = "Cloud-based AI. Available by default and used as the fallback for other backends.",
                selected = state.preferredBackend == AIBackend.FIREBASE,
                enabled = true,
                status = "Default fallback",
                onSelect = { viewModel.selectBackend(AIBackend.FIREBASE) },
            )

            BackendPreferenceCard(
                title = "Gemini Nano",
                description = "On-device AI managed by Android AICore. SunCommerz Associate does not download or manage this model.",
                selected = state.preferredBackend == AIBackend.GEMINI_NANO,
                enabled = state.nanoAvailable == true,
                status = when (state.nanoAvailable) {
                    true -> "Available on this device"
                    false -> "AICore status: ${state.nanoStatus ?: "Unavailable"}"
                    null -> "Checking device availability…"
                },
                onSelect = { viewModel.selectBackend(AIBackend.GEMINI_NANO) },
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("Gemma 4 E2B IT (LiteRT-LM)", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Optional on-device model managed by this app. Download size is approximately 2.59 GB. The model is verified before it can be selected.",
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    when (state.download.status) {
                        ModelDownloadStatus.INSTALLED -> Text(
                            "Installed and verified",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                        )
                        ModelDownloadStatus.QUEUED -> Text("Download queued")
                        ModelDownloadStatus.DOWNLOADING -> Text("Downloading…")
                        ModelDownloadStatus.PAUSED -> Text("Waiting for an allowed network")
                        ModelDownloadStatus.VERIFYING -> Text("Verifying model…")
                        ModelDownloadStatus.FAILED -> Text(
                            state.download.message ?: "Download failed",
                            color = MaterialTheme.colorScheme.error,
                        )
                        ModelDownloadStatus.NOT_INSTALLED -> Text("Not installed")
                    }

                    if (state.download.status == ModelDownloadStatus.DOWNLOADING ||
                        state.download.status == ModelDownloadStatus.QUEUED ||
                        state.download.status == ModelDownloadStatus.PAUSED
                    ) {
                        LinearProgressIndicator(
                            progress = state.download.progressPercent / 100f,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            "${state.download.progressPercent}%  •  " +
                                "${formatGiB(state.download.downloadedBytes)} / ${formatGiB(state.download.totalBytes)}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Allow mobile data", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Off by default; downloads use Wi-Fi/unmetered networks.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = state.mobileDataAllowed,
                            onCheckedChange = viewModel::setMobileDataAllowed,
                            enabled = state.download.status != ModelDownloadStatus.DOWNLOADING &&
                                state.download.status != ModelDownloadStatus.QUEUED &&
                                state.download.status != ModelDownloadStatus.PAUSED,
                        )
                    }

                    if (state.download.status == ModelDownloadStatus.INSTALLED) {
                        BackendPreferenceCard(
                            title = "Use LiteRT-LM",
                            description = "Prefer this installed local model for AI Pickup.",
                            selected = state.preferredBackend == AIBackend.LITERT_LM,
                            enabled = true,
                            status = "Ready to select",
                            onSelect = { viewModel.selectBackend(AIBackend.LITERT_LM) },
                        )
                        OutlinedButton(
                            onClick = viewModel::removeModel,
                            enabled = !state.isBusy,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Remove downloaded model")
                        }
                    } else {
                        Button(
                            onClick = viewModel::downloadModel,
                            enabled = !state.isBusy &&
                                state.download.status != ModelDownloadStatus.DOWNLOADING &&
                                state.download.status != ModelDownloadStatus.QUEUED &&
                                state.download.status != ModelDownloadStatus.PAUSED,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(if (state.download.status == ModelDownloadStatus.FAILED) "Retry download" else "Download model")
                        }

                        if (state.download.status == ModelDownloadStatus.DOWNLOADING ||
                            state.download.status == ModelDownloadStatus.QUEUED ||
                            state.download.status == ModelDownloadStatus.PAUSED
                        ) {
                            OutlinedButton(
                                onClick = viewModel::cancelDownload,
                                enabled = !state.isBusy,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Cancel download")
                            }
                        }
                    }
                }
            }

            state.error?.let { message ->
                Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun BackendPreferenceCard(
    title: String,
    description: String,
    selected: Boolean,
    enabled: Boolean,
    status: String,
    onSelect: () -> Unit,
) {
    Card(
        onClick = onSelect,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(selected = selected, onClick = onSelect, enabled = enabled)
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodySmall)
                Text(
                    status,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (enabled) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun formatGiB(bytes: Long): String =
    String.format("%.2f GiB", bytes / (1024.0 * 1024.0 * 1024.0))
