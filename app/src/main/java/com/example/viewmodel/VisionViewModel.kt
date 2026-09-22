package com.example.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.BackendStatus
import com.example.model.DetectionResponse
import com.example.model.ImageAnalysisResponse
import com.example.model.OCRResponse
import com.example.model.QAMessage
import com.example.repository.VisionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ResultsTab {
    NONE,
    OBJECTS,
    OCR,
    ANALYSIS
}

data class VisionUiState(
    val selectedImageBitmap: Bitmap? = null,
    val selectedImageUri: Uri? = null,
    val currentQuestion: String = "",
    val conversationHistory: List<QAMessage> = emptyList(),
    val detectionResults: DetectionResponse? = null,
    val ocrResults: OCRResponse? = null,
    val analysisResults: ImageAnalysisResponse? = null,
    val activeTab: ResultsTab = ResultsTab.NONE,
    val backendStatus: BackendStatus = BackendStatus(),
    val isLoading: Boolean = false,
    val statusMessage: String = "",
    val errorMessage: String? = null,
    val showBackendConfigDialog: Boolean = false
)

class VisionViewModel(
    private val repository: VisionRepository = VisionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(VisionUiState())
    val uiState: StateFlow<VisionUiState> = _uiState.asStateFlow()

    init {
        checkBackendHealth()
    }

    fun onImageSelected(bitmap: Bitmap, uri: Uri? = null) {
        _uiState.update { current ->
            current.copy(
                selectedImageBitmap = bitmap,
                selectedImageUri = uri,
                // Keep existing conversation history or fresh start?
                // The same image can support multiple questions; switching image starts fresh or keeps history?
                // Let's reset CV results for new image, keep or clear conversation as appropriate.
                detectionResults = null,
                ocrResults = null,
                analysisResults = null,
                activeTab = ResultsTab.NONE,
                errorMessage = null
            )
        }
    }

    fun clearImage() {
        _uiState.update { current ->
            current.copy(
                selectedImageBitmap = null,
                selectedImageUri = null,
                currentQuestion = "",
                detectionResults = null,
                ocrResults = null,
                analysisResults = null,
                activeTab = ResultsTab.NONE,
                conversationHistory = emptyList(),
                errorMessage = null
            )
        }
    }

    fun onQuestionChange(newQuestion: String) {
        _uiState.update { it.copy(currentQuestion = newQuestion) }
    }

    fun clearConversation() {
        _uiState.update { it.copy(conversationHistory = emptyList()) }
    }

    fun setActiveTab(tab: ResultsTab) {
        _uiState.update {
            it.copy(activeTab = if (it.activeTab == tab) ResultsTab.NONE else tab)
        }
    }

    fun toggleBackendDialog(show: Boolean) {
        _uiState.update { it.copy(showBackendConfigDialog = show) }
    }

    fun updateBackendUrl(newUrl: String) {
        repository.updateBackendUrl(newUrl)
        _uiState.update {
            it.copy(
                backendStatus = it.backendStatus.copy(baseUrl = repository.getBackendUrl())
            )
        }
        checkBackendHealth()
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun checkBackendHealth() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(backendStatus = it.backendStatus.copy(isChecking = true, errorMessage = null))
            }
            val result = repository.checkBackendHealth()
            result.fold(
                onSuccess = { health ->
                    _uiState.update {
                        it.copy(
                            backendStatus = it.backendStatus.copy(
                                isConnected = true,
                                isChecking = false,
                                lastChecked = System.currentTimeMillis(),
                                libraries = health.libraries,
                                device = health.device,
                                errorMessage = null
                            )
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            backendStatus = it.backendStatus.copy(
                                isConnected = false,
                                isChecking = false,
                                lastChecked = System.currentTimeMillis(),
                                errorMessage = error.localizedMessage ?: "Connection refused"
                            )
                        )
                    }
                }
            )
        }
    }

    fun askQuestion(overrideQuestion: String? = null) {
        val question = (overrideQuestion ?: _uiState.value.currentQuestion).trim()
        val bitmap = _uiState.value.selectedImageBitmap ?: return
        if (question.isBlank() || _uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    statusMessage = "Reasoning with Gemini 3.5 Flash...",
                    currentQuestion = "",
                    errorMessage = null
                )
            }

            val cvTelemetry = _uiState.value.analysisResults?.vqaContextPrompt
            val result = repository.askGeminiVQA(bitmap, question, cvTelemetry)

            result.fold(
                onSuccess = { answer ->
                    val newMsg = QAMessage(
                        question = question,
                        answer = answer,
                        isCvEnriched = !cvTelemetry.isNullOrBlank()
                    )
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            statusMessage = "",
                            conversationHistory = it.conversationHistory + newMsg
                        )
                    }
                },
                onFailure = { error ->
                    val errorMsg = error.localizedMessage ?: "Failed to query Gemini VQA service."
                    val newMsg = QAMessage(
                        question = question,
                        answer = "Error: $errorMsg",
                        isError = true
                    )
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            statusMessage = "",
                            conversationHistory = it.conversationHistory + newMsg,
                            errorMessage = errorMsg
                        )
                    }
                }
            )
        }
    }

    fun describeImage() {
        val bitmap = _uiState.value.selectedImageBitmap ?: return
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    statusMessage = "Analyzing scene description with Gemini...",
                    errorMessage = null
                )
            }

            val cvTelemetry = _uiState.value.analysisResults?.vqaContextPrompt
            val result = repository.describeImage(bitmap, cvTelemetry)

            result.fold(
                onSuccess = { description ->
                    val msg = QAMessage(
                        question = "Describe this image in detail.",
                        answer = description,
                        isCvEnriched = !cvTelemetry.isNullOrBlank()
                    )
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            statusMessage = "",
                            conversationHistory = it.conversationHistory + msg
                        )
                    }
                },
                onFailure = { error ->
                    val errorMsg = error.localizedMessage ?: "Scene description failed."
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            statusMessage = "",
                            errorMessage = errorMsg
                        )
                    }
                }
            )
        }
    }

    fun detectObjects() {
        val bitmap = _uiState.value.selectedImageBitmap ?: return
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    statusMessage = "Running YOLOv8 (PyTorch) object detector...",
                    errorMessage = null
                )
            }

            val result = repository.detectObjects(bitmap)

            result.fold(
                onSuccess = { response ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            statusMessage = "",
                            detectionResults = response,
                            activeTab = ResultsTab.OBJECTS
                        )
                    }
                },
                onFailure = { error ->
                    val errorMsg = "YOLO detection failed: ${error.localizedMessage}. Ensure Python CV engine is running (python/cv_engine/main.py)."
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            statusMessage = "",
                            errorMessage = errorMsg
                        )
                    }
                }
            )
        }
    }

    fun readText() {
        val bitmap = _uiState.value.selectedImageBitmap ?: return
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    statusMessage = "Extracting text with EasyOCR...",
                    errorMessage = null
                )
            }

            val result = repository.readText(bitmap)

            result.fold(
                onSuccess = { response ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            statusMessage = "",
                            ocrResults = response,
                            activeTab = ResultsTab.OCR
                        )
                    }
                },
                onFailure = { error ->
                    val errorMsg = "EasyOCR failed: ${error.localizedMessage}. Ensure Python CV engine is running."
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            statusMessage = "",
                            errorMessage = errorMsg
                        )
                    }
                }
            )
        }
    }

    fun analyzeImage() {
        val bitmap = _uiState.value.selectedImageBitmap ?: return
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    statusMessage = "Running full CV pipeline (OpenCV + skimage + TF + YOLO + OCR)...",
                    errorMessage = null
                )
            }

            val result = repository.analyzeImage(bitmap)

            result.fold(
                onSuccess = { response ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            statusMessage = "",
                            analysisResults = response,
                            detectionResults = DetectionResponse(
                                success = true,
                                count = response.yoloDetections.size,
                                objects = response.yoloDetections,
                                message = "Extracted from full pipeline"
                            ),
                            ocrResults = OCRResponse(
                                success = true,
                                count = response.ocrResults.size,
                                items = response.ocrResults,
                                concatenatedText = response.ocrResults.joinToString(" ") { it.text }
                            ),
                            activeTab = ResultsTab.ANALYSIS
                        )
                    }
                },
                onFailure = { error ->
                    val errorMsg = "CV pipeline analysis failed: ${error.localizedMessage}. Ensure Python CV engine is running."
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            statusMessage = "",
                            errorMessage = errorMsg
                        )
                    }
                }
            )
        }
    }
}
