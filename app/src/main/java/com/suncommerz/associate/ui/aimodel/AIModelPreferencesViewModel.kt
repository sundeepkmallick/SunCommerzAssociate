package com.suncommerz.associate.ui.aimodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suncommerz.associate.BuildConfig
import com.suncommerz.associate.ai.AIBackend
import com.suncommerz.associate.ai.AIModelPreferencesStore
import com.suncommerz.associate.ai.GeminiNanoPickupAgent
import com.suncommerz.associate.ai.LocalModelCatalog
import com.suncommerz.associate.ai.LocalModelDefinition
import com.suncommerz.associate.ai.download.LiteRtLmModelDownloadManager
import com.suncommerz.associate.ai.download.ModelDownloadState
import com.suncommerz.associate.ai.download.ModelDownloadStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AIModelPreferencesUiState(
    val preferredBackend: AIBackend = AIBackend.FIREBASE,
    val liteRtLmRuntimeVersion: String = BuildConfig.LITERT_LM_VERSION,
    val nanoAvailable: Boolean? = null,
    val nanoStatus: String? = null,
    val mobileDataAllowed: Boolean = false,
    val localModels: List<LocalModelDefinition> = LocalModelCatalog.models,
    val selectedLocalModelId: String = LocalModelCatalog.DEFAULT_MODEL_ID,
    val installedLocalModelId: String? = null,
    val download: ModelDownloadState = ModelDownloadState(ModelDownloadStatus.NOT_INSTALLED),
    val isBusy: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class AIModelPreferencesViewModel @Inject constructor(
    private val preferences: AIModelPreferencesStore,
    private val nanoAgent: GeminiNanoPickupAgent,
    private val downloadManager: LiteRtLmModelDownloadManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        AIModelPreferencesUiState(
            preferredBackend = preferences.getPreferredBackend(),
            mobileDataAllowed = preferences.isMobileDataAllowed(),
            selectedLocalModelId = preferences.getSelectedLocalModelId(),
            liteRtLmRuntimeVersion = BuildConfig.LITERT_LM_VERSION,
        )
    )
    val uiState: StateFlow<AIModelPreferencesUiState> = _uiState.asStateFlow()

    init {
        refreshNanoAvailability()
        viewModelScope.launch {
            while (isActive) {
                refreshDownloadState()
                delay(1_500)
            }
        }
    }

    fun refreshNanoAvailability() {
        viewModelScope.launch {
            val status = runCatching { nanoAgent.availabilityStatus() }.getOrDefault("STATUS_CHECK_FAILED")
            _uiState.update { it.copy(nanoAvailable = status == "AVAILABLE", nanoStatus = status) }
        }
    }

    fun selectBackend(backend: AIBackend) {
        val state = _uiState.value
        val usable = when (backend) {
            AIBackend.FIREBASE -> true
            AIBackend.GEMINI_NANO -> state.nanoAvailable == true
            AIBackend.LITERT_LM -> state.installedLocalModelId?.let { id ->
                LocalModelCatalog.get(id).pickupAgentValidated
            } == true
        }
        if (!usable) return
        preferences.setPreferredBackend(backend)
        _uiState.update { it.copy(preferredBackend = backend, error = null) }
    }

    fun selectLocalModel(modelId: String) {
        if (LocalModelCatalog.models.none { it.id == modelId }) return
        preferences.setSelectedLocalModelId(modelId)
        _uiState.update { it.copy(selectedLocalModelId = modelId, error = null) }
        viewModelScope.launch { refreshDownloadState() }
    }

    fun setMobileDataAllowed(allowed: Boolean) {
        preferences.setMobileDataAllowed(allowed)
        _uiState.update { it.copy(mobileDataAllowed = allowed) }
    }

    fun downloadModel(modelId: String = _uiState.value.selectedLocalModelId) {
        viewModelScope.launch {
            val model = LocalModelCatalog.get(modelId)
            val runtimeVersion = BuildConfig.LITERT_LM_VERSION
            if (!LocalModelCatalog.canDownload(model, runtimeVersion)) {
                _uiState.update { it.copy(error = LocalModelCatalog.downloadBlockReason(model, runtimeVersion)) }
                return@launch
            }
            _uiState.update { it.copy(isBusy = true, error = null) }
            runCatching {
                // SDK agents are created and closed per turn, so no native engine remains
                // resident while a replacement model is downloaded.
                if (_uiState.value.installedLocalModelId != null &&
                    _uiState.value.installedLocalModelId != modelId
                ) {
                    preferences.setPreferredBackend(AIBackend.FIREBASE)
                    _uiState.update { it.copy(preferredBackend = AIBackend.FIREBASE) }
                }
                downloadManager.enqueue(modelId, _uiState.value.mobileDataAllowed)
            }.onSuccess { state ->
                _uiState.update { it.copy(download = state, isBusy = false) }
            }.onFailure { error ->
                _uiState.update { it.copy(isBusy = false, error = error.message ?: "Unable to start the model download") }
            }
        }
    }

    fun cancelDownload() {
        viewModelScope.launch {
            downloadManager.cancelDownload()
            refreshDownloadState()
        }
    }

    fun removeModel() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, error = null) }
            runCatching {
                if (_uiState.value.preferredBackend == AIBackend.LITERT_LM) {
                    preferences.setPreferredBackend(AIBackend.FIREBASE)
                    _uiState.update { it.copy(preferredBackend = AIBackend.FIREBASE) }
                }
                downloadManager.removeInstalledModel()
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        installedLocalModelId = null,
                        download = ModelDownloadState(ModelDownloadStatus.NOT_INSTALLED),
                        isBusy = false,
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isBusy = false, error = error.message ?: "Unable to remove the model") }
            }
        }
    }

    private suspend fun refreshDownloadState() {
        val modelId = _uiState.value.selectedLocalModelId
        runCatching { downloadManager.getState(modelId) }
            .onSuccess { download ->
                val installedId = downloadManager.installedModelDefinition()?.id
                _uiState.update {
                    it.copy(
                        download = download,
                        installedLocalModelId = installedId,
                    )
                }
            }
            .onFailure { error ->
                _uiState.update { it.copy(error = error.message ?: "Unable to read model download status") }
            }
    }
}
