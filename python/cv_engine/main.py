"""
VisionAI Computer Vision Engine - FastAPI Server
------------------------------------------------
Provides high-performance REST endpoints for the VisionAI Android app,
orchestrating OpenCV, scikit-image, YOLO (PyTorch), EasyOCR, and TensorFlow.

Running the server:
    cd python/cv_engine
    pip install -r requirements.txt
    python main.py
"""

import time
import os
import io
from typing import List
import uvicorn
from fastapi import FastAPI, UploadFile, File, Form, HTTPException
from fastapi.middleware.cors import CORSMiddleware

# Computer Vision modules
from image_processor import OpenCVProcessor
from image_analyzer import SkimageAnalyzer
from object_detector import YOLOObjectDetector
from ocr_engine import EasyOCREngine
from tensorflow_module import TensorFlowModule

# Schemas
from schemas import (
    DetectionResponse,
    OCRResponse,
    ImageAnalysisResponse,
    HealthResponse,
    OpenCVMetrics,
    SkimageMetrics,
    TensorFlowFeatures,
    DetectedObject,
    OCRItem
)

# Check versions for Health Check
import cv2
import skimage
import torch
import tensorflow as tf
import ultralytics
import easyocr

app = FastAPI(
    title="VisionAI Computer Vision Engine",
    description="Multimodal CV Engine powered by OpenCV, scikit-image, YOLO, EasyOCR, PyTorch, and TensorFlow",
    version="1.0.0"
)

# Enable CORS for local network and emulator access
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Initialize engines at startup
print("[VisionAI] Initializing Computer Vision pipeline engines...")
opencv_proc = OpenCVProcessor()
skimage_analyzer = SkimageAnalyzer()
yolo_detector = YOLOObjectDetector()
ocr_engine = EasyOCREngine()
tf_module = TensorFlowModule()
print("[VisionAI] All 6 CV/AI modules loaded successfully.")


@app.get("/health", response_model=HealthResponse)
def health_check():
    """Returns the operational status and library versions of all 6 CV components."""
    device_str = "cuda" if torch.cuda.is_available() else "cpu"
    return HealthResponse(
        status="online",
        libraries={
            "opencv": cv2.__version__,
            "scikit_image": skimage.__version__,
            "torch": torch.__version__,
            "ultralytics_yolo": ultralytics.__version__,
            "easyocr": easyocr.__version__,
            "tensorflow": tf.__version__
        },
        pytorch_cuda_available=torch.cuda.is_available(),
        device=device_str
    )


@app.post("/detect", response_model=DetectionResponse)
async def detect_objects(file: UploadFile = File(...), confidence: float = Form(0.30)):
    """
    Executes YOLO / PyTorch object detection on the uploaded image.
    """
    start_time = time.time()
    try:
        contents = await file.read()
        img_bgr = opencv_proc.decode_image(contents)
        img_rgb = opencv_proc.bgr_to_rgb(img_bgr)

        # Run YOLO with PyTorch inference
        objects = yolo_detector.detect(img_rgb, confidence_threshold=confidence)
        duration_ms = (time.time() - start_time) * 1000.0

        msg = f"Detected {len(objects)} object(s)." if objects else "No objects were confidently detected."
        return DetectionResponse(
            success=True,
            count=len(objects),
            objects=objects,
            message=msg,
            processing_time_ms=round(duration_ms, 2)
        )
    except Exception as e:
        duration_ms = (time.time() - start_time) * 1000.0
        return DetectionResponse(
            success=False,
            count=0,
            objects=[],
            message=f"Detection error: {str(e)}",
            processing_time_ms=round(duration_ms, 2)
        )


@app.post("/ocr", response_model=OCRResponse)
async def read_text(file: UploadFile = File(...)):
    """
    Executes EasyOCR text recognition on the uploaded image.
    """
    start_time = time.time()
    try:
        contents = await file.read()
        img_bgr = opencv_proc.decode_image(contents)
        img_rgb = opencv_proc.bgr_to_rgb(img_bgr)

        items = ocr_engine.read_text(img_rgb)
        duration_ms = (time.time() - start_time) * 1000.0

        concatenated = " ".join([it.text for it in items])
        msg = f"Detected {len(items)} text snippet(s)." if items else "No readable text was detected."

        return OCRResponse(
            success=True,
            count=len(items),
            items=items,
            concatenated_text=concatenated,
            message=msg,
            processing_time_ms=round(duration_ms, 2)
        )
    except Exception as e:
        duration_ms = (time.time() - start_time) * 1000.0
        return OCRResponse(
            success=False,
            count=0,
            items=[],
            concatenated_text="",
            message=f"OCR error: {str(e)}",
            processing_time_ms=round(duration_ms, 2)
        )


@app.post("/analyze", response_model=ImageAnalysisResponse)
async def analyze_image(file: UploadFile = File(...)):
    """
    Runs the complete multi-library Computer Vision pipeline:
    1. OpenCV: image loading, validation, aspect ratio, brightness, contrast, blur metric
    2. scikit-image: exposure check, Sobel edge density, HSV saturation, Shannon entropy
    3. TensorFlow: image preprocessing, variance, and global semantic classification
    4. YOLO + PyTorch: spatial object localization
    5. EasyOCR: text extraction
    6. Formulates enriched visual context for Gemini Multimodal VQA
    """
    start_time = time.time()
    try:
        contents = await file.read()
        # 1. OpenCV
        img_bgr = opencv_proc.decode_image(contents)
        resized_bgr, scale = opencv_proc.resize_with_aspect_ratio(img_bgr, max_dimension=1280)
        img_rgb = opencv_proc.bgr_to_rgb(resized_bgr)

        cv_metrics_raw = opencv_proc.compute_quality_metrics(resized_bgr)
        opencv_metrics = OpenCVMetrics(**cv_metrics_raw)

        # 2. scikit-image
        sk_metrics_raw = skimage_analyzer.analyze_properties(img_rgb)
        skimage_metrics = SkimageMetrics(**sk_metrics_raw)

        # 3. TensorFlow
        tf_metrics_raw = tf_module.analyze_tensor(img_rgb)
        tensorflow_features = TensorFlowFeatures(**tf_metrics_raw)

        # 4. YOLO (PyTorch)
        detected_objects = yolo_detector.detect(img_rgb, confidence_threshold=0.30)

        # 5. EasyOCR
        ocr_items = ocr_engine.read_text(img_rgb)

        # 6. Formulate enriched context prompt for Gemini VQA
        context_lines = [
            "### COMPUTER VISION PIPELINE TELEMETRY ###",
            f"- Image Dimensions: {opencv_metrics.width}x{opencv_metrics.height} px",
            f"- Lighting & Contrast: Brightness={opencv_metrics.brightness_mean}/255, Contrast RMS={opencv_metrics.contrast_rms}, Low Contrast={skimage_metrics.is_low_contrast}",
            f"- Sharpness: Laplacian Blur Var={opencv_metrics.blur_laplacian_var} ({'Blurry' if opencv_metrics.is_blurry else 'Sharp'}), Sobel Edge Density={skimage_metrics.sobel_edge_density}",
            f"- Information Entropy: {skimage_metrics.shannon_entropy} (Saturation={skimage_metrics.mean_saturation})",
        ]

        if tensorflow_features.top_predictions:
            preds_str = ", ".join([f"{p['label']} ({round(p['probability']*100, 1)}%)" for p in tensorflow_features.top_predictions])
            context_lines.append(f"- TensorFlow Scene Classification: {preds_str}")

        if detected_objects:
            objs_str = ", ".join([f"{obj.class_name} ({round(obj.confidence * 100, 1)}%)" for obj in detected_objects])
            context_lines.append(f"- YOLO Detected Objects: {objs_str}")
        else:
            context_lines.append("- YOLO Detected Objects: None detected above threshold.")

        if ocr_items:
            text_str = ", ".join([f'"{it.text}"' for it in ocr_items])
            context_lines.append(f"- EasyOCR Detected Text: {text_str}")
        else:
            context_lines.append("- EasyOCR Detected Text: None detected.")

        context_prompt = "\n".join(context_lines)
        duration_ms = (time.time() - start_time) * 1000.0

        return ImageAnalysisResponse(
            success=True,
            opencv=opencv_metrics,
            scikit_image=skimage_metrics,
            tensorflow=tensorflow_features,
            yolo_detections=detected_objects,
            ocr_results=ocr_items,
            vqa_context_prompt=context_prompt,
            processing_time_ms=round(duration_ms, 2),
            message="Computer Vision pipeline execution completed successfully."
        )

    except Exception as e:
        duration_ms = (time.time() - start_time) * 1000.0
        raise HTTPException(status_code=500, detail=f"Pipeline processing failed: {str(e)}")


if __name__ == "__main__":
    port = int(os.environ.get("PORT", 8000))
    print(f"[VisionAI] Starting CV Engine server on http://0.0.0.0:{port} ...")
    uvicorn.run(app, host="0.0.0.0", port=port)
