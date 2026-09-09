package com.suncommerz.associate.ai.provider

import android.content.Context
import com.google.adk.kt.litertlm.LiteRtLmModel
import com.google.adk.kt.models.Model
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalModelProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val modelFile: File
        get() = File(
            context.filesDir,
            "models/local_model.litertlm"
        )

    /*fun get(): Model {
        check(isModelAvailable()) {
            "LiteRT-LM model not found: ${modelFile.absolutePath}"
        }

        return LiteRtLmModel.create(
            EngineConfig(
                modelPath = modelFile.absolutePath,
                backend = Backend.CPU()
            )
        )
    }*/

    fun resolveModelFile(): File {
        return modelFile
    }

    fun isModelAvailable(): Boolean {
        return modelFile.exists() && modelFile.length() > 0
    }
}