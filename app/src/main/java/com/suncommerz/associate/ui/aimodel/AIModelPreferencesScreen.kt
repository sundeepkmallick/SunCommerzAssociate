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
import com.suncommerz.associate.ai.LocalModelCatalog
import com.suncommerz.associate.ai.LocalModelDefinition
import com.suncommerz.associate.ai.download.ModelDownloadStatus

@Composable
fun AIModelPreferencesScreen(
    viewModel: AIModelPreferencesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Choose which AI backend AI Pickup should prefer. If an on-device backend is unavailable or fails, the app falls back to Firebase ADK.",
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

            Text("LiteRT-LM local models", style = MaterialTheme.typography.titleLarge)
            Text("App runtime: LiteRT-LM ${state.liteRtLmRuntimeVersion} (from the packaged Gradle dependency)", style = MaterialTheme.typography.bodySmall)
            Text(
                "Choose any catalogue entry to review its specifications. Only models with a pinned, integrity-verified artifact and completed app validation can currently be downloaded. One local model is installed at a time; a verified replacement retires the previous model only after successful installation.",
                style = MaterialTheme.typography.bodySmall,
            )

            state.localModels.forEach { model ->
                LocalModelCard(
                    model = model,
                    selected = state.selectedLocalModelId == model.id,
                    installed = state.installedLocalModelId == model.id,
                    download = if (state.selectedLocalModelId == model.id) state.download.status else null,
                    onSelect = { viewModel.selectLocalModel(model.id) },
                    runtimeVersion = state.liteRtLmRuntimeVersion,
                )
            }

            val selectedModel = state.localModels.firstOrNull { it.id == state.selectedLocalModelId }
            if (selectedModel != null && LocalModelCatalog.canDownload(selectedModel, state.liteRtLmRuntimeVersion)) {
                if (state.download.status == ModelDownloadStatus.INSTALLED &&
                    state.installedLocalModelId == selectedModel.id
                ) {
                    if (selectedModel.pickupAgentValidated) {
                        BackendPreferenceCard(
                            title = "Use ${selectedModel.displayName}",
                            description = "Prefer this installed local model for AI Pickup.",
                            selected = state.preferredBackend == AIBackend.LITERT_LM,
                            enabled = true,
                            status = "Installed, integrity-verified, and BOPIS-validated",
                            onSelect = { viewModel.selectBackend(AIBackend.LITERT_LM) },
                        )
                    } else {
                        Text(
                            "Downloaded, but not yet validated for AI Pickup tool calling. Checksum verification status is shown in the model entry; this model cannot be selected as the active backend yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    OutlinedButton(
                        onClick = viewModel::removeModel,
                        enabled = !state.isBusy,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Remove downloaded model") }
                } else {
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
                        OutlinedButton(
                            onClick = viewModel::cancelDownload,
                            enabled = !state.isBusy,
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Cancel download") }
                    } else {
                        Button(
                            onClick = { viewModel.downloadModel(selectedModel.id) },
                            enabled = !state.isBusy,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                if (state.installedLocalModelId != null) "Download and replace current model"
                                else if (state.download.status == ModelDownloadStatus.FAILED) "Retry download"
                                else "Download selected model"
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Allow mobile data", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Off by default; model downloads use Wi-Fi/unmetered networks unless enabled.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = state.mobileDataAllowed,
                    onCheckedChange = viewModel::setMobileDataAllowed,
                    enabled = !state.isBusy &&
                        state.download.status != ModelDownloadStatus.DOWNLOADING &&
                        state.download.status != ModelDownloadStatus.QUEUED &&
                        state.download.status != ModelDownloadStatus.PAUSED,
                )
            }

            state.download.message?.takeIf { state.download.status == ModelDownloadStatus.FAILED }?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun LocalModelCard(
    model: LocalModelDefinition,
    selected: Boolean,
    installed: Boolean,
    download: ModelDownloadStatus?,
    runtimeVersion: String,
    onSelect: () -> Unit,
) {
    Card(
        onClick = onSelect,
        enabled = true,
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
            RadioButton(selected = selected, onClick = onSelect, enabled = true)
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(model.displayName, style = MaterialTheme.typography.titleMedium)
                Text(model.description, style = MaterialTheme.typography.bodySmall)
                Text("Approx. ${model.approximateSizeMb} MB", style = MaterialTheme.typography.labelMedium)
                Text("LiteRT-LM: ${model.minimumLiteRtLmVersion ?: "Exact minimum version not verified"}" +
                    (model.maximumTestedLiteRtLmVersion?.let { " (tested through $it)" } ?: ""), style = MaterialTheme.typography.bodySmall)
                Text(
                    if (installed && model.sha256 != null) "Installed · SHA-256 verified"
                    else if (installed) "Installed · checksum not available"
                    else if (!LocalModelCatalog.canDownload(model, runtimeVersion)) "Listed · no downloadable artifact"
                    else when (download) {
                        ModelDownloadStatus.QUEUED -> "Download queued"
                        ModelDownloadStatus.DOWNLOADING -> "Downloading"
                        ModelDownloadStatus.PAUSED -> "Waiting for an allowed network"
                        ModelDownloadStatus.FAILED -> "Download failed"
                        else -> "Download available"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (installed) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(model.availabilityNote, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    LocalModelCatalog.downloadBlockReason(model, runtimeVersion),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
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
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
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
