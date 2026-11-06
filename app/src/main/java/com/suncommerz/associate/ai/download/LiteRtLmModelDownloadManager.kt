package com.suncommerz.associate.ai.download

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.util.Log
import com.suncommerz.associate.BuildConfig
import com.suncommerz.associate.ai.LocalModelCatalog
import com.suncommerz.associate.ai.LocalModelDefinition
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

enum class ModelDownloadStatus { NOT_INSTALLED, QUEUED, DOWNLOADING, PAUSED, VERIFYING, INSTALLED, FAILED }

data class ModelDownloadState(
    val status: ModelDownloadStatus,
    val progressPercent: Int = 0,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val message: String? = null,
)

@Singleton
class LiteRtLmModelDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val downloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val modelDirectory: File
        get() = context.getExternalFilesDir(null) ?: context.filesDir

    suspend fun enqueue(allowMobileData: Boolean): ModelDownloadState =
        enqueue(LocalModelCatalog.DEFAULT_MODEL_ID, allowMobileData)

    suspend fun enqueue(modelId: String, allowMobileData: Boolean): ModelDownloadState =
        withContext(Dispatchers.IO) {
            val definition = requireDownloadableModel(modelId)
            val installed = getState(modelId)
            if (installed.status == ModelDownloadStatus.INSTALLED) return@withContext installed

            val requiredBytes = definition.sizeBytes + MIN_FREE_SPACE_AFTER_DOWNLOAD
            check(modelDirectory.usableSpace >= requiredBytes) {
                "At least ${formatGiB(requiredBytes)} of free storage is required to download this model."
            }

            val existingId = preferences.getLong(KEY_DOWNLOAD_ID, INVALID_DOWNLOAD_ID)
            if (existingId != INVALID_DOWNLOAD_ID) {
                val existing = query(existingId)
                if (existing != null && existing.status in ACTIVE_DOWNLOAD_STATUSES) {
                    val activeModelId = preferences.getString(KEY_DOWNLOAD_MODEL_ID, null)
                    if (activeModelId == modelId) return@withContext existing.toState(definition.sizeBytes)
                    downloadManager.remove(existingId)
                } else {
                    downloadManager.remove(existingId)
                }
            }

            temporaryFile(modelId).delete()
            val request = DownloadManager.Request(Uri.parse(checkNotNull(definition.downloadUrl)))
                .setTitle(definition.displayName)
                .setDescription("Downloading the optional on-device AI model")
                .setMimeType("application/octet-stream")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(allowMobileData)
                .setAllowedOverRoaming(false)
                .setDestinationInExternalFilesDir(context, null, temporaryFile(modelId).name)

            val id = downloadManager.enqueue(request)
            preferences.edit()
                .putLong(KEY_DOWNLOAD_ID, id)
                .putString(KEY_DOWNLOAD_MODEL_ID, modelId)
                .remove(KEY_FAILURE)
                .apply()
            ModelDownloadState(ModelDownloadStatus.QUEUED, totalBytes = definition.sizeBytes, message = "Download queued")
        }

    suspend fun getState(): ModelDownloadState =
        getState(preferences.getString(KEY_ACTIVE_MODEL_ID, LocalModelCatalog.DEFAULT_MODEL_ID)
            ?: LocalModelCatalog.DEFAULT_MODEL_ID)

    suspend fun getState(modelId: String): ModelDownloadState = withContext(Dispatchers.IO) {
        val definition = LocalModelCatalog.get(modelId)
        if (isInstalled(modelId)) return@withContext ModelDownloadState(
            ModelDownloadStatus.INSTALLED, totalBytes = definition.sizeBytes,
            message = if (definition.sha256 != null) "SHA-256 verified" else "Installed without checksum verification"
        )

        val id = preferences.getLong(KEY_DOWNLOAD_ID, INVALID_DOWNLOAD_ID)
        val downloadingModelId = preferences.getString(KEY_DOWNLOAD_MODEL_ID, null)
        if (id == INVALID_DOWNLOAD_ID || downloadingModelId != modelId) {
            val failure = preferences.getString(KEY_FAILURE, null)
            return@withContext if (failure == null) {
                ModelDownloadState(ModelDownloadStatus.NOT_INSTALLED, totalBytes = definition.sizeBytes)
            } else ModelDownloadState(ModelDownloadStatus.FAILED, totalBytes = definition.sizeBytes, message = failure)
        }

        val record = query(id)
            ?: return@withContext failed("The download record is no longer available. You can start it again.", definition.sizeBytes)
        when (record.status) {
            DownloadManager.STATUS_SUCCESSFUL -> verifyAndInstall(id, definition)
            DownloadManager.STATUS_FAILED -> ModelDownloadState(
                ModelDownloadStatus.FAILED, totalBytes = definition.sizeBytes,
                message = record.reasonDescription(),
            )
            else -> record.toState(definition.sizeBytes)
        }
    }

    suspend fun cancelDownload() = withContext(Dispatchers.IO) {
        val id = preferences.getLong(KEY_DOWNLOAD_ID, INVALID_DOWNLOAD_ID)
        if (id != INVALID_DOWNLOAD_ID) downloadManager.remove(id)
        val modelId = preferences.getString(KEY_DOWNLOAD_MODEL_ID, LocalModelCatalog.DEFAULT_MODEL_ID)
            ?: LocalModelCatalog.DEFAULT_MODEL_ID
        preferences.edit().remove(KEY_DOWNLOAD_ID).remove(KEY_DOWNLOAD_MODEL_ID).remove(KEY_FAILURE).apply()
        temporaryFile(modelId).delete()
    }

    suspend fun removeInstalledModel() = withContext(Dispatchers.IO) {
        cancelDownload()
        val activeId = preferences.getString(KEY_ACTIVE_MODEL_ID, LocalModelCatalog.DEFAULT_MODEL_ID)
            ?: LocalModelCatalog.DEFAULT_MODEL_ID
        LocalModelCatalog.models.forEach { model -> installedFile(model).delete() }
        preferences.edit().remove(KEY_INSTALL_ACCEPTED).remove(KEY_ACTIVE_MODEL_ID).apply()
        Log.i(TAG, "Removed installed LiteRT-LM model $activeId")
    }

    fun isInstalled(): Boolean = installedModelDefinition() != null

    fun isInstalled(modelId: String): Boolean {
        val definition = LocalModelCatalog.get(modelId)
        val file = installedFile(definition)
        return file.isFile && (definition.expectedSizeBytes == null || file.length() == definition.expectedSizeBytes) &&
            preferences.getBoolean(KEY_INSTALL_ACCEPTED, false) &&
            preferences.getString(KEY_ACTIVE_MODEL_ID, LocalModelCatalog.DEFAULT_MODEL_ID) == modelId
    }

    fun installedModelDefinition(): LocalModelDefinition? {
        val modelId = preferences.getString(KEY_ACTIVE_MODEL_ID, LocalModelCatalog.DEFAULT_MODEL_ID)
            ?: LocalModelCatalog.DEFAULT_MODEL_ID
        return LocalModelCatalog.get(modelId).takeIf { isInstalled(it.id) }
    }

    fun installedModelFile(): File? =
        installedModelDefinition()?.let(::installedFile)

    fun installedModelFile(modelId: String): File? =
        LocalModelCatalog.get(modelId).takeIf { isInstalled(modelId) }?.let(::installedFile)

    private suspend fun verifyAndInstall(id: Long, definition: LocalModelDefinition): ModelDownloadState {
        val source = temporaryFile(definition.id)
        if (!source.isFile) {
            downloadManager.remove(id)
            clearDownload()
            return failed("The downloaded model file is missing.", definition.sizeBytes)
        }
        definition.expectedSizeBytes?.let { expected ->
            if (source.length() != expected) {
                source.delete()
                downloadManager.remove(id)
                clearDownload()
                return failed("Downloaded file size did not match the exact published artifact size.", definition.sizeBytes)
            }
        }

        val expectedHash = definition.sha256
        if (expectedHash != null) {
            if (!expectedHash.matches(Regex("(?i)^[a-f0-9]{64}$"))) {
                source.delete()
                downloadManager.remove(id)
                clearDownload()
                return failed("The catalogue checksum is malformed. The downloaded file was discarded.", definition.sizeBytes)
            }
            val actualHash = sha256(source)
            if (!actualHash.equals(expectedHash, ignoreCase = true)) {
                source.delete()
                downloadManager.remove(id)
                clearDownload()
                return failed("Model integrity verification failed. The downloaded file was discarded.", definition.sizeBytes)
            }
        } else {
            Log.w(TAG, "Installing experimental model ${definition.id} without checksum verification")
        }

        val destination = installedFile(definition)
        val backup = File(modelDirectory, "${definition.artifactFileName}.previous")
        backup.delete()
        if (destination.exists()) check(destination.renameTo(backup)) {
            "Could not safely stage the previous model before replacement."
        }
        if (!source.renameTo(destination)) {
            if (backup.exists()) backup.renameTo(destination)
            throw IllegalStateException("The verified model could not be moved into its installation location.")
        }

        val previousId = preferences.getString(KEY_ACTIVE_MODEL_ID, null)
        preferences.edit()
            .remove(KEY_DOWNLOAD_ID)
            .remove(KEY_DOWNLOAD_MODEL_ID)
            .remove(KEY_FAILURE)
            .putString(KEY_ACTIVE_MODEL_ID, definition.id)
            .putBoolean(KEY_INSTALL_ACCEPTED, true)
            .apply()
        backup.delete()
        previousId?.takeIf { it != definition.id }?.let { oldId ->
            LocalModelCatalog.models.firstOrNull { it.id == oldId }?.let(::installedFile)?.delete()
        }
        downloadManager.remove(id)
        Log.i(TAG, "Installed and SHA-256 verified ${definition.displayName}")
        return ModelDownloadState(
            ModelDownloadStatus.INSTALLED,
            totalBytes = definition.sizeBytes,
            message = if (expectedHash != null) "SHA-256 verified" else "Installed without checksum verification",
        )
    }

    private fun requireDownloadableModel(modelId: String): LocalModelDefinition {
        val definition = LocalModelCatalog.get(modelId)
        check(LocalModelCatalog.canDownload(definition, BuildConfig.LITERT_LM_VERSION)) {
            LocalModelCatalog.downloadBlockReason(definition, BuildConfig.LITERT_LM_VERSION)
        }
        return definition
    }

    private fun installedFile(definition: LocalModelDefinition) =
        File(modelDirectory, definition.artifactFileName)

    private fun temporaryFile(modelId: String): File {
        val definition = LocalModelCatalog.get(modelId)
        return File(modelDirectory, "${definition.artifactFileName}.download")
    }

    private fun clearDownload() {
        preferences.edit().remove(KEY_DOWNLOAD_ID).remove(KEY_DOWNLOAD_MODEL_ID).apply()
    }

    private fun failed(message: String, totalBytes: Long) =
        ModelDownloadState(ModelDownloadStatus.FAILED, totalBytes = totalBytes, message = message)

    private fun query(id: Long): DownloadRecord? {
        val cursor = downloadManager.query(DownloadManager.Query().setFilterById(id)) ?: return null
        cursor.use {
            if (!it.moveToFirst()) return null
            return DownloadRecord(
                status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)),
                reason = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON)),
                downloaded = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)),
                total = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)),
            )
        }
    }

    private fun DownloadRecord.toState(expectedBytes: Long): ModelDownloadState {
        val percent = if (total > 0L) ((downloaded * 100L) / total).toInt().coerceIn(0, 100) else 0
        val status = when (this.status) {
            DownloadManager.STATUS_PENDING -> ModelDownloadStatus.QUEUED
            DownloadManager.STATUS_RUNNING -> ModelDownloadStatus.DOWNLOADING
            DownloadManager.STATUS_PAUSED -> ModelDownloadStatus.PAUSED
            else -> ModelDownloadStatus.NOT_INSTALLED
        }
        return ModelDownloadState(status, percent, downloaded, total.takeIf { it > 0 } ?: expectedBytes)
    }

    private fun DownloadRecord.reasonDescription(): String = when (reason) {
        DownloadManager.ERROR_CANNOT_RESUME -> "The server could not resume the download. Remove the partial download and try again."
        DownloadManager.ERROR_DEVICE_NOT_FOUND -> "Download storage is unavailable."
        DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "A model file already exists. Remove it before downloading again."
        DownloadManager.ERROR_FILE_ERROR -> "The device could not write the model file."
        DownloadManager.ERROR_HTTP_DATA_ERROR -> "A network error interrupted the model download."
        DownloadManager.ERROR_INSUFFICIENT_SPACE -> "There is not enough free storage to finish the download."
        DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "The model host redirected too many times."
        DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "The model host rejected the download request."
        else -> "Download failed (code $reason). You can retry from this screen."
    }

    private suspend fun sha256(file: File): String = withContext(Dispatchers.IO) {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        digest.digest().joinToString("") { "%02x".format(Locale.US, it.toInt() and 0xff) }
    }

    private fun formatGiB(bytes: Long) = String.format(Locale.US, "%.2f GiB", bytes / (1024.0 * 1024.0 * 1024.0))

    private data class DownloadRecord(
        val status: Int, val reason: Int, val downloaded: Long, val total: Long,
    )

    private companion object {
        const val TAG = "LiteRtLmDownload"
        const val PREFERENCES_NAME = "litert_lm_download"
        const val KEY_DOWNLOAD_ID = "download_id"
        const val KEY_DOWNLOAD_MODEL_ID = "download_model_id"
        const val KEY_INSTALL_ACCEPTED = "verified"
        const val KEY_FAILURE = "failure"
        const val KEY_ACTIVE_MODEL_ID = "active_model_id"
        const val INVALID_DOWNLOAD_ID = -1L
        const val MIN_FREE_SPACE_AFTER_DOWNLOAD = 512L * 1024L * 1024L
        val ACTIVE_DOWNLOAD_STATUSES = setOf(
            DownloadManager.STATUS_PENDING, DownloadManager.STATUS_RUNNING, DownloadManager.STATUS_PAUSED,
        )
    }
}
