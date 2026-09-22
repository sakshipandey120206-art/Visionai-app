"""
Data models and response schemas for the VisionAI Computer Vision Engine.
Provides strict type definitions for OpenCV, skimage, YOLO, EasyOCR, and TensorFlow outputs.
"""

from typing import List, Optional, Dict, Any
from pydantic import BaseModel, Field


class BoundingBox(BaseModel):
    """Normalized or pixel coordinates for detected entities [x1, y1, x2, y2]."""
    x1: float
    y1: float
    x2: float
    y2: float
    width: float
    height: float


class DetectedObject(BaseModel):
    """Structured entity returned by the YOLO / PyTorch object detector."""
    class_id: int
    class_name: str
    confidence: float
    bbox: BoundingBox


class DetectionResponse(BaseModel):
    """Response payload for the YOLO object detection pipeline."""
    success: bool
    count: int
    objects: List[DetectedObject] = Field(default_factory=list)
    message: str = ""
    processing_time_ms: float = 0.0


class OCRItem(BaseModel):
    """Recognized text span produced by EasyOCR."""
    text: str
    confidence: float
    bbox: Optional[List[List[int]]] = None


class OCRResponse(BaseModel):
    """Response payload for the EasyOCR recognition pipeline."""
    success: bool
    count: int
    items: List[OCRItem] = Field(default_factory=list)
    concatenated_text: str = ""
    message: str = ""
    processing_time_ms: float = 0.0


class OpenCVMetrics(BaseModel):
    """Low-level image characteristics computed via OpenCV (cv2)."""
    width: int
    height: int
    channels: int
    brightness_mean: float
    contrast_rms: float
    blur_laplacian_var: float
    is_blurry: bool
    color_mean_bgr: List[float]


class SkimageMetrics(BaseModel):
    """Scientific structural and exposure analysis computed via scikit-image."""
    is_low_contrast: bool
    sobel_edge_density: float
    mean_saturation: float
    shannon_entropy: float
    scientific_summary: str


class TensorFlowFeatures(BaseModel):
    """Semantic features and classification predictions via TensorFlow (tf.keras)."""
    model_name: str
    is_functional: bool
    top_predictions: List[Dict[str, Any]] = Field(default_factory=list)
    normalized_variance: float
    quality_assessment: str


class ImageAnalysisResponse(BaseModel):
    """Unified response containing the full multi-library CV processing pipeline results."""
    success: bool
    opencv: OpenCVMetrics
    scikit_image: SkimageMetrics
    tensorflow: TensorFlowFeatures
    yolo_detections: List[DetectedObject] = Field(default_factory=list)
    ocr_results: List[OCRItem] = Field(default_factory=list)
    vqa_context_prompt: str
    processing_time_ms: float = 0.0
    message: str = ""


class HealthResponse(BaseModel):
    """Health check response listing versions of the 6 integrated libraries."""
    status: str
    libraries: Dict[str, str]
    pytorch_cuda_available: bool
    device: str
