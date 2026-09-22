package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BackendStatus
import com.example.model.DetectionResponse
import com.example.model.ImageAnalysisResponse
import com.example.model.OCRResponse
import com.example.model.QAMessage
import com.example.viewmodel.ResultsTab
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackendStatusBadge(
    status: BackendStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when {
        status.isChecking -> MaterialTheme.colorScheme.tertiary
        status.isConnected -> Color(0xFF10B981) // Emerald
        else -> Color(0xFFF59E0B) // Amber
    }

    val statusText = when {
        status.isChecking -> "Checking CV..."
        status.isConnected -> "CV: Ready"
        else -> "CV: Offline"
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = statusColor.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ImagePreviewCard(
    bitmap: Bitmap,
    onReplace: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            androidx.compose.foundation.Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Selected image preview",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp, max = 280.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.Black.copy(alpha = 0.05f))
            )

            // Overlay action buttons
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = onReplace,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Replace Image", modifier = Modifier.size(18.dp))
                }

                SmallFloatingActionButton(
                    onClick = onRemove,
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Remove Image", modifier = Modifier.size(18.dp))
                }
            }

            // Dimension badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.65f),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = "${bitmap.width} × ${bitmap.height} px",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyImageCard(
    onPickGallery: () -> Unit,
    onLaunchCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp, horizontal = 20.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoCamera,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Upload or capture an image to begin",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Supports YOLO object detection, EasyOCR, scikit-image analysis, and Gemini Multimodal VQA",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onPickGallery,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Select Photo")
                }

                OutlinedButton(
                    onClick = onLaunchCamera,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Camera")
                }
            }
        }
    }
}

@Composable
fun QuickQuestionChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SuggestionChip(
        onClick = onClick,
        label = { Text(text, style = MaterialTheme.typography.bodySmall) },
        shape = RoundedCornerShape(16.dp),
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        modifier = modifier
    )
}

@Composable
fun DetectionsView(
    response: DetectionResponse,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Widgets, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Detected Objects (YOLO / PyTorch)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                }
            }

            Text(
                text = "Model: YOLOv8 Nano | Inference time: ${response.processingTimeMs}ms",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )

            if (response.objects.isEmpty()) {
                Text(
                    text = "No objects were confidently detected.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    response.objects.forEach { obj ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = obj.className.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            val pct = (obj.confidence * 100).toInt()
                            Text(
                                text = "$pct%",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (pct >= 80) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "[${obj.bbox.x1.toInt()},${obj.bbox.y1.toInt()}]",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OCRResultView(
    response: OCRResponse,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TextFields, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Detected Text (EasyOCR)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                }
            }

            Text(
                text = "Language: English | Inference time: ${response.processingTimeMs}ms",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )

            if (response.items.isEmpty()) {
                Text(
                    text = "No readable text was detected.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    response.items.forEach { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = item.text,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            val pct = (item.confidence * 100).toInt()
                            Text(
                                text = "$pct%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (response.concatenatedText.isNotBlank()) {
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Detected Text", response.concatenatedText)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(top = 6.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy All Text", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FullAnalysisView(
    response: ImageAnalysisResponse,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Analytics, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CV Multi-Library Analysis",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 1. OpenCV Section
            Text(
                text = "1. OpenCV (cv2) Low-Level Metrics",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                MetricPill("Brightness", "${response.opencv.brightnessMean.toInt()}/255", Modifier.weight(1f))
                MetricPill("Contrast RMS", "${response.opencv.contrastRms.toInt()}", Modifier.weight(1f))
                MetricPill(
                    "Sharpness",
                    if (response.opencv.isBlurry) "Blurry" else "Sharp",
                    Modifier.weight(1f),
                    highlight = !response.opencv.isBlurry
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. scikit-image Section
            Text(
                text = "2. scikit-image (skimage) Structural Analysis",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                MetricPill("Edge Density", "${(response.scikitImage.sobelEdgeDensity * 100).toInt()}%", Modifier.weight(1f))
                MetricPill("Saturation", "${(response.scikitImage.meanSaturation * 100).toInt()}%", Modifier.weight(1f))
                MetricPill("Entropy", "${response.scikitImage.shannonEntropy.toInt()}", Modifier.weight(1f))
            }
            Text(
                text = response.scikitImage.scientificSummary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3. TensorFlow Section
            Text(
                text = "3. TensorFlow (tf.keras) Semantic Classifier",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            if (response.tensorflow.topPredictions.isNotEmpty()) {
                Column(modifier = Modifier.padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    response.tensorflow.topPredictions.forEach { pred ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(pred.label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                            Text("${(pred.probability * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Text(
                    text = "Quality: ${response.tensorflow.qualityAssessment}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. YOLO & EasyOCR Summary counts
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                MetricPill("YOLO Objects", "${response.yoloDetections.size} detected", Modifier.weight(1f))
                MetricPill("EasyOCR Text", "${response.ocrResults.size} snippet(s)", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun MetricPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (highlight) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ConversationItemView(
    message: QAMessage,
    modifier: Modifier = Modifier
) {
    val timeStr = remember(message.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        // User Question
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp),
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(
                        text = message.question,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Gemini AI Answer
        Row(
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp),
                color = if (message.isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (message.isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.widthIn(max = 320.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (message.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Gemini VQA",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (message.isCvEnriched) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "CV Enriched",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = timeStr,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = message.answer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun BackendConfigDialog(
    status: BackendStatus,
    onDismiss: () -> Unit,
    onSaveUrl: (String) -> Unit,
    onTestConnection: () -> Unit
) {
    var urlInput by remember { mutableStateOf(status.baseUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Sensors, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("CV Engine Connection")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Configure the address of your Python Computer Vision service (OpenCV, scikit-image, YOLO, EasyOCR, TensorFlow):",
                    style = MaterialTheme.typography.bodySmall
                )

                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("Server URL") },
                    placeholder = { Text("http://10.0.2.2:8000") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val statusText = when {
                        status.isChecking -> "Checking..."
                        status.isConnected -> "Connected (${status.device})"
                        else -> "Offline"
                    }
                    val statusColor = if (status.isConnected) Color(0xFF10B981) else Color(0xFFEF4444)
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(statusColor))
                    Text(statusText, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.weight(1f))
                    OutlinedButton(
                        onClick = onTestConnection,
                        enabled = !status.isChecking,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Test")
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "How to run the Python server:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "cd python/cv_engine\npip install -r requirements.txt\npython main.py",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Text(
                            text = "• Emulator default: http://10.0.2.2:8000\n• Physical phone: use PC Wi-Fi IP (e.g. http://192.168.1.50:8000)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveUrl(urlInput)
                    onDismiss()
                }
            ) {
                Text("Save & Connect")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
