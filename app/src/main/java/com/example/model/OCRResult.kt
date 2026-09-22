package com.example.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OCRItem(
    @Json(name = "text") val text: String = "",
    @Json(name = "confidence") val confidence: Float = 0f,
    @Json(name = "bbox") val bbox: List<List<Int>>? = null
)

@JsonClass(generateAdapter = true)
data class OCRResponse(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "count") val count: Int = 0,
    @Json(name = "items") val items: List<OCRItem> = emptyList(),
    @Json(name = "concatenated_text") val concatenatedText: String = "",
    @Json(name = "message") val message: String = "",
    @Json(name = "processing_time_ms") val processingTimeMs: Float = 0f
)
