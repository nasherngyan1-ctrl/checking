package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddParcelScreen(
    viewModel: DeliveryViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.clearExtractedData()
        viewModel.navigateTo(AppScreen.Home)
    }

    val context = LocalContext.current
    val isExtracting by viewModel.isExtracting.collectAsStateWithLifecycle()

    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var priceText by remember { mutableStateOf("") }
    var recipientName by remember { mutableStateOf("") }
    var recipientPhone by remember { mutableStateOf("") }
    var deliveryAddress by remember { mutableStateOf("") }
    var sellerName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var extractionFinished by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun processBitmapWithOcr(bitmap: Bitmap) {
        selectedBitmap = bitmap
        errorMessage = null

        viewModel.extractInfoFromBitmap(bitmap) { extracted ->
            if (extracted.price > 0L) {
                priceText = extracted.price.toString()
            }
            if (extracted.recipientPhone.isNotEmpty()) {
                recipientPhone = extracted.recipientPhone
            }
            if (extracted.deliveryAddress.isNotEmpty()) {
                deliveryAddress = extracted.deliveryAddress
            }
            if (extracted.sellerName.isNotEmpty()) {
                sellerName = extracted.sellerName
            }
            if (extracted.notes.isNotEmpty()) {
                notes = extracted.notes
            }
            extractionFinished = true
        }
    }

    val pickPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bitmap = viewModel.loadBitmapFromUri(uri)
            if (bitmap != null) {
                processBitmapWithOcr(bitmap)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "تۆمارکردنی کاڵا بە وێنە",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearExtractedData()
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
        modifier = modifier.testTag("add_parcel_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Camera / Image Card
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
                    if (selectedBitmap == null) {
                        // Live Camera to snap photo of package
                        LiveCameraView(
                            onPhotoCaptured = { bitmap ->
                                processBitmapWithOcr(bitmap)
                            }
                        )

                        // Choose from gallery or test sample
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
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
                                onClick = {
                                    try {
                                        val stream = context.assets.open("sample_parcel.jpg")
                                        val bmp = BitmapFactory.decodeStream(stream)
                                        stream.close()
                                        if (bmp != null) {
                                            processBitmapWithOcr(bmp)
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
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
                                Text("دانانی وێنەی نموونە", fontSize = 11.sp)
                            }
                        }
                    } else {
                        // Display snapped photo
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(2.dp, NavyPrimary, RoundedCornerShape(12.dp))
                        ) {
                            Image(
                                bitmap = selectedBitmap!!.asImageBitmap(),
                                contentDescription = "وێنەی هەڵبژێردراو",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            if (isExtracting) {
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
                                            text = "خوێندنەوەی لەزگەی پاکێجەکە...",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Re-take photo button
                        OutlinedButton(
                            onClick = {
                                selectedBitmap = null
                                extractionFinished = false
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("وێنەیەکی تر بگرەوە")
                        }
                    }
                }
            }

            // Information Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "زانیاری کاڵا بۆ پاشەکەوتکردن",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )

                    // 1. Price
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() }) priceText = input
                            },
                            label = { Text("نرخ بە دینار (IQD) *") },
                            placeholder = { Text("نرخ دیاری بکە") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("price_input_field")
                        )

                        // Quick Price Chips
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(5000L, 10000L, 15000L, 25000L, 35000L, 45000L, 47000L, 50000L).forEach { amount ->
                                FilterChip(
                                    selected = priceText == amount.toString(),
                                    onClick = { priceText = amount.toString() },
                                    label = { Text(AppUtils.formatIqdPrice(amount), fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    // 2. Delivery Address
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = deliveryAddress,
                            onValueChange = { deliveryAddress = it },
                            label = { Text("ناونیشانی گەیاندن *") },
                            placeholder = { Text("شار یان گەڕەک دیاری بکە") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = ExpressOrange)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("address_input_field")
                        )

                        // Quick City Chips
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("هەولێر", "سلێمانی", "دهۆک", "کەرکووک", "زاخۆ", "سۆران", "ڕانیە", "هەڵەبجە", "مام زاوا").forEach { city ->
                                FilterChip(
                                    selected = deliveryAddress.contains(city),
                                    onClick = {
                                        deliveryAddress = if (deliveryAddress.isBlank()) city else "$city / $deliveryAddress"
                                    },
                                    label = { Text(city, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    // 3. Customer Phone
                    OutlinedTextField(
                        value = recipientPhone,
                        onValueChange = { recipientPhone = it },
                        label = { Text("ژمارەی مۆبایلی کڕیار *") },
                        placeholder = { Text("0750xxxxxxx") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = NavyPrimary)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("phone_input_field")
                    )

                    // 4. Notes / Seller
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("تێبینی یان فرۆشیار (ئارەزوومەندانە)") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Notes, contentDescription = null)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Error Message
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Color(0xFFEF4444),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            // Save Button
            Button(
                onClick = {
                    val price = priceText.toLongOrNull()
                    when {
                        selectedBitmap == null -> {
                            errorMessage = "تکایە سەرەتا وێنەی لەزگەی پاکێجەکە بگرە"
                        }
                        price == null || price <= 0 -> {
                            errorMessage = "تکایە نرخ دیاری بکە"
                        }
                        deliveryAddress.isBlank() -> {
                            errorMessage = "تکایە ناونیشانی گەیاندن دیاری بکە"
                        }
                        recipientPhone.isBlank() -> {
                            errorMessage = "تکایە ژمارەی تەلەفۆنی کڕیار بنووسە"
                        }
                        else -> {
                            viewModel.saveParcel(
                                bitmap = selectedBitmap!!,
                                price = price,
                                recipientName = recipientName.ifBlank { "کڕیار" },
                                recipientPhone = recipientPhone,
                                deliveryAddress = deliveryAddress,
                                sellerName = sellerName,
                                sellerPhone = "",
                                notes = notes,
                                onSuccess = {
                                    viewModel.clearExtractedData()
                                    viewModel.navigateTo(AppScreen.Home)
                                }
                            )
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ExpressOrange),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_save_parcel_button")
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "💾 پاشەکەوتکردن لەناو سیستەم",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
