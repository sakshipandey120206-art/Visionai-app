package com.example.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BoundingBox(
    @Json(name = "x1") val x1: Float = 0f,
    @Json(name = "y1") val y1: Float = 0f,
    @Json(name = "x2") val x2: Float = 0f,
    @Json(name = "y2") val y2: Float = 0f,
    @Json(name = "width") val width: Float = 0f,
    @Json(name = "height") val height: Float = 0f
)

@JsonClass(generateAdapter = true)
data class DetectedObject(
    @Json(name = "class_id") val classId: Int = 0,
    @Json(name = "class_name") val className: String = "",
    @Json(name = "confidence") val confidence: Float = 0f,
    @Json(name = "bbox") val bbox: BoundingBox = BoundingBox()
)

@JsonClass(generateAdapter = true)
data class DetectionResponse(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "count") val count: Int = 0,
    @Json(name = "objects") val objects: List<DetectedObject> = emptyList(),
    @Json(name = "message") val message: String = "",
    @Json(name = "processing_time_ms") val processingTimeMs: Float = 0f
)
