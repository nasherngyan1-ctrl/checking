package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.ParcelEntity
import com.example.ui.AppScreen
import com.example.ui.DeliveryViewModel
import com.example.ui.components.AppUtils
import com.example.ui.theme.CardBackground
import com.example.ui.theme.ExpressOrange
import com.example.ui.theme.ExpressOrangeContainer
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenLight
import com.example.ui.theme.TextMuted
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: DeliveryViewModel,
    modifier: Modifier = Modifier
) {
    val pendingParcels by viewModel.pendingParcels.collectAsStateWithLifecycle()
    val deliveredParcels by viewModel.deliveredParcels.collectAsStateWithLifecycle()
    val totalPendingPrice by viewModel.totalPendingPrice.collectAsStateWithLifecycle()
    val totalDeliveredPrice by viewModel.totalDeliveredPrice.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ExpressOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "پشکنەری بەرید",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "ناسینەوە و چێکی پاکێج بە وێنە",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavyDark
                )
            )
        },
        modifier = modifier.testTag("home_screen")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Metrics Banner
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "ماوە",
                        value = "${pendingParcels.size} کاڵا",
                        subValue = AppUtils.formatIqdPrice(totalPendingPrice),
                        containerColor = ExpressOrangeContainer,
                        accentColor = ExpressOrange,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "گەیەندراو",
                        value = "${deliveredParcels.size} کاڵا",
                        subValue = AppUtils.formatIqdPrice(totalDeliveredPrice),
                        containerColor = SuccessGreenLight,
                        accentColor = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Primary Action Buttons
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Big Visual Scanner Button
                    Card(
                        onClick = { viewModel.navigateTo(AppScreen.ScanChecker) },
                        colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start_scan_button")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(ExpressOrange),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "کامێرا",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "📸 پشکنینی کاڵا بە وێنە",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "وێنەی لەزگەی پاکێجەکە بگرە بۆ چێککردن و دۆزینەوەی نرخ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 12.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.DocumentScanner,
                                contentDescription = null,
                                tint = ExpressOrangeLight,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // Quick Auto-Register by Photo Button
                    Button(
                        onClick = { viewModel.navigateTo(AppScreen.AddParcel) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("add_parcel_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ExpressOrange)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "⚡ تۆمارکردنی خێرا بە وێنە (خۆکارانە نرخ دەردەهێنێت)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("گەڕان بەپێی ناونیشان، مۆبایل، یان نرخ...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "گەڕان")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "پاککردنەوە")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_field")
                )
            }

            // Tab Selector: Pending vs Delivered
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE2E8F0))
                        .padding(4.dp)
                ) {
                    TabPill(
                        label = "چاوەڕوانکراوەکان (${pendingParcels.size})",
                        isSelected = selectedTab == 0,
                        onClick = { viewModel.setSelectedTab(0) },
                        modifier = Modifier.weight(1f)
                    )
                    TabPill(
                        label = "گەیەندراوەکان (${deliveredParcels.size})",
                        isSelected = selectedTab == 1,
                        onClick = { viewModel.setSelectedTab(1) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Parcel Items List
            val currentList = if (selectedTab == 0) pendingParcels else deliveredParcels

            if (currentList.isEmpty()) {
                item {
                    EmptyParcelsState(
                        isDeliveredTab = selectedTab == 1,
                        searchQuery = searchQuery,
                        onAddClick = { viewModel.navigateTo(AppScreen.AddParcel) },
                        onScanClick = { viewModel.navigateTo(AppScreen.ScanChecker) }
                    )
                }
            } else {
                items(currentList, key = { it.id }) { parcel ->
                    ParcelCard(
                        parcel = parcel,
                        onMarkDelivered = { viewModel.markAsDelivered(parcel.id) },
                        onMarkPending = { viewModel.markAsPending(parcel.id) },
                        onDelete = { viewModel.deleteParcel(parcel) },
                        onCall = { AppUtils.dialPhoneNumber(context, parcel.recipientPhone) },
                        onCheckWithThis = {
                            val bmp = viewModel.loadBitmapFromUri(Uri.fromFile(File(parcel.photoPath)))
                            if (bmp != null) {
                                viewModel.checkParcelByImage(bmp)
                                viewModel.navigateTo(AppScreen.ScanChecker)
                            }
                        }
                    )
                }
            }
        }
    }
}

val ExpressOrangeLight = Color(0xFFFB923C)

@Composable
fun TabPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) NavyPrimary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else NavyDark,
            fontSize = 13.sp
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subValue: String,
    containerColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = accentColor,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = NavyDark
            )
            Text(
                text = subValue,
                style = MaterialTheme.typography.bodySmall,
                color = NavyDark.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun ParcelCard(
    parcel: ParcelEntity,
    onMarkDelivered: () -> Unit,
    onMarkPending: () -> Unit,
    onDelete: () -> Unit,
    onCall: () -> Unit,
    onCheckWithThis: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("parcel_card_${parcel.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Price & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Price Tag Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ExpressOrange)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = AppUtils.formatIqdPrice(parcel.price),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                // Status Tag
                if (parcel.isDelivered) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SuccessGreenLight)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "گەیەندراوە",
                                color = SuccessGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFEF3C7))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "چاوەڕوانە",
                            color = Color(0xFFB45309),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Main Content: Image and Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Parcel photo thumbnail
                val imageFile = File(parcel.photoPath)
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageFile.exists()) {
                        AsyncImage(
                            model = imageFile,
                            contentDescription = "وێنەی پاکێج",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Details Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (parcel.deliveryAddress.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = ExpressOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = parcel.deliveryAddress,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = NavyDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (parcel.recipientPhone.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                tint = NavyPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = parcel.recipientPhone,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = NavyPrimary
                            )
                        }
                    }

                    if (parcel.recipientName.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = parcel.recipientName,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                maxLines = 1
                            )
                        }
                    }

                    if (parcel.notes.isNotBlank()) {
                        Text(
                            text = "تێبینی: ${parcel.notes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!parcel.isDelivered) {
                    // Quick Call
                    if (parcel.recipientPhone.isNotBlank()) {
                        IconButton(
                            onClick = onCall,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEFF6FF))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "تەلەفۆن",
                                tint = NavyPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Mark as delivered / remove from pending
                    Button(
                        onClick = onMarkDelivered,
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "گەیاندن (لابردن)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    // Return to pending
                    OutlinedButton(
                        onClick = onMarkPending,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("گەڕاندنەوە بۆ چاوەڕوان", fontSize = 12.sp)
                    }
                }

                // Delete Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "سڕینەوە",
                        tint = Color(0xFFEF4444)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyParcelsState(
    isDeliveredTab: Boolean,
    searchQuery: String,
    onAddClick: () -> Unit,
    onScanClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isDeliveredTab) Icons.Default.History else Icons.Default.LocalShipping,
                    contentDescription = null,
                    tint = NavyPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = when {
                    searchQuery.isNotBlank() -> "هیچ ئەنجامێک نەدۆزرایەوە بۆ: $searchQuery"
                    isDeliveredTab -> "هێشتا هیچ کاڵایەک نەگەیەندراوە"
                    else -> "هیچ کاڵایەکی چاوەڕوانکراو نییە"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = NavyDark
            )

            Text(
                text = if (!isDeliveredTab) {
                    "وێنەی پاکێجەکان پێش وەختە تۆمار بکە، یاخود پشکنینی ڕاستەوخۆ بکە."
                } else {
                    "کاتێک پاکێجێک بە وێنە چێک دەکەیت و دەیگەیەنیت، لێرە ئەرشیف دەکرێت."
                },
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                textAlign = TextAlign.Center,
                fontSize = 12.sp
            )

            if (!isDeliveredTab && searchQuery.isBlank()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Button(
                        onClick = onScanClick,
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Text("📸 پشکنینی وێنە")
                    }
                    OutlinedButton(onClick = onAddClick) {
                        Text("➕ زیادکردنی کاڵا")
                    }
                }
            }
        }
    }
}
