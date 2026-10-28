package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.ui.theme.CardBackground
import com.example.ui.theme.ExpressOrange
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenLight
import com.example.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
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
    var autoSaveEnabled by remember { mutableStateOf(false) }
    var autoExtractedSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun processBitmapWithAutoExtraction(bitmap: Bitmap) {
        selectedBitmap = bitmap
        errorMessage = null

        if (autoSaveEnabled) {
            viewModel.autoSaveFromBitmap(bitmap) {
                viewModel.navigateTo(AppScreen.Home)
            }
        } else {
            viewModel.extractInfoFromBitmap(bitmap) { extracted ->
                priceText = extracted.price.toString()
                recipientPhone = extracted.recipientPhone
                deliveryAddress = extracted.deliveryAddress
                recipientName = extracted.recipientName
                sellerName = extracted.sellerName
                notes = extracted.notes
                autoExtractedSuccess = true
            }
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            processBitmapWithAutoExtraction(bitmap)
        }
    }

    val pickPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bitmap = viewModel.loadBitmapFromUri(uri)
            if (bitmap != null) {
                processBitmapWithAutoExtraction(bitmap)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "تۆمارکردن بە وێنە (خۆکار)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ExpressOrange)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "AI",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
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
            // Auto-Save Toggle Banner
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (autoSaveEnabled) SuccessGreenLight else Color(0xFFF1F5F9)
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
                            tint = if (autoSaveEnabled) SuccessGreen else NavyPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "تۆمارکردنی یەکسەر (خۆکار)",
                                fontWeight = FontWeight.Bold,
                                color = NavyDark,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (autoSaveEnabled)
                                    "بە گرتنی وێنەکە دەستبەجێ زانیارییەکان دەردەهێنرێت و پاشەکەوت دەبێت"
                                else
                                    "زانیارییەکان دەردەهێنرێت و پێش پاشەکەوتکردن دەیبینیت",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Switch(
                        checked = autoSaveEnabled,
                        onCheckedChange = { autoSaveEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SuccessGreen
                        )
                    )
                }
            }

            // Photo Selection Section
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (selectedBitmap == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(2.dp, ExpressOrange.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .background(Color(0xFFF8FAFC))
                                .clickable { takePictureLauncher.launch() },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(ExpressOrange),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                                Text(
                                    text = "📸 وێنەی لەزگەی پاکێجەکە بگرە",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = NavyDark
                                )
                                Text(
                                    text = "خۆکارانە نرخ و مۆبایل و ناونیشان دەردەهێنێت بەبێ دەستلێدان!",
                                    fontSize = 12.sp,
                                    color = ExpressOrange,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
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
                                            text = "⚡ دەرهێنانی خۆکارانەی نرخ و زانیاری لە وێنەکە...",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Photo Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { takePictureLauncher.launch() },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("گرتنی وێنە")
                        }

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
                                .height(48.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("لە گەلەری")
                        }
                    }

                    // Test with sample Postyar photo
                    Button(
                        onClick = {
                            try {
                                val stream = context.assets.open("sample_parcel.jpg")
                                val bmp = BitmapFactory.decodeStream(stream)
                                stream.close()
                                if (bmp != null) {
                                    processBitmapWithAutoExtraction(bmp)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFEF3C7),
                            contentColor = Color(0xFFB45309)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Science, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "🧪 تاقیکردنەوەی دەرهێنانی ئۆتۆماتیک بە وێنەی نموونە",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Auto-extracted success banner
            AnimatedVisibility(visible = autoExtractedSuccess) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SuccessGreenLight)
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "✅ زانیارییەکان (نرخ، ناونیشان، مۆبایل) بە سەرکەوتوویی لە وێنەکە دەرهێنران!",
                            color = SuccessGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Extracted Fields (Pre-filled automatically, can be edited if needed)
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "زانیارییە دەرهێنراوەکانی کاڵا",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NavyDark
                        )
                        if (autoExtractedSuccess) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SuccessGreen)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "دەرهێنراو لە وێنە",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Price Field
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() }) priceText = input
                            },
                            label = { Text("نرخ بە گەیاندنەوە (دینار IQD) *") },
                            placeholder = { Text("خۆکارانە لە وێنە دەردەهێنرێت") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("price_input_field")
                        )

                        // Quick Price Chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(10000L, 25000L, 35000L, 47000L).forEach { amount ->
                                FilterChip(
                                    selected = priceText == amount.toString(),
                                    onClick = { priceText = amount.toString() },
                                    label = { Text(AppUtils.formatIqdPrice(amount), fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    // Delivery Address
                    OutlinedTextField(
                        value = deliveryAddress,
                        onValueChange = { deliveryAddress = it },
                        label = { Text("ناونیشانی گەیاندن *") },
                        placeholder = { Text("بۆ نموونە: هەولێر / مام زاوا") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = ExpressOrange)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("address_input_field")
                    )

                    // Customer Phone
                    OutlinedTextField(
                        value = recipientPhone,
                        onValueChange = { recipientPhone = it },
                        label = { Text("ژمارەی مۆبایلی کڕیار *") },
                        placeholder = { Text("بۆ نموونە: 07507496271") },
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

                    // Notes / Seller Name
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("تێبینی یان فرۆشیار") },
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

            // Manual / Confirm Save Button
            Button(
                onClick = {
                    val price = priceText.toLongOrNull()
                    when {
                        selectedBitmap == null -> {
                            errorMessage = "تکایە سەرەتا وێنەی لەزگەی پاکێجەکە بگرە"
                        }
                        price == null || price <= 0 -> {
                            errorMessage = "تکایە نرخ بە دروستی بنووسە"
                        }
                        deliveryAddress.isBlank() -> {
                            errorMessage = "تکایە ناونیشانی گەیاندن بنووسە"
                        }
                        recipientPhone.isBlank() -> {
                            errorMessage = "تکایە ژمارەی تەلەفۆنی کڕیار بنووسە"
                        }
                        else -> {
                            viewModel.saveParcel(
                                bitmap = selectedBitmap!!,
                                price = price,
                                recipientName = recipientName,
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
