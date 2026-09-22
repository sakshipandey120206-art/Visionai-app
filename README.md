# VisionAI: Intelligent Visual Question Answering System Using Multimodal AI and Computer Vision

A complete, working college Computer Vision mini-project combining traditional Computer Vision algorithms, deep learning inference, object detection, optical character recognition, and cutting-edge multimodal Large Language Model (Gemini) Visual Question Answering.

---

## 🏛️ System Architecture

VisionAI is architected cleanly across three logical tiers to respect native mobile constraints and the Python-centric ecosystem of modern Computer Vision frameworks:

```
+-----------------------------------------------------------------------------------+
|                            LAYER 1: ANDROID FRONTEND                              |
|  - Jetpack Compose + Material 3 UI                                                |
|  - ViewModel + Coroutines + Photo Picker + Camera Capture                         |
|  - Multi-turn Visual Q&A Conversation Stream                                      |
|  - CV Results Dashboard (YOLO Bounding Boxes, EasyOCR Text, OpenCV Metrics)       |
+-----------------------------------------+-----------------------------------------+
                                          |
                        [Image Bytes + Question Prompt]
                                          |
                     +--------------------+--------------------+
                     |                                         |
                     v                                         v
+-------------------------------------------+   +-----------------------------------+
|      LAYER 2: PYTHON CV ENGINE            |   |     LAYER 3: GEMINI VQA CLOUD     |
|  - OpenCV (cv2): Ingestion, Contrast,     |   |  - Model: gemini-3.5-flash        |
|    Brightness, Laplacian Blur, Sharpening |   |  - Direct Multimodal REST API     |
|  - scikit-image: Dynamic Range, Sobel     |   |  - Ingests Image + Prompt         |
|    Edge Density, HSV Saturation, Entropy  |   |  - Augmented with CV Telemetry    |
|  - YOLO (Ultralytics) + PyTorch:          |   |  - Conversational History         |
|    Deep Tensor Object Detection           |   |                                   |
|  - EasyOCR: Text & Typography Recognition |   +-----------------------------------+
|  - TensorFlow (tf.keras): Standardization |
|    Variance & MobileNetV2 Scene Classifier|
+-------------------------------------------+
```

---

## 🔬 Rigorous Role of All 6 Computer Vision & AI Libraries

VisionAI does not use placeholders, mocks, or fake detections. Each requested framework has a genuine, documented function:

| Library | Engine Role & Specific Implementation |
| :--- | :--- |
| **OpenCV (`cv2`)** | **Low-level image processing & validation:** Decodes compressed byte streams (`cv2.imdecode`), aspect-ratio preserving downsampling (`cv2.resize`), RGB/BGR color channel conversion (`cv2.cvtColor`), luminance and RMS contrast standard deviation, and blur detection via the second derivative Laplacian operator (`cv2.Laplacian(gray, cv2.CV_64F).var()`). |
| **scikit-image (`skimage`)** | **Scientific structural & exposure analysis:** Rigorous statistical dynamic range analysis (`skimage.exposure.is_low_contrast`), 3x3 isotropic gradient magnitude edge density calculation (`skimage.filters.sobel`), perceptual chroma saturation distribution (`skimage.color.rgb2hsv`), and information-theoretic structural texture entropy (`skimage.measure.shannon_entropy`). |
| **YOLO (`ultralytics`)** | **Object Detection:** YOLOv8 neural network detecting 80 standard COCO object classes with exact spatial bounding box offsets (`[x1, y1, x2, y2]`) and confidence ratings. |
| **PyTorch (`torch`)** | **Deep Learning Tensor Execution Runtime:** Powers the deep convolutional and transformer layers of YOLOv8. Manages tensor device memory (`torch.cuda` or `torch.device('cpu')`) and performs inference without gradient computation (`torch.no_grad()`). |
| **EasyOCR (`easyocr`)** | **Text Extraction (OCR):** Detects and decodes natural text in scenes with bounding polygon vertices and confidence levels. Returns detected text strings to augment visual comprehension. |
| **TensorFlow (`tensorflow`)** | **Deep Feature Extraction & Global Classification:** Uses `tf.image.resize` and `tf.image.per_image_standardization` to compute normalized tensor variance, and feeds input into a `tf.keras.applications.MobileNetV2` network to infer global scene-level categories. |

---

## 🚀 Getting Started

### 1. Launching the Python Computer Vision Engine (Layer 2)

The Python service runs locally on port 8000:

```bash
cd python/cv_engine
pip install -r requirements.txt
python main.py
```

The server starts on `http://0.0.0.0:8000`. You can verify it by opening `http://localhost:8000/health` in your browser.

#### Connecting from the Android App:
- **Android Emulator**: Uses `http://10.0.2.2:8000` (pre-configured as default in the app settings).
- **Physical Device**: Connect phone and computer to the same Wi-Fi, find your computer's local IP (e.g. `192.168.1.50`), and tap the server badge in the app's top bar to enter `http://192.168.1.50:8000`.

### 2. Configuring Gemini Multimodal API (Layer 3)

The app accesses the Gemini multimodal API using the official REST endpoint with model `gemini-3.5-flash`:
1. Obtain an API key from Google AI Studio.
2. In Google AI Studio, set `GEMINI_API_KEY` in the Secrets panel (or `.env` file).
3. The app automatically reads `BuildConfig.GEMINI_API_KEY`.

---

## 📱 Features of the Android App

- **Photo Ingestion**: High-quality Android Photo Picker and Camera capture with proper runtime permissions.
- **Image Preview**: Rounded preview card with replace and remove controls, maintaining proper aspect ratios.
- **Visual Question Answering**: Ask any question about the selected image. Answers are generated by Gemini multimodal AI (`gemini-3.5-flash`) enriched by CV pipeline detections.
- **Multi-turn Conversation Stream**: Maintain ongoing Q&A history for the same image without re-selecting it.
- **Quick Question Chips**: Instantly populate common visual queries ("What is in this image?", "What objects can you identify?", "What text is visible?").
- **Dedicated CV Actions**:
  - **Detect Objects**: Triggers YOLOv8 + PyTorch to display identified objects and confidence percentages.
  - **Read Text**: Triggers EasyOCR to extract scene text snippets.
  - **Analyze Image**: Runs the complete OpenCV + scikit-image + TensorFlow + YOLO + EasyOCR pipeline to display a telemetry dashboard.
  - **Describe Image**: Generates a rich multimodal scene narrative via Gemini.
- **Graceful Offline & Degradation**: Clearly indicates whether the Python CV engine is connected or offline. When offline, Gemini Cloud VQA continues to operate independently.
