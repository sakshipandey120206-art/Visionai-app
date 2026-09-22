package com.example.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenCVMetrics(
    @Json(name = "width") val width: Int = 0,
    @Json(name = "height") val height: Int = 0,
    @Json(name = "channels") val channels: Int = 3,
    @Json(name = "brightness_mean") val brightnessMean: Float = 0f,
    @Json(name = "contrast_rms") val contrastRms: Float = 0f,
    @Json(name = "blur_laplacian_var") val blurLaplacianVar: Float = 0f,
    @Json(name = "is_blurry") val isBlurry: Boolean = false,
    @Json(name = "color_mean_bgr") val colorMeanBgr: List<Float> = emptyList()
)

@JsonClass(generateAdapter = true)
data class SkimageMetrics(
    @Json(name = "is_low_contrast") val isLowContrast: Boolean = false,
    @Json(name = "sobel_edge_density") val sobelEdgeDensity: Float = 0f,
    @Json(name = "mean_saturation") val meanSaturation: Float = 0f,
    @Json(name = "shannon_entropy") val shannonEntropy: Float = 0f,
    @Json(name = "scientific_summary") val scientificSummary: String = ""
)

@JsonClass(generateAdapter = true)
data class TensorFlowPrediction(
    @Json(name = "label") val label: String = "",
    @Json(name = "probability") val probability: Float = 0f
)

@JsonClass(generateAdapter = true)
data class TensorFlowFeatures(
    @Json(name = "model_name") val modelName: String = "",
    @Json(name = "is_functional") val isFunctional: Boolean = false,
    @Json(name = "top_predictions") val topPredictions: List<TensorFlowPrediction> = emptyList(),
    @Json(name = "normalized_variance") val normalizedVariance: Float = 0f,
    @Json(name = "quality_assessment") val qualityAssessment: String = ""
)

@JsonClass(generateAdapter = true)
data class ImageAnalysisResponse(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "opencv") val opencv: OpenCVMetrics = OpenCVMetrics(),
    @Json(name = "scikit_image") val scikitImage: SkimageMetrics = SkimageMetrics(),
    @Json(name = "tensorflow") val tensorflow: TensorFlowFeatures = TensorFlowFeatures(),
    @Json(name = "yolo_detections") val yoloDetections: List<DetectedObject> = emptyList(),
    @Json(name = "ocr_results") val ocrResults: List<OCRItem> = emptyList(),
    @Json(name = "vqa_context_prompt") val vqaContextPrompt: String = "",
    @Json(name = "processing_time_ms") val processingTimeMs: Float = 0f,
    @Json(name = "message") val message: String = ""
)

@JsonClass(generateAdapter = true)
data class HealthCheckResponse(
    @Json(name = "status") val status: String = "",
    @Json(name = "libraries") val libraries: Map<String, String> = emptyMap(),
    @Json(name = "pytorch_cuda_available") val pytorchCudaAvailable: Boolean = false,
    @Json(name = "device") val device: String = ""
)
