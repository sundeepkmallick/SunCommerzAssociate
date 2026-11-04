package com.suncommerz.associate.ai.download

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

enum class ModelDownloadStatus {
    NOT_INSTALLED,
    QUEUED,
    DOWNLOADING,
    PAUSED,
    VERIFYING,
    INSTALLED,
    FAILED,
}

data class ModelDownloadState(
    val status: ModelDownloadStatus,
    val progressPercent: Int = 0,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = MODEL_SIZE_BYTES,
    val message: String? = null,
) {
    companion object {
        const val MODEL_SIZE_BYTES = 2_588_147_712L
    }
}

/**
 * Android DownloadManager owns the transfer, allowing it to continue while the
 * app is backgrounded and survive app process death/reboot. Network interruption
 * retries are delegated to the platform. The model is activated only after its
 * published byte size and SHA-256 have both been verified.
 */
@Singleton
class LiteRtLmModelDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val downloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    private val modelDirectory: File
        get() = context.getExternalFilesDir(null) ?: context.filesDir
    private val installedFile: File
        get() = File(modelDirectory, MODEL_FILE_NAME)
    private val temporaryFile: File
        get() = File(modelDirectory, TEMP_FILE_NAME)

    suspend fun enqueue(allowMobileData: Boolean): ModelDownloadState = withContext(Dispatchers.IO) {
        if (installedFile.isFile && installedFile.length() == ModelDownloadState.MODEL_SIZE_BYTES) {
            val installed = getState()
            if (installed.status == ModelDownloadStatus.INSTALLED) return@withContext installed
            installedFile.delete()
        }

        val requiredBytes = ModelDownloadState.MODEL_SIZE_BYTES + MIN_FREE_SPACE_AFTER_DOWNLOAD
        check(modelDirectory.usableSpace >= requiredBytes) {
            "At least 3.1 GB of free storage is required to download and install this model."
        }

        val existingId = preferences.getLong(KEY_DOWNLOAD_ID, INVALID_DOWNLOAD_ID)
        if (existingId != INVALID_DOWNLOAD_ID) {
            val existing = query(existingId)
            if (existing != null && existing.status in ACTIVE_DOWNLOAD_STATUSES) {
                return@withContext existing.toState()
            }
            downloadManager.remove(existingId)
        }

        temporaryFile.delete()
        val request = DownloadManager.Request(Uri.parse(MODEL_DOWNLOAD_URL))
            .setTitle("Gemma 4 E2B IT")
            .setDescription("Downloading the optional on-device AI model")
            .setMimeType("application/octet-stream")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(allowMobileData)
            .setAllowedOverRoaming(false)
            .setDestinationInExternalFilesDir(context, null, TEMP_FILE_NAME)

        val id = downloadManager.enqueue(request)
        preferences.edit()
            .putLong(KEY_DOWNLOAD_ID, id)
            .remove(KEY_VERIFIED)
            .remove(KEY_FAILURE)
            .apply()
        ModelDownloadState(ModelDownloadStatus.QUEUED, message = "Download queued")
    }

    suspend fun getState(): ModelDownloadState = withContext(Dispatchers.IO) {
        if (installedFile.isFile) {
            if (installedFile.length() != ModelDownloadState.MODEL_SIZE_BYTES) {
                installedFile.delete()
                preferences.edit().remove(KEY_VERIFIED).apply()
            } else if (preferences.getBoolean(KEY_VERIFIED, false)) {
                return@withContext ModelDownloadState(ModelDownloadStatus.INSTALLED)
            } else {
                val valid = sha256(installedFile).equals(EXPECTED_SHA256, ignoreCase = true)
                if (valid) {
                    preferences.edit().putBoolean(KEY_VERIFIED, true).apply()
                    return@withContext ModelDownloadState(ModelDownloadStatus.INSTALLED)
                }
                installedFile.delete()
                preferences.edit().remove(KEY_VERIFIED).apply()
                return@withContext failed("The existing model failed SHA-256 verification and was removed.")
            }
        }

        val id = preferences.getLong(KEY_DOWNLOAD_ID, INVALID_DOWNLOAD_ID)
        if (id == INVALID_DOWNLOAD_ID) {
            val previousFailure = preferences.getString(KEY_FAILURE, null)
            return@withContext if (previousFailure == null) {
                ModelDownloadState(ModelDownloadStatus.NOT_INSTALLED)
            } else {
                ModelDownloadState(ModelDownloadStatus.FAILED, message = previousFailure)
            }
        }

        val record = query(id)
            ?: return@withContext failed("The download record is no longer available. You can start it again.")

        when (record.status) {
            DownloadManager.STATUS_SUCCESSFUL -> verifyAndInstall(id)
            DownloadManager.STATUS_FAILED -> ModelDownloadState(
                ModelDownloadStatus.FAILED,
                message = record.reasonDescription(),
            )
            else -> record.toState()
        }
    }

    suspend fun cancelDownload() = withContext(Dispatchers.IO) {
        val id = preferences.getLong(KEY_DOWNLOAD_ID, INVALID_DOWNLOAD_ID)
        if (id != INVALID_DOWNLOAD_ID) downloadManager.remove(id)
        preferences.edit().remove(KEY_DOWNLOAD_ID).remove(KEY_FAILURE).apply()
        temporaryFile.delete()
    }

    suspend fun removeInstalledModel() = withContext(Dispatchers.IO) {
        cancelDownload()
        installedFile.delete()
        preferences.edit().remove(KEY_VERIFIED).apply()
    }

    fun isInstalled(): Boolean =
        installedFile.isFile &&
            installedFile.length() == ModelDownloadState.MODEL_SIZE_BYTES &&
            preferences.getBoolean(KEY_VERIFIED, false)

    fun installedModelFile(): File? = installedFile.takeIf { isInstalled() }

    private suspend fun verifyAndInstall(id: Long): ModelDownloadState {
        val source = temporaryFile
        if (!source.isFile || source.length() != ModelDownloadState.MODEL_SIZE_BYTES) {
            downloadManager.remove(id)
            preferences.edit().remove(KEY_DOWNLOAD_ID).apply()
            return failed("Downloaded file size did not match the published model artifact.")
        }

        val actualHash = sha256(source)
        if (!actualHash.equals(EXPECTED_SHA256, ignoreCase = true)) {
            source.delete()
            downloadManager.remove(id)
            preferences.edit().remove(KEY_DOWNLOAD_ID).apply()
            return failed("Model integrity verification failed. The incomplete or modified file was discarded.")
        }

        val destination = installedFile
        if (destination.exists()) destination.delete()
        check(source.renameTo(destination)) {
            "The verified model could not be moved into its installation location."
        }
        preferences.edit()
            .remove(KEY_DOWNLOAD_ID)
            .remove(KEY_FAILURE)
            .putBoolean(KEY_VERIFIED, true)
            .apply()
        downloadManager.remove(id)
        Log.i(TAG, "Gemma 4 E2B IT installed and SHA-256 verified")
        return ModelDownloadState(ModelDownloadStatus.INSTALLED)
    }

    private fun failed(message: String): ModelDownloadState {
        preferences.edit().putString(KEY_FAILURE, message).apply()
        return ModelDownloadState(ModelDownloadStatus.FAILED, message = message)
    }

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

    private fun DownloadRecord.toState(): ModelDownloadState {
        val percent = if (total > 0L) ((downloaded * 100L) / total).toInt().coerceIn(0, 100) else 0
        val status = when (this.status) {
            DownloadManager.STATUS_PENDING -> ModelDownloadStatus.QUEUED
            DownloadManager.STATUS_RUNNING -> ModelDownloadStatus.DOWNLOADING
            DownloadManager.STATUS_PAUSED -> ModelDownloadStatus.PAUSED
            else -> ModelDownloadStatus.NOT_INSTALLED
        }
        return ModelDownloadState(
            status,
            percent,
            downloaded,
            total.takeIf { it > 0 } ?: ModelDownloadState.MODEL_SIZE_BYTES,
        )
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
        digest.digest().joinToString("") {
            "%02x".format(Locale.US, it.toInt() and 0xff)
        }
    }

    private data class DownloadRecord(
        val status: Int,
        val reason: Int,
        val downloaded: Long,
        val total: Long,
    )

    private companion object {
        const val TAG = "LiteRtLmDownload"
        const val PREFERENCES_NAME = "litert_lm_download"
        const val KEY_DOWNLOAD_ID = "download_id"
        const val KEY_VERIFIED = "verified"
        const val KEY_FAILURE = "failure"
        const val INVALID_DOWNLOAD_ID = -1L
        const val MODEL_FILE_NAME = "gemma-4-E2B-it.litertlm"
        const val TEMP_FILE_NAME = "gemma-4-E2B-it.litertlm.download"
        const val MIN_FREE_SPACE_AFTER_DOWNLOAD = 512L * 1024L * 1024L
        const val EXPECTED_SHA256 = "181938105e0eefd105961417e8da75903eacda102c4fce9ce90f50b97139a63c"
        const val MODEL_DOWNLOAD_URL =
            "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/6b78abd019e61a1ca4cbe3b212d2c9ce8ff38a94/gemma-4-E2B-it.litertlm"

        val ACTIVE_DOWNLOAD_STATUSES = setOf(
            DownloadManager.STATUS_PENDING,
            DownloadManager.STATUS_RUNNING,
            DownloadManager.STATUS_PAUSED,
        )
    }
}
