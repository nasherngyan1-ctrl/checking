package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.components.LiveCameraView
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
            // Live Camera or Captured Photo Box
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (scanBitmap == null) {
                        // Live in-app CameraX feed with viewfinder
                        LiveCameraView(
                            onPhotoCaptured = { bitmap ->
                                viewModel.checkParcelByImage(bitmap)
                            }
                        )

                        // Secondary options
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
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
                                    .height(44.dp)
                            ) {
                                Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("لە گەلەری")
                            }

                            Button(
                                onClick = { viewModel.testWithSampleParcel() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFEF3C7),
                                    contentColor = Color(0xFFB45309)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(44.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Science, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تاقیکردنەوە بە نموونە", fontSize = 11.sp)
                            }
                        }
                    } else {
                        // Showing Captured Photo Preview
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
                                contentDescription = "وێنەی گیراو",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )

                            if (isMatching) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.7f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        CircularProgressIndicator(color = ExpressOrange)
                                        Text(
                                            text = "پشکنینی ورد و بەراوردکردن...",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Re-take / Clear button
                        Button(
                            onClick = { viewModel.clearScan() },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("📸 کردنەوەی کامێرا بۆ وێنەیەکی تر")
                        }
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
                        // MATCH FOUND
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
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Match Status Header
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
                                            text = "ئەم کاڵایە دۆزرایەوە!",
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
                                            text = "دڵنیایی: %${eval.confidencePercent}",
                                            color = SuccessGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                if (eval.matchReason.isNotBlank()) {
                                    Text(
                                        text = "هۆکاری هاوتایی: ${eval.matchReason}",
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                }

                                // Photo Comparison (Side by side)
                                val savedFile = File(parcel.photoPath)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    if (scanBitmap != null) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "وێنەی گیراو ئێستا",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextMuted,
                                                fontSize = 11.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Image(
                                                bitmap = scanBitmap!!.asImageBitmap(),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(110.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                            )
                                        }
                                    }

                                    if (savedFile.exists()) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "وێنەی سەرەکی تۆمارکراو",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextMuted,
                                                fontSize = 11.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            AsyncImage(
                                                model = savedFile,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(110.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                            )
                                        }
                                    }
                                }

                                // Big Price Banner
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(ExpressOrange)
                                        .padding(14.dp),
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

                                // Parcel Details
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF8FAFC))
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (parcel.deliveryAddress.isNotBlank()) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = ExpressOrange,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
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
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Call,
                                                    contentDescription = null,
                                                    tint = NavyPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "مۆبایل: ${parcel.recipientPhone}",
                                                    fontWeight = FontWeight.Medium,
                                                    color = NavyPrimary
                                                )
                                            }

                                            Button(
                                                onClick = { AppUtils.dialPhoneNumber(context, parcel.recipientPhone) },
                                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("تەلەفۆن", fontSize = 11.sp)
                                            }
                                        }
                                    }

                                    if (parcel.notes.isNotBlank()) {
                                        Text(
                                            text = "تێبینی: ${parcel.notes}",
                                            color = TextMuted,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                // Safety Prompt: Never delete without explicit click!
                                Text(
                                    text = "ئایا دڵنیایت ئەم پاکێجەت گەیاندووە؟",
                                    fontWeight = FontWeight.Bold,
                                    color = NavyDark,
                                    fontSize = 14.sp
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // NO - Return without deleting
                                    OutlinedButton(
                                        onClick = { viewModel.clearScan() },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(50.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("نەخێر، ئەمە نییە")
                                    }

                                    // YES - Confirm Delivery & Remove
                                    Button(
                                        onClick = {
                                            viewModel.markAsDelivered(parcel.id) {
                                                viewModel.navigateTo(AppScreen.Home)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .height(50.dp)
                                            .testTag("mark_delivered_and_remove_button")
                                    ) {
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "بەڵێ، گەیەندراوە (لابردن)",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
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
                                    text = "هیچ کاڵایەکی هاوشێوە نەدۆزرایەوە",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEF4444),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "ئەم کاڵایە لە لیستی چاوەڕوانکراودا نییە یان پێشتر گەیاندرابێت. بۆیە هیچ کاڵایەک لە سیستەم لانەدرا.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                    textAlign = TextAlign.Center
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.clearScan() },
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

                    // Manual candidate selection list (Allows courier to manually select the right parcel if OCR missed it)
                    if (eval.allPendingCandidates.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "📦 کاڵاکانی تری چاوەڕوانکراو (دەتوانیت کاڵای دروست دەستنیشان بکەیت):",
                                    fontWeight = FontWeight.Bold,
                                    color = NavyDark,
                                    fontSize = 13.sp
                                )

                                eval.allPendingCandidates.forEach { candidate ->
                                    val isCurrent = eval.isMatch && eval.parcel.id == candidate.id
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isCurrent) SuccessGreenLight else Color(0xFFF8FAFC)
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        border = if (isCurrent) BorderStroke(1.5.dp, SuccessGreen) else null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.manuallySelectCandidate(candidate)
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                val cFile = File(candidate.photoPath)
                                                if (cFile.exists()) {
                                                    AsyncImage(
                                                        model = cFile,
                                                        contentDescription = null,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier
                                                            .size(46.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                    )
                                                }

                                                Column {
                                                    Text(
                                                        text = AppUtils.formatIqdPrice(candidate.price),
                                                        fontWeight = FontWeight.Bold,
                                                        color = ExpressOrange,
                                                        fontSize = 14.sp
                                                    )
                                                    if (candidate.deliveryAddress.isNotBlank()) {
                                                        Text(
                                                            text = candidate.deliveryAddress,
                                                            color = NavyDark,
                                                            fontSize = 12.sp,
                                                            maxLines = 1
                                                        )
                                                    }
                                                    if (candidate.recipientPhone.isNotBlank()) {
                                                        Text(
                                                            text = candidate.recipientPhone,
                                                            color = TextMuted,
                                                            fontSize = 11.sp
                                                        )
                                                    }
                                                }
                                            }

                                            if (isCurrent) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = SuccessGreen,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            } else {
                                                OutlinedButton(
                                                    onClick = { viewModel.manuallySelectCandidate(candidate) },
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text("هەڵبژاردن", fontSize = 11.sp)
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
        }
    }
}
