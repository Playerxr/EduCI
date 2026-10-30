package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.models.EducationalIllustration
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.OrangePrimary
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebIllustrationViewerDialog(
    illustrations: List<EducationalIllustration>,
    initialSubjectId: String = "all",
    isOfflineMode: Boolean = false,
    onDismiss: () -> Unit
) {
    var selectedFilterSubject by remember { mutableStateOf(initialSubjectId) }
    var activeIllustration by remember { mutableStateOf<EducationalIllustration?>(null) }
    var showInAppWebView by remember { mutableStateOf(false) }
    var currentWebUrl by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isWebLoading by remember { mutableStateOf(false) }
    var webLoadingProgress by remember { mutableStateOf(0) }
    var webHasError by remember { mutableStateOf(false) }

    val filteredList = remember(illustrations, selectedFilterSubject) {
        if (selectedFilterSubject == "all") {
            illustrations
        } else {
            illustrations.filter { it.subjectId == selectedFilterSubject }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top App Bar
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = if (showInAppWebView) "Explorateur Visuel Web" else "Illustrations & Schémas Scolaires",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (showInAppWebView) currentWebUrl else "Programme officiel CI • SVT, PC, Maths, HG",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (showInAppWebView) {
                                    showInAppWebView = false
                                } else {
                                    onDismiss()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (showInAppWebView) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Close,
                                contentDescription = "Fermer"
                            )
                        }
                    },
                    actions = {
                        if (showInAppWebView) {
                            IconButton(
                                onClick = { webViewInstance?.reload() }
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Actualiser")
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    val defaultQuery = if (selectedFilterSubject == "svt") "schema svt bac terminale" else "schema bac cote d ivoire"
                                    val encoded = URLEncoder.encode(defaultQuery, StandardCharsets.UTF_8.toString())
                                    currentWebUrl = "https://www.google.com/search?tbm=isch&q=$encoded"
                                    showInAppWebView = true
                                }
                            ) {
                                Icon(Icons.Default.Search, contentDescription = "Recherche Web Libre", tint = OrangePrimary)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                if (showInAppWebView) {
                    // In-App Web Viewer Mode
                    BackHandler {
                        if (webViewInstance?.canGoBack() == true) {
                            webViewInstance?.goBack()
                        } else {
                            showInAppWebView = false
                        }
                    }

                    if (isWebLoading) {
                        LinearProgressIndicator(
                            progress = { webLoadingProgress / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = OrangePrimary,
                        )
                    }

                    // Web Controls Bar
                    Surface(
                        tonalElevation = 2.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Rechercher un schéma (ex: mitose, circuit rlc...)", fontSize = 12.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                singleLine = true,
                                shape = RoundedCornerShape(24.dp),
                                trailingIcon = {
                                    if (searchQuery.isNotBlank()) {
                                        IconButton(
                                            onClick = {
                                                val encoded = URLEncoder.encode(searchQuery.trim(), StandardCharsets.UTF_8.toString())
                                                currentWebUrl = "https://www.google.com/search?tbm=isch&q=$encoded"
                                                webViewInstance?.loadUrl(currentWebUrl)
                                            }
                                        ) {
                                            Icon(Icons.Default.ArrowForward, contentDescription = "Aller", tint = OrangePrimary)
                                        }
                                    }
                                }
                            )
                        }
                    }

                    if (isOfflineMode) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.WifiOff, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(40.dp))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Connexion Internet Requise pour la vue Web",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Vous êtes actuellement en Mode Hors-Ligne (0% Data). Vous pouvez toutefois consulter la bibliothèque de schémas officiels pré-intégrés !",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Button(onClick = { showInAppWebView = false }) {
                                        Text("Voir les fiches de schémas intégrées")
                                    }
                                }
                            }
                        }
                    } else {
                        // Native Android WebView
                        Box(modifier = Modifier.fillMaxSize()) {
                            AndroidView(
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        layoutParams = ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT
                                        )
                                        settings.apply {
                                            javaScriptEnabled = true
                                            domStorageEnabled = true
                                            loadWithOverviewMode = true
                                            useWideViewPort = true
                                            builtInZoomControls = true
                                            displayZoomControls = false
                                        }
                                        webViewClient = object : WebViewClient() {
                                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                                isWebLoading = true
                                                webHasError = false
                                                currentWebUrl = url ?: ""
                                            }

                                            override fun onPageFinished(view: WebView?, url: String?) {
                                                isWebLoading = false
                                                currentWebUrl = url ?: ""
                                            }

                                            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                                                super.onReceivedError(view, request, error)
                                                webHasError = true
                                                isWebLoading = false
                                            }
                                        }
                                        webChromeClient = object : WebChromeClient() {
                                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                                webLoadingProgress = newProgress
                                            }
                                        }
                                        loadUrl(currentWebUrl)
                                        webViewInstance = this
                                    }
                                },
                                update = { webView ->
                                    if (webView.url != currentWebUrl && currentWebUrl.isNotBlank()) {
                                        webView.loadUrl(currentWebUrl)
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )

                            if (webHasError) {
                                Card(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Page difficile à charger. Vérifiez votre connexion.", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Educational Illustrations Directory
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Info banner
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = OrangePrimary.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(OrangePrimary.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Collections,
                                        contentDescription = null,
                                        tint = OrangePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Support Visuel & Schémas d'Appui",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Consultez les fiches de schémas ou explorez les figures en direct sur le web pour mieux mémoriser !",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Subject Filter Chips
                        val filterCategories = listOf(
                            "all" to "Tous les schémas",
                            "svt" to "🧬 SVT",
                            "pc" to "⚡ Physique-Chimie",
                            "math" to "📐 Maths",
                            "hg" to "🌍 Hist-Géo",
                            "philo" to "🏛️ Philosophie"
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 12.dp)
                        ) {
                            items(filterCategories) { (id, label) ->
                                val isSelected = selectedFilterSubject == id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedFilterSubject = id },
                                    label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = OrangePrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        // List of Illustrations Cards
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(filteredList, key = { it.id }) { ill ->
                                IllustrationCard(
                                    illustration = ill,
                                    onOpenWebView = { query ->
                                        val encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
                                        currentWebUrl = "https://www.google.com/search?tbm=isch&q=$encoded"
                                        searchQuery = query
                                        showInAppWebView = true
                                    },
                                    onOpenArticle = { url ->
                                        currentWebUrl = url
                                        searchQuery = ill.title
                                        showInAppWebView = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IllustrationCard(
    illustration: EducationalIllustration,
    onOpenWebView: (String) -> Unit,
    onOpenArticle: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = when (illustration.subjectId) {
                        "svt" -> GreenSuccess.copy(alpha = 0.15f)
                        "pc" -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                        "math" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else -> OrangePrimary.copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = illustration.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (illustration.subjectId) {
                            "svt" -> GreenSuccess
                            "pc" -> MaterialTheme.colorScheme.error
                            "math" -> MaterialTheme.colorScheme.primary
                            else -> OrangePrimary
                        }
                    )
                }

                Text(
                    text = "Programme CI",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = illustration.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = illustration.description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Points clés de la figure
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Éléments et annotations à retenir pour l'épreuve :",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    illustration.keyPoints.forEach { point ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("• ", fontSize = 12.sp, color = OrangePrimary, fontWeight = FontWeight.Bold)
                            Text(point, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = { onOpenWebView(illustration.defaultWebQuery) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ImageSearch, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Voir Schémas Web", fontSize = 12.sp, maxLines = 1)
                }

                OutlinedButton(
                    onClick = { onOpenArticle(illustration.webFallbackUrl) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Article", fontSize = 12.sp)
                }
            }
        }
    }
}
