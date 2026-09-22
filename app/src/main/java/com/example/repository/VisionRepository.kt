package com.example.repository

import android.graphics.Bitmap
import com.example.model.DetectionResponse
import com.example.model.HealthCheckResponse
import com.example.model.ImageAnalysisResponse
import com.example.model.OCRResponse
import com.example.network.GeminiService
import com.example.network.VisionService
import com.example.utils.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class VisionRepository(
    private val visionService: VisionService = VisionService(),
    private val geminiService: GeminiService = GeminiService()
) {

    fun updateBackendUrl(newUrl: String) {
        visionService.updateBaseUrl(newUrl)
    }

    fun getBackendUrl(): String = visionService.getBaseUrl()

    suspend fun checkBackendHealth(): Result<HealthCheckResponse> = withContext(Dispatchers.IO) {
        try {
            val response = visionService.checkHealth()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun detectObjects(bitmap: Bitmap, confidenceThreshold: Float = 0.30f): Result<DetectionResponse> =
        withContext(Dispatchers.IO) {
            try {
                val part = ImageUtils.createMultipartFromBitmap(bitmap)
                val confBody = confidenceThreshold.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val response = visionService.detectObjects(part, confBody)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun readText(bitmap: Bitmap): Result<OCRResponse> = withContext(Dispatchers.IO) {
        try {
            val part = ImageUtils.createMultipartFromBitmap(bitmap)
            val response = visionService.readText(part)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun analyzeImage(bitmap: Bitmap): Result<ImageAnalysisResponse> = withContext(Dispatchers.IO) {
        try {
            val part = ImageUtils.createMultipartFromBitmap(bitmap)
            val response = visionService.analyzeImage(part)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun askGeminiVQA(
        bitmap: Bitmap,
        question: String,
        cvTelemetryContext: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        geminiService.askVisualQuestion(bitmap, question, cvTelemetryContext)
    }

    suspend fun describeImage(
        bitmap: Bitmap,
        cvTelemetryContext: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        geminiService.describeImage(bitmap, cvTelemetryContext)
    }
}
