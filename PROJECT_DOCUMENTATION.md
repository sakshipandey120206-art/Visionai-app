# VisionAI Engineering & Academic Documentation

**Project Title:** VisionAI: Intelligent Visual Question Answering System Using Multimodal AI and Computer Vision  
**Academic Category:** Computer Vision / Artificial Intelligence / Multimodal AI  
**Primary Focus:** Visual Question Answering (VQA) with Hybrid Algorithmic Telemetry  

---

## 1. Abstract

Visual Question Answering (VQA) represents a frontier challenge at the intersection of Computer Vision (CV) and Natural Language Processing (NLP). While modern multimodal Large Language Models (such as Google DeepMind's Gemini) excel at zero-shot semantic comprehension, they often benefit from explicit algorithmic priors such as spatial bounding boxes, low-level illumination metrics, and OCR character extractions.

**VisionAI** bridges this gap by creating an end-to-end multi-tiered system:
1. Native client layer for image capture and conversational dialogue.
2. An analytical Computer Vision engine orchestrating six fundamental Python libraries: **OpenCV**, **scikit-image**, **YOLOv8**, **PyTorch**, **EasyOCR**, and **TensorFlow**.
3. A multimodal cloud reasoning model (**Gemini 3.5 Flash**) providing accurate natural language responses grounded in both raw pixels and computer vision telemetry.

---

## 2. Multi-Library Integration Specification

### 2.1 OpenCV (`cv2`) — Low-Level Image Processing
- **Decoupled Bytes Parsing:** Ingests raw HTTP multipart payloads into contiguous 8-bit unsigned integer arrays (`np.uint8`) and reconstructs the BGR matrix via `cv2.imdecode`.
- **Dynamic Downscaling:** Enforces a maximum dimension constraint of 1280px using area relation interpolation (`cv2.INTER_AREA`) to preserve spatial frequencies without excessive memory consumption.
- **RMS Contrast & Luminance:** Computes the arithmetic mean and root-mean-square (RMS) deviation of the single-channel grayscale matrix `cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)`.
- **Laplacian Blur Quantification:** Convolves the grayscale image with the second-order differential Laplacian operator:
  $$\nabla^2 f = \frac{\partial^2 f}{\partial x^2} + \frac{\partial^2 f}{\partial y^2}$$
  The variance of the resulting coefficients quantifies high-frequency edge transitions. Values $< 85.0$ trigger a blur flag.

### 2.2 scikit-image (`skimage`) — Scientific Image Analysis
- **Why used alongside OpenCV:** OpenCV prioritizes raw throughput and spatial convolutions, whereas scikit-image provides scientific implementations for statistical exposure, directional gradient filtering, and information entropy.
- **Dynamic Range Assessment:** `skimage.exposure.is_low_contrast` evaluates histogram quantile distribution to detect underexposed or overexposed captures.
- **Sobel Gradient Magnitude:** Employs `skimage.filters.sobel` to compute normalized 2D gradient vectors across pixel neighborhoods, measuring edge density.
- **Shannon Entropy:** `skimage.measure.shannon_entropy` calculates statistical randomness of pixel intensity distributions:
  $$H = -\sum_{i} P(i) \log_2 P(i)$$

### 2.3 YOLOv8 (`ultralytics`) & PyTorch (`torch`) — Deep Object Detection
- **Model Architecture:** YOLOv8 Nano (`yolov8n.pt`) with anchor-free decoupled head.
- **PyTorch Execution:** The PyTorch framework (`torch.Tensor`, `torch.no_grad()`, `torch.cuda`) manages device tensors, executes model weights, and outputs non-maximum suppression (NMS) bounding boxes with COCO labels.
- **Coordinates:** Emits standardized bounding box bounds $[x_1, y_1, x_2, y_2]$ paired with confidence probabilities.

### 2.4 EasyOCR (`easyocr`) — Optical Character Recognition
- **Architecture:** Combines CRAFT (Character Region Awareness for Text Detection) with deep ResNet-LSTM-CTC recognition.
- **Execution:** Detects multilingual text runs in natural environments, extracting polygon coordinates and text confidence.

### 2.5 TensorFlow (`tensorflow`) — Global Semantic Extraction
- **Image Ops Pipeline:** Uses `tf.image.resize` and `tf.image.per_image_standardization` to evaluate pixel variance.
- **MobileNetV2 Classifier:** Passes preprocessed images through a `tf.keras.applications.MobileNetV2` network. Softmax activations generate global ImageNet classifications that complement YOLO's localized bounding boxes.

### 2.6 Gemini Multimodal VQA (`gemini-3.5-flash`)
- **Direct REST Integration:** Invokes Google Generative Language API (`/v1beta/models/gemini-3.5-flash:generateContent`).
- **Telemetry Fusion:** Ingests base64-encoded JPEG image bytes along with user questions, augmented by telemetry extracted from the CV engine (detected objects, OCR text, lighting conditions).

---

## 3. Data Flow Diagram

```
[User Camera / Gallery]
       |
       v
[Android ImageUtils (Rotation & JPEG Compression)]
       |
       +-----------------------------------+
       |                                   |
       v                                   v
[Python CV Engine: /analyze]      [Gemini REST Client]
  1. cv2 (Decode, Resize, Blur)            |
  2. skimage (Exposure, Entropy)           |
  3. YOLO + PyTorch (Objects)              |
  4. EasyOCR (Text)                        |
  5. TensorFlow (MobileNetV2)              |
       |                                   |
       v                                   |
 [CV Telemetry Metadata]                   |
       |                                   |
       +---------------------------------->+
                                           |
                                           v
                             [Gemini Multimodal Reasoning]
                                           |
                                           v
                            [Structured Answer to UI]
```

---

## 4. API Endpoints Contract (Python Engine)

| Method | Route | Payload | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/health` | None | Reports engine status and installed versions of all 6 libraries. |
| `POST` | `/detect` | `multipart/form-data: file` | Returns YOLO-detected objects with bounding boxes and confidences. |
| `POST` | `/ocr` | `multipart/form-data: file` | Returns EasyOCR text extractions. |
| `POST` | `/analyze` | `multipart/form-data: file` | Comprehensive report aggregating cv2, skimage, TF, YOLO, and EasyOCR. |

---

## 5. Security & Academic Integrity Notes

- **No Simulated Data:** All detections, bounding boxes, and OCR results are derived from real framework inference. If no text or objects are present, empty lists are returned without fabrication.
- **Client Key Security:** In production systems, client applications should proxy AI calls through an authenticated backend. For this college project demonstration, the key is passed via `BuildConfig.GEMINI_API_KEY` with zero hardcoding in source control.
