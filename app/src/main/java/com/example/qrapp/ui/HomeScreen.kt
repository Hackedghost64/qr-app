package com.example.qrapp.ui

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.qrapp.generator.QrGenerator
import com.example.qrapp.model.QrCustomizationConfig
import com.example.qrapp.ui.components.AppHeader
import com.example.qrapp.ui.components.CustomizationSection
import com.example.qrapp.ui.components.LinkInputCard
import com.example.qrapp.ui.components.QrPreviewCard
import com.example.qrapp.util.QrExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    var linkText by remember { mutableStateOf("https://github.com") }
    var activeQrLink by remember { mutableStateOf("") }
    var config by remember { mutableStateOf(QrCustomizationConfig()) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Helper to generate preview
    fun updatePreview(link: String, currentConfig: QrCustomizationConfig) {
        if (link.isBlank()) return
        coroutineScope.launch {
            val bmp = withContext(Dispatchers.Default) {
                QrGenerator.generateQrBitmap(
                    content = link,
                    config = currentConfig,
                    targetSize = 512
                )
            }
            previewBitmap = bmp
        }
    }

    // Re-render preview whenever config changes and QR is already generated
    LaunchedEffect(config) {
        if (activeQrLink.isNotBlank()) {
            updatePreview(activeQrLink, config)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            AppHeader()

            // Input Card
            LinkInputCard(
                linkText = linkText,
                onLinkChange = { linkText = it },
                onPasteClick = {
                    val clip = QrExporter.getClipboardText(context)
                    if (!clip.isNullOrBlank()) {
                        linkText = clip
                        Toast.makeText(context, "Pasted from clipboard", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                    }
                },
                onGenerateClick = {
                    val normalized = QrExporter.normalizeUrl(linkText)
                    activeQrLink = normalized
                    updatePreview(normalized, config)
                },
                isValidUrl = QrExporter.isValidUrl(linkText)
            )

            // QR Preview & Customization (Visible when generated)
            AnimatedVisibility(
                visible = previewBitmap != null,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 3 }
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    QrPreviewCard(
                        qrBitmap = previewBitmap,
                        currentLink = activeQrLink,
                        onDownloadClick = {
                            coroutineScope.launch {
                                val fullResBitmap = withContext(Dispatchers.Default) {
                                    QrGenerator.generateQrBitmap(
                                        content = activeQrLink,
                                        config = config,
                                        targetSize = config.exportResolution
                                    )
                                }
                                val result = QrExporter.saveBitmapToGallery(
                                    context = context,
                                    bitmap = fullResBitmap,
                                    filenamePrefix = "QR_${System.currentTimeMillis()}"
                                )
                                if (result.isSuccess) {
                                    snackbarHostState.showSnackbar(
                                        "Saved to Pictures/QR Studio (${config.exportResolution}x${config.exportResolution}px)"
                                    )
                                } else {
                                    snackbarHostState.showSnackbar(
                                        "Failed to save: ${result.exceptionOrNull()?.localizedMessage}"
                                    )
                                }
                            }
                        },
                        onShareClick = {
                            coroutineScope.launch {
                                val fullResBitmap = withContext(Dispatchers.Default) {
                                    QrGenerator.generateQrBitmap(
                                        content = activeQrLink,
                                        config = config,
                                        targetSize = 1024
                                    )
                                }
                                QrExporter.shareBitmap(context, fullResBitmap, "Share QR Code")
                            }
                        },
                        onCopyLinkClick = {
                            QrExporter.copyToClipboard(context, activeQrLink, "QR Code Link")
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Link copied to clipboard!")
                            }
                        }
                    )

                    CustomizationSection(
                        config = config,
                        onConfigChange = { updated ->
                            config = updated
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
