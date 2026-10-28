package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ui.AppScreen
import com.example.ui.DeliveryViewModel
import com.example.ui.components.AppUtils
import com.example.ui.theme.CardBackground
import com.example.ui.theme.ExpressOrange
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenLight
import com.example.ui.theme.TextMuted
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanCheckScreen(
    viewModel: DeliveryViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.clearScan()
        viewModel.navigateTo(AppScreen.Home)
    }

    val context = LocalContext.current
    val isMatching by viewModel.isMatching.collectAsStateWithLifecycle()
    val matchEvaluation by viewModel.matchEvaluation.collectAsStateWithLifecycle()
    val scanBitmap by viewModel.currentScanBitmap.collectAsStateWithLifecycle()

    var autoRemoveEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(matchEvaluation, autoRemoveEnabled) {
        val eval = matchEvaluation
        if (autoRemoveEnabled && eval != null && eval.isMatch) {
            viewModel.markAsDelivered(eval.parcel.id) {
                viewModel.navigateTo(AppScreen.Home)
            }
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            viewModel.checkParcelByImage(bitmap)
        }
    }

    val pickPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bitmap = viewModel.loadBitmapFromUri(uri)
            if (bitmap != null) {
                viewModel.checkParcelByImage(bitmap)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "پشکنینی کاڵا بە وێنە",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearScan()
                        viewModel.navigateTo(AppScreen.Home)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "گەڕانەوە",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyDark)
            )
        },
        modifier = modifier.testTag("scan_check_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Auto-Remove Toggle Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (autoRemoveEnabled) SuccessGreenLight else Color(0xFFF1F5F9)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (autoRemoveEnabled) SuccessGreen else NavyPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "لابردنی دەستبەجێ کاتی دۆزینەوە (Auto-Remove)",
                                fontWeight = FontWeight.Bold,
                                color = NavyDark,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (autoRemoveEnabled)
                                    "بە دۆزینەوەی وێنەکە دەستبەجێ لە سیستەم لادەبرێت و دەگاتە ئەرشیف"
                                else
                                    "دوای دۆزینەوە، زانیارییەکان پێشان دەدات بۆ پەسەندکردن",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Switch(
                        checked = autoRemoveEnabled,
                        onCheckedChange = { autoRemoveEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SuccessGreen
                        )
                    )
                }
            }

            // Camera / Photo Action Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (scanBitmap == null) {
                        // Viewfinder placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(2.dp, ExpressOrange.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .background(Color(0xFFF8FAFC)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(ExpressOrange.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DocumentScanner,
                                        contentDescription = null,
                                        tint = ExpressOrange,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Text(
                                    text = "وێنەی لەزگەی پاکێجەکە بگرە",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyDark
                                )
                                Text(
                                    text = "ئەپڵیکەیشنەکە بە وێنە بەراوردی دەکات لەگەڵ کاڵاکان",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    } else {
                        // Display the captured query bitmap
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(2.dp, NavyPrimary, RoundedCornerShape(12.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = scanBitmap!!.asImageBitmap(),
                                contentDescription = "وێنەی پشکنراو",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )

                            if (isMatching) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.65f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        CircularProgressIndicator(color = ExpressOrange)
                                        Text(
                                            text = "شیکردنەوە و بەراوردکردنی وێنە...",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Photo source buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.clearScan()
                                takePictureLauncher.launch()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("take_photo_scan_button")
                        ) {
                            Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("گرتنی وێنە")
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.clearScan()
                                pickPhotoLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("لە گەلەری")
                        }
                    }

                    // Test with Sample Parcel button
                    Button(
                        onClick = { viewModel.testWithSampleParcel() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFEF3C7),
                            contentColor = Color(0xFFB45309)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("test_sample_parcel_button")
                    ) {
                        Icon(imageVector = Icons.Default.Science, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "🧪 تاقیکردنەوە بە پاکێجی نموونە (Postyar)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Results Section
            if (!isMatching && matchEvaluation != null) {
                val eval = matchEvaluation!!
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically()
                ) {
                    if (eval.isMatch) {
                        // MATCH FOUND!
                        val parcel = eval.parcel
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, SuccessGreen, RoundedCornerShape(16.dp))
                                .testTag("match_found_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Match Banner
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = SuccessGreen,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = "کاڵاکە دۆزرایەوە لەناو سیستەم!",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = SuccessGreen
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SuccessGreenLight)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "هاوشێوەیی: %${eval.confidencePercent}",
                                            color = SuccessGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                // Prominent Price Highlight
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(ExpressOrange)
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "نرخی وەرگیراو بە گەیاندنەوە",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                        Text(
                                            text = AppUtils.formatIqdPrice(parcel.price),
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                }

                                // Parcel Details Box
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF8FAFC))
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (parcel.deliveryAddress.isNotBlank()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = ExpressOrange,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "ناونیشان: ${parcel.deliveryAddress}",
                                                fontWeight = FontWeight.Bold,
                                                color = NavyDark
                                            )
                                        }
                                    }

                                    if (parcel.recipientPhone.isNotBlank()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Call,
                                                    contentDescription = null,
                                                    tint = NavyPrimary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = "ژمارەی کڕیار: ${parcel.recipientPhone}",
                                                    fontWeight = FontWeight.Medium,
                                                    color = NavyPrimary
                                                )
                                            }

                                            Button(
                                                onClick = {
                                                    AppUtils.dialPhoneNumber(context, parcel.recipientPhone)
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = NavyPrimary
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Call,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("پەیوەندی", fontSize = 12.sp)
                                            }
                                        }
                                    }

                                    if (parcel.recipientName.isNotBlank()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = TextMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "ناوی کڕیار: ${parcel.recipientName}",
                                                color = TextMuted,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }

                                    if (parcel.notes.isNotBlank()) {
                                        Text(
                                            text = "تێبینی لەزگە: ${parcel.notes}",
                                            color = TextMuted,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                // Registered Photo Preview
                                val savedFile = File(parcel.photoPath)
                                if (savedFile.exists()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "وێنەی تۆمارکراوی پێشوەختە:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextMuted
                                        )
                                        AsyncImage(
                                            model = savedFile,
                                            contentDescription = "وێنەی تۆمارکراو",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(130.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                        )
                                    }
                                }

                                // Crucial User Action: "گەیەندرا و لایدە لە سیستەم"
                                Button(
                                    onClick = {
                                        viewModel.markAsDelivered(parcel.id) {
                                            viewModel.navigateTo(AppScreen.Home)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp)
                                        .testTag("mark_delivered_and_remove_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "✅ گەیەندرا - ئێستا لە سیستەم لایدە",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    } else {
                        // NO MATCH FOUND
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, Color(0xFFEF4444), RoundedCornerShape(16.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(42.dp)
                                )
                                Text(
                                    text = "ئەم کاڵایە لە لیستی چاوەڕوانکراودا نییە",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEF4444),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "ڕەنگە ئەم کاڵایە پێشتر گەیاندرابێت و لە سیستەم لابرا بێت، یان وێنەکە ناڕوون بێت.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                    textAlign = TextAlign.Center
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.clearScan()
                                            takePictureLauncher.launch()
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("دووبارە گرتنەوە")
                                    }

                                    Button(
                                        onClick = { viewModel.navigateTo(AppScreen.AddParcel) },
                                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("تۆمارکردن")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
