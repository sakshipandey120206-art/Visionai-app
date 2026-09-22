"""
EasyOCR Engine Module
---------------------
Provides Optical Character Recognition (OCR) using EasyOCR.

Role in Pipeline:
-----------------
1. Detects text occurrences within the input image.
2. Recognizes alphanumeric characters and symbols with confidence scores.
3. Provides bounding quadrilateral coordinates for each recognized text segment.
4. Returns an empty or uncertainty state if no text is detectable or if confidence is low.
"""

from typing import List, Optional
import numpy as np
import easyocr

from schemas import OCRItem


class EasyOCREngine:
    """Optical Character Recognition engine using EasyOCR."""

    def __init__(self, languages: Optional[List[str]] = None, use_gpu: bool = False):
        if languages is None:
            languages = ['en']
        self.languages = languages
        self.use_gpu = use_gpu
        self._reader: Optional[easyocr.Reader] = None
        self._init_reader()

    def _init_reader(self) -> None:
        """Initializes the EasyOCR reader model."""
        try:
            self._reader = easyocr.Reader(self.languages, gpu=self.use_gpu)
        except Exception as e:
            print(f"[EasyOCREngine] Warning: Could not initialize EasyOCR reader: {e}")
            self._reader = None

    def is_ready(self) -> bool:
        """Returns True if EasyOCR reader is initialized."""
        return self._reader is not None

    def read_text(self, img_rgb: np.ndarray, min_confidence: float = 0.20) -> List[OCRItem]:
        """
        Executes EasyOCR text detection and recognition on the RGB image.
        Returns a list of OCRItem instances with text, confidence, and polygon bounding box.
        """
        if self._reader is None:
            self._init_reader()
            if self._reader is None:
                return []

        results = []
        try:
            # EasyOCR expects an RGB NumPy array or image path
            ocr_output = self._reader.readtext(img_rgb)

            for bbox, text, confidence in ocr_output:
                if confidence >= min_confidence:
                    clean_text = str(text).strip()
                    if clean_text:
                        # Convert bbox coordinate list to integers
                        clean_bbox = [[int(pt[0]), int(pt[1])] for pt in bbox]
                        results.append(OCRItem(
                            text=clean_text,
                            confidence=round(float(confidence), 4),
                            bbox=clean_bbox
                        ))

            # Sort by confidence descending
            results.sort(key=lambda item: item.confidence, reverse=True)
            return results

        except Exception as e:
            print(f"[EasyOCREngine] OCR read error: {e}")
            return []
