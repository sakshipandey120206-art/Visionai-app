package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.utils.ImageUtils
import com.example.viewmodel.ResultsTab
import com.example.viewmodel.VisionUiState
import com.example.viewmodel.VisionViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisionAIScreen(
    viewModel: VisionViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Activity result launcher for Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val bitmap = ImageUtils.uriToBitmap(context, uri)
            if (bitmap != null) {
                viewModel.onImageSelected(bitmap, uri)
            } else {
                Toast.makeText(context, "Could not load selected image.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Camera capture launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            viewModel.onImageSelected(bitmap, null)
        }
    }

    // Permission launcher for camera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Camera permission is required to capture photos.", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchCamera() {
        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            cameraLauncher.launch(null)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Auto-scroll to bottom of conversation on new message
    LaunchedEffect(uiState.conversationHistory.size) {
        if (uiState.conversationHistory.isNotEmpty()) {
            listState.animateScrollToItem(uiState.conversationHistory.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "VisionAI",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "VQA + CV",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Multimodal Visual Intelligence System",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    BackendStatusBadge(
                        status = uiState.backendStatus,
                        onClick = { viewModel.toggleBackendDialog(true) },
                        modifier = Modifier.testTag("backend_status_badge")
                    )
                    if (uiState.selectedImageBitmap != null) {
                        IconButton(
                            onClick = { viewModel.clearImage() },
                            modifier = Modifier.testTag("clear_image_button")
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Bottom Question Input Bar
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    // Quick questions row (horizontal scroll)
                    if (uiState.selectedImageBitmap != null) {
                        val quickQuestions = listOf(
                            "What is in this image?",
                            "Describe this image.",
                            "What objects can you identify?",
                            "How many people are visible?",
                            "What is the main subject?",
                            "What text is visible?"
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(bottom = 6.dp)
                        ) {
                            quickQuestions.forEach { prompt ->
                                QuickQuestionChip(
                                    text = prompt,
                                    onClick = {
                                        viewModel.onQuestionChange(prompt)
                                    }
                                )
                            }
                        }
                    }

                    // Input Text Field & Send Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = uiState.currentQuestion,
                            onValueChange = { viewModel.onQuestionChange(it) },
                            placeholder = { Text("Ask a question about this image…") },
                            trailingIcon = {
                                if (uiState.currentQuestion.isNotBlank()) {
                                    IconButton(onClick = { viewModel.onQuestionChange("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear text")
                                    }
                                }
                            },
                            enabled = uiState.selectedImageBitmap != null && !uiState.isLoading,
                            singleLine = true,
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("question_input_field")
                        )

                        val canSend = uiState.selectedImageBitmap != null &&
                                uiState.currentQuestion.isNotBlank() &&
                                !uiState.isLoading

                        FloatingActionButton(
                            onClick = { viewModel.askQuestion() },
                            containerColor = if (canSend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (canSend) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            shape = CircleShape,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("ask_ai_button")
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Ask AI",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Error banner
            AnimatedVisibility(visible = uiState.errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.dismissError() },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Status message during active processing
            AnimatedVisibility(visible = uiState.isLoading) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Text(
                            text = uiState.statusMessage,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Main scrollable content
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                // Section 1: Image Card
                item {
                    val bitmap = uiState.selectedImageBitmap
                    if (bitmap != null) {
                        ImagePreviewCard(
                            bitmap = bitmap,
                            onReplace = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onRemove = { viewModel.clearImage() },
                            modifier = Modifier.testTag("image_preview_card")
                        )
                    } else {
                        EmptyImageCard(
                            onPickGallery = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onLaunchCamera = { launchCamera() },
                            modifier = Modifier.testTag("empty_image_card")
                        )
                    }
                }

                // Section 2: Action Toolbar (Enabled when image is loaded)
                if (uiState.selectedImageBitmap != null) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Computer Vision Operations",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                // Describe Image (Gemini Multimodal)
                                Button(
                                    onClick = { viewModel.describeImage() },
                                    enabled = !uiState.isLoading,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.testTag("describe_image_button")
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Describe Image", style = MaterialTheme.typography.labelMedium)
                                }

                                // Detect Objects (YOLO / PyTorch)
                                FilledTonalButton(
                                    onClick = { viewModel.detectObjects() },
                                    enabled = !uiState.isLoading,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("detect_objects_button")
                                ) {
                                    Icon(Icons.Default.Widgets, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Detect Objects", style = MaterialTheme.typography.labelMedium)
                                }

                                // Read Text (EasyOCR)
                                FilledTonalButton(
                                    onClick = { viewModel.readText() },
                                    enabled = !uiState.isLoading,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("read_text_button")
                                ) {
                                    Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Read Text", style = MaterialTheme.typography.labelMedium)
                                }

                                // Analyze Image (OpenCV + skimage + TF + YOLO + OCR)
                                FilledTonalButton(
                                    onClick = { viewModel.analyzeImage() },
                                    enabled = !uiState.isLoading,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("analyze_image_button")
                                ) {
                                    Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Analyze Image", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }

                    // Section 3: Active CV Results Panels
                    item {
                        when (uiState.activeTab) {
                            ResultsTab.OBJECTS -> {
                                uiState.detectionResults?.let { resp ->
                                    DetectionsView(
                                        response = resp,
                                        onClose = { viewModel.setActiveTab(ResultsTab.NONE) },
                                        modifier = Modifier.testTag("detections_view")
                                    )
                                }
                            }
                            ResultsTab.OCR -> {
                                uiState.ocrResults?.let { resp ->
                                    OCRResultView(
                                        response = resp,
                                        onClose = { viewModel.setActiveTab(ResultsTab.NONE) },
                                        modifier = Modifier.testTag("ocr_view")
                                    )
                                }
                            }
                            ResultsTab.ANALYSIS -> {
                                uiState.analysisResults?.let { resp ->
                                    FullAnalysisView(
                                        response = resp,
                                        onClose = { viewModel.setActiveTab(ResultsTab.NONE) },
                                        modifier = Modifier.testTag("analysis_view")
                                    )
                                }
                            }
                            ResultsTab.NONE -> {
                                // Collapsed state
                            }
                        }
                    }

                    // Section 4: Visual Q&A Conversation Header
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Text(
                                text = "Visual Q&A Stream (${uiState.conversationHistory.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (uiState.conversationHistory.isNotEmpty()) {
                                TextButton(
                                    onClick = { viewModel.clearConversation() }
                                ) {
                                    Text("Clear Chat", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    // Section 5: Conversation Items
                    if (uiState.conversationHistory.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.QuestionAnswer,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "No questions asked yet",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Type any question below or tap a quick question chip",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    } else {
                        items(uiState.conversationHistory, key = { it.id }) { message ->
                            ConversationItemView(
                                message = message,
                                modifier = Modifier.testTag("conversation_item_${message.id}")
                            )
                        }
                    }
                }
            }
        }
    }

    // Backend Connection Settings Dialog
    if (uiState.showBackendConfigDialog) {
        BackendConfigDialog(
            status = uiState.backendStatus,
            onDismiss = { viewModel.toggleBackendDialog(false) },
            onSaveUrl = { viewModel.updateBackendUrl(it) },
            onTestConnection = { viewModel.checkBackendHealth() }
        )
    }
}
