"""
scikit-image Image Analyzer Module
----------------------------------
Performs scientific, structural, and exposure analysis on images using scikit-image (skimage).

Why scikit-image is used in addition to OpenCV:
----------------------------------------------
While OpenCV (cv2) is optimized for real-time computer vision and basic array transforms,
scikit-image provides scientific, research-grade algorithms:
1. skimage.exposure: Mathematically rigorous dynamic range evaluation and low-contrast detection.
2. skimage.filters.sobel: Isotropic 3x3 Sobel gradient filtering for accurate edge density calculations.
3. skimage.color.rgb2hsv: Scientific color-space conversion for analyzing chroma saturation.
4. skimage.measure.shannon_entropy: Quantifies structural complexity and texture density
   based on grayscale information entropy.
"""

from typing import Dict, Any
import numpy as np
import skimage.color
import skimage.exposure
import skimage.filters
import skimage.measure


class SkimageAnalyzer:
    """Scientific image analysis using scikit-image."""

    @staticmethod
    def analyze_properties(img_rgb: np.ndarray) -> Dict[str, Any]:
        """
        Calculates scientific image metrics using scikit-image functions.
        Accepts an RGB image in NumPy uint8 format [0, 255].
        """
        # Ensure image is in [0, 1] float representation for scientific computations
        img_float = skimage.img_as_float(img_rgb)
        gray_float = skimage.color.rgb2gray(img_rgb)

        # 1. Scientific exposure analysis: is_low_contrast
        # Determines if the image's dynamic range is too narrow for reliable downstream CV
        is_low_contrast = bool(skimage.exposure.is_low_contrast(img_rgb, fraction_threshold=0.05))

        # 2. Edge density via Sobel filter
        # Sobel calculates the gradient magnitude of image intensity at each point
        sobel_edges = skimage.filters.sobel(gray_float)
        edge_density = float(np.mean(sobel_edges))

        # 3. Color saturation via scientific HSV transformation
        hsv_img = skimage.color.rgb2hsv(img_float)
        saturation_channel = hsv_img[:, :, 1]
        mean_saturation = float(np.mean(saturation_channel))

        # 4. Shannon Entropy: Structural complexity of the image
        entropy = float(skimage.measure.shannon_entropy(gray_float))

        # 5. Scientific summary string explaining findings
        summary_parts = []
        if is_low_contrast:
            summary_parts.append("Low contrast image detected (narrow histogram distribution).")
        else:
            summary_parts.append("Adequate contrast and healthy dynamic range.")

        if edge_density > 0.08:
            summary_parts.append("High geometric texture and edge complexity.")
        elif edge_density < 0.02:
            summary_parts.append("Smooth or uniform image with minimal edge gradients.")
        else:
            summary_parts.append("Moderate structural edge content.")

        if mean_saturation > 0.45:
            summary_parts.append("Vibrant, high-chroma color saturation.")
        elif mean_saturation < 0.15:
            summary_parts.append("Subdued or near-monochromatic color profile.")
        else:
            summary_parts.append("Natural, balanced color saturation.")

        scientific_summary = " ".join(summary_parts)

        return {
            "is_low_contrast": is_low_contrast,
            "sobel_edge_density": round(edge_density, 4),
            "mean_saturation": round(mean_saturation, 4),
            "shannon_entropy": round(entropy, 4),
            "scientific_summary": scientific_summary
        }
