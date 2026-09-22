package com.example.network

import com.example.model.DetectionResponse
import com.example.model.HealthCheckResponse
import com.example.model.ImageAnalysisResponse
import com.example.model.OCRResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.util.concurrent.TimeUnit

interface VisionApi {
    @GET("health")
    suspend fun checkHealth(): HealthCheckResponse

    @Multipart
    @POST("detect")
    suspend fun detectObjects(
        @Part file: MultipartBody.Part,
        @Part("confidence") confidence: RequestBody
    ): DetectionResponse

    @Multipart
    @POST("ocr")
    suspend fun readText(
        @Part file: MultipartBody.Part
    ): OCRResponse

    @Multipart
    @POST("analyze")
    suspend fun analyzeImage(
        @Part file: MultipartBody.Part
    ): ImageAnalysisResponse
}

class VisionService(initialBaseUrl: String = "http://10.0.2.2:8000/") {

    private var currentBaseUrl: String = sanitizeUrl(initialBaseUrl)
    private var api: VisionApi = buildApi(currentBaseUrl)

    private fun sanitizeUrl(url: String): String {
        var clean = url.trim()
        if (!clean.startsWith("http://") && !clean.startsWith("https://")) {
            clean = "http://$clean"
        }
        if (!clean.endsWith("/")) {
            clean = "$clean/"
        }
        return clean
    }

    private fun buildApi(url: String): VisionApi {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(VisionApi::class.java)
    }

    fun updateBaseUrl(newUrl: String) {
        val sanitized = sanitizeUrl(newUrl)
        if (sanitized != currentBaseUrl) {
            currentBaseUrl = sanitized
            api = buildApi(sanitized)
        }
    }

    fun getBaseUrl(): String = currentBaseUrl

    suspend fun checkHealth(): HealthCheckResponse = api.checkHealth()

    suspend fun detectObjects(file: MultipartBody.Part, confidence: RequestBody): DetectionResponse =
        api.detectObjects(file, confidence)

    suspend fun readText(file: MultipartBody.Part): OCRResponse =
        api.readText(file)

    suspend fun analyzeImage(file: MultipartBody.Part): ImageAnalysisResponse =
        api.analyzeImage(file)
}
