"""
OpenCV Image Processor Module
-----------------------------
Provides low-level image processing, validation, color conversions,
and structural quality analysis using OpenCV (cv2).

Role in Pipeline:
1. Decode incoming compressed JPEG/PNG bytes safely into standard NumPy array.
2. Validate image dimensions, depth, and channels.
3. Perform RGB/BGR conversions for downstream libraries (YOLO/EasyOCR require RGB).
4. Compute essential visual metrics: brightness, contrast (RMS), and sharpness/blur
   using the Laplacian operator.
5. Provide optional denoising (Gaussian) and unsharp masking.
"""

import io
from typing import Tuple, Dict, Any, List
import numpy as np
import cv2


class OpenCVProcessor:
    """Encapsulates all OpenCV (cv2) low-level image operations."""

    BLUR_THRESHOLD: float = 85.0  # Laplacian variance threshold below which an image is considered blurry

    @staticmethod
    def decode_image(image_bytes: bytes) -> np.ndarray:
        """
        Loads and decodes raw image bytes into a BGR NumPy ndarray.
        Throws ValueError if data is corrupt or unreadable.
        """
        if not image_bytes:
            raise ValueError("Empty image byte buffer received.")

        nparr = np.frombuffer(image_bytes, np.uint8)
        img_bgr = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        if img_bgr is None:
            raise ValueError("OpenCV failed to decode image. Format may be unsupported or corrupted.")
        return img_bgr

    @staticmethod
    def bgr_to_rgb(img_bgr: np.ndarray) -> np.ndarray:
        """Converts OpenCV native BGR format to standard RGB."""
        return cv2.cvtColor(img_bgr, cv2.COLOR_BGR2RGB)

    @staticmethod
    def rgb_to_bgr(img_rgb: np.ndarray) -> np.ndarray:
        """Converts RGB format back to OpenCV native BGR format."""
        return cv2.cvtColor(img_rgb, cv2.COLOR_RGB2BGR)

    @staticmethod
    def resize_with_aspect_ratio(img: np.ndarray, max_dimension: int = 1280) -> Tuple[np.ndarray, float]:
        """
        Resizes an image so its largest dimension is at most max_dimension,
        strictly preserving the aspect ratio.
        """
        h, w = img.shape[:2]
        if max(h, w) <= max_dimension:
            return img, 1.0

        scaling_factor = max_dimension / float(max(h, w))
        new_w = int(w * scaling_factor)
        new_h = int(h * scaling_factor)

        resized = cv2.resize(img, (new_w, new_h), interpolation=cv2.INTER_AREA)
        return resized, scaling_factor

    @classmethod
    def compute_quality_metrics(cls, img_bgr: np.ndarray) -> Dict[str, Any]:
        """
        Calculates brightness, contrast, blur score, and color distribution using cv2 functions.
        """
        h, w, channels = img_bgr.shape
        gray = cv2.cvtColor(img_bgr, cv2.COLOR_BGR2GRAY)

        # 1. Brightness: Mean pixel value in grayscale [0, 255]
        brightness = float(np.mean(gray))

        # 2. Contrast: Standard deviation of pixel values (RMS contrast)
        contrast = float(np.std(gray))

        # 3. Blur Detection: Variance of Laplacian operator
        laplacian = cv2.Laplacian(gray, cv2.CV_64F)
        blur_var = float(laplacian.var())
        is_blurry = blur_var < cls.BLUR_THRESHOLD

        # 4. Color channel means in BGR
        mean_bgr: List[float] = [float(x) for x in cv2.mean(img_bgr)[:3]]

        return {
            "width": int(w),
            "height": int(h),
            "channels": int(channels),
            "brightness_mean": round(brightness, 2),
            "contrast_rms": round(contrast, 2),
            "blur_laplacian_var": round(blur_var, 2),
            "is_blurry": is_blurry,
            "color_mean_bgr": [round(c, 2) for c in mean_bgr]
        }

    @staticmethod
    def enhance_image(img_bgr: np.ndarray, denoise: bool = False, sharpen: bool = False) -> np.ndarray:
        """
        Applies optional Gaussian blur denoising or unsharp masking kernel.
        """
        processed = img_bgr.copy()
        if denoise:
            # Gaussian blur filter to smooth out sensor noise
            processed = cv2.GaussianBlur(processed, (3, 3), sigmaX=0.5)

        if sharpen:
            # Unsharp mask using Gaussian weighting
            blurred = cv2.GaussianBlur(processed, (0, 0), sigmaX=3.0)
            processed = cv2.addWeighted(processed, 1.5, blurred, -0.5, 0)

        return processed

    @staticmethod
    def encode_to_jpeg(img_bgr: np.ndarray, quality: int = 90) -> bytes:
        """Encodes an OpenCV image ndarray into a JPEG byte array."""
        success, encoded = cv2.imencode('.jpg', img_bgr, [int(cv2.IMWRITE_JPEG_QUALITY), quality])
        if not success:
            raise RuntimeError("Failed to encode image to JPEG format.")
        return encoded.tobytes()
