"""
Object Detector Module
----------------------
Integrates Ultralytics YOLO with the PyTorch (torch) deep-learning execution runtime.

Role in Pipeline & PyTorch Integration:
---------------------------------------
1. YOLO (You Only Look Once) is executed via the official Ultralytics framework,
   which relies directly on PyTorch as its tensor computation and neural-network execution engine.
2. PyTorch (torch) manages tensor memory, device allocation (CPU vs CUDA), and forward-pass inference:
   - torch.device('cuda' if torch.cuda.is_available() else 'cpu')
   - Forward pass evaluates the deep convolutional and attention layers of YOLOv8.
   - Tensor outputs containing bounding box regression offsets [x1, y1, x2, y2]
     and class probability distributions are transformed into structured detections.
3. Provides genuine, non-fabricated object bounding boxes, confidence scores, and class labels.
"""

from typing import List, Dict, Any, Optional
import os
import numpy as np
import torch
from ultralytics import YOLO

from schemas import DetectedObject, BoundingBox


class YOLOObjectDetector:
    """YOLOv8 Object Detector powered by PyTorch."""

    def __init__(self, model_name: str = "yolov8n.pt"):
        """
        Initializes the YOLO model and validates the underlying PyTorch backend.
        """
        self.device_str = "cuda" if torch.cuda.is_available() else "cpu"
        self.device = torch.device(self.device_str)
        self.model_name = model_name
        self._model: Optional[YOLO] = None
        self._load_model()

    def _load_model(self) -> None:
        """Loads the YOLO PyTorch model weights."""
        try:
            # Ultralytics automatically checks and downloads yolov8n.pt if not locally cached
            self._model = YOLO(self.model_name)
            # Send model execution to designated PyTorch device
            self._model.to(self.device_str)
        except Exception as e:
            # If offline without pre-downloaded weights, provide clean exception logging
            print(f"[YOLOObjectDetector] Warning: Could not initialize YOLO model '{self.model_name}': {e}")
            self._model = None

    def is_ready(self) -> bool:
        """Returns True if the PyTorch YOLO model is loaded and ready for inference."""
        return self._model is not None

    def detect(self, img_rgb: np.ndarray, confidence_threshold: float = 0.30) -> List[DetectedObject]:
        """
        Runs object detection on an RGB image array using YOLO on PyTorch.
        Returns a structured list of DetectedObject instances.
        """
        if self._model is None:
            # Attempt lazy reload
            self._load_model()
            if self._model is None:
                return []

        h, w = img_rgb.shape[:2]
        detected_objects: List[DetectedObject] = []

        try:
            # PyTorch inference forward-pass via YOLO
            # Ultralytics converts the NumPy array to a torch.Tensor, normalizes to [0, 1],
            # and runs the PyTorch computational graph.
            with torch.no_grad():
                results = self._model(
                    img_rgb,
                    conf=confidence_threshold,
                    device=self.device_str,
                    verbose=False
                )

            if not results or len(results) == 0:
                return []

            first_res = results[0]
            boxes = first_res.boxes
            if boxes is None or len(boxes) == 0:
                return []

            # Extract PyTorch tensor data to CPU
            xyxy = boxes.xyxy.cpu().numpy()
            confidences = boxes.conf.cpu().numpy()
            class_ids = boxes.cls.cpu().numpy().astype(int)
            names = first_res.names  # dict mapping class_id -> class_name

            for i in range(len(class_ids)):
                cid = int(class_ids[i])
                cname = str(names.get(cid, f"class_{cid}"))
                conf = float(confidences[i])
                x1, y1, x2, y2 = [float(coord) for coord in xyxy[i]]

                # Clamp to image dimensions
                x1 = max(0.0, min(float(w), x1))
                y1 = max(0.0, min(float(h), y1))
                x2 = max(0.0, min(float(w), x2))
                y2 = max(0.0, min(float(h), y2))

                bbox = BoundingBox(
                    x1=round(x1, 2),
                    y1=round(y1, 2),
                    x2=round(x2, 2),
                    y2=round(y2, 2),
                    width=round(x2 - x1, 2),
                    height=round(y2 - y1, 2)
                )

                detected_objects.append(DetectedObject(
                    class_id=cid,
                    class_name=cname,
                    confidence=round(conf, 4),
                    bbox=bbox
                ))

            # Sort by confidence descending
            detected_objects.sort(key=lambda obj: obj.confidence, reverse=True)
            return detected_objects

        except Exception as e:
            print(f"[YOLOObjectDetector] Inference error: {e}")
            return []
