package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ParcelEntity
import com.example.data.repository.ParcelRepository
import com.example.util.AutoParcelExtractor
import com.example.util.ExtractedParcelData
import com.example.util.MatchEvaluation
import com.example.util.VisualMatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class AppScreen {
    data object Home : AppScreen()
    data object AddParcel : AppScreen()
    data object ScanChecker : AppScreen()
}

class DeliveryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ParcelRepository

    init {
        val dao = AppDatabase.getDatabase(application).parcelDao()
        repository = ParcelRepository(application, dao)
        // Clean start - no auto-seeding of fake packages into user database
    }

    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Home)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0: Pending, 1: Delivered
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    val pendingParcels: StateFlow<List<ParcelEntity>> = combine(
        repository.pendingParcels,
        _searchQuery
    ) { list, query ->
        if (query.isBlank()) list
        else list.filter { parcelMatchesQuery(it, query) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deliveredParcels: StateFlow<List<ParcelEntity>> = combine(
        repository.deliveredParcels,
        _searchQuery
    ) { list, query ->
        if (query.isBlank()) list
        else list.filter { parcelMatchesQuery(it, query) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalPendingPrice: StateFlow<Long> = repository.totalPendingPrice
        .combine(MutableStateFlow(0L)) { price, _ -> price ?: 0L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalDeliveredPrice: StateFlow<Long> = repository.totalDeliveredPrice
        .combine(MutableStateFlow(0L)) { price, _ -> price ?: 0L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    // Visual Matcher Scan State
    private val _isMatching = MutableStateFlow(false)
    val isMatching: StateFlow<Boolean> = _isMatching.asStateFlow()

    private val _matchEvaluation = MutableStateFlow<MatchEvaluation?>(null)
    val matchEvaluation: StateFlow<MatchEvaluation?> = _matchEvaluation.asStateFlow()

    private val _currentScanBitmap = MutableStateFlow<Bitmap?>(null)
    val currentScanBitmap: StateFlow<Bitmap?> = _currentScanBitmap.asStateFlow()

    // Auto-Extraction State
    private val _isExtracting = MutableStateFlow(false)
    val isExtracting: StateFlow<Boolean> = _isExtracting.asStateFlow()

    private val _extractedData = MutableStateFlow<ExtractedParcelData?>(null)
    val extractedData: StateFlow<ExtractedParcelData?> = _extractedData.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedTab(index: Int) {
        _selectedTab.value = index
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun extractInfoFromBitmap(bitmap: Bitmap, onComplete: ((ExtractedParcelData) -> Unit)? = null) {
        _isExtracting.value = true
        viewModelScope.launch {
            val data = withContext(Dispatchers.Default) {
                AutoParcelExtractor.extractFromBitmap(bitmap)
            }
            _extractedData.value = data
            _isExtracting.value = false
            onComplete?.invoke(data)
        }
    }

    fun autoSaveFromBitmap(
        bitmap: Bitmap,
        onSuccess: () -> Unit
    ) {
        _isExtracting.value = true
        viewModelScope.launch {
            val data = withContext(Dispatchers.Default) {
                AutoParcelExtractor.extractFromBitmap(bitmap)
            }
            _extractedData.value = data
            _isExtracting.value = false

            repository.insertParcel(
                bitmap = bitmap,
                price = data.price,
                recipientName = data.recipientName,
                recipientPhone = data.recipientPhone,
                deliveryAddress = data.deliveryAddress,
                sellerName = data.sellerName,
                sellerPhone = "",
                notes = data.notes
            )
            _userMessage.value = "خۆکارانە زانیارییەکان دەرهێنران و کاڵاکە پاشەکەوت کرا!"
            onSuccess()
        }
    }

    fun clearExtractedData() {
        _extractedData.value = null
        _isExtracting.value = false
    }

    fun saveParcel(
        bitmap: Bitmap,
        price: Long,
        recipientName: String,
        recipientPhone: String,
        deliveryAddress: String,
        sellerName: String,
        sellerPhone: String,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.insertParcel(
                bitmap = bitmap,
                price = price,
                recipientName = recipientName,
                recipientPhone = recipientPhone,
                deliveryAddress = deliveryAddress,
                sellerName = sellerName,
                sellerPhone = sellerPhone,
                notes = notes
            )
            _userMessage.value = "کاڵاکە بە سەرکەوتوویی پاشەکەوت کرا"
            onSuccess()
        }
    }

    fun markAsDelivered(id: Long, onDelivered: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.markAsDelivered(id)
            _userMessage.value = "کاڵاکە گەیەندرا و لە سیستەم لادرا"
            _matchEvaluation.value = null
            onDelivered?.invoke()
        }
    }

    fun markAsPending(id: Long) {
        viewModelScope.launch {
            repository.markAsPending(id)
            _userMessage.value = "کاڵاکە گەڕێندرایەوە بۆ چاوەڕوانکراو"
        }
    }

    fun deleteParcel(parcel: ParcelEntity) {
        viewModelScope.launch {
            repository.deleteParcel(parcel)
            _userMessage.value = "کاڵاکە سڕایەوە"
        }
    }

    fun checkParcelByImage(bitmap: Bitmap) {
        _currentScanBitmap.value = bitmap
        _isMatching.value = true
        _matchEvaluation.value = null

        viewModelScope.launch {
            val pendingList = repository.getPendingParcelsList()
            val evaluation = withContext(Dispatchers.Default) {
                VisualMatcher.findBestMatchWithOcr(bitmap, pendingList)
            }
            _isMatching.value = false
            _matchEvaluation.value = evaluation
        }
    }

    fun manuallySelectCandidate(parcel: ParcelEntity) {
        val currentEval = _matchEvaluation.value
        _matchEvaluation.value = MatchEvaluation(
            parcel = parcel,
            confidencePercent = 100,
            isMatch = true,
            matchReason = "دەستنیشانکراوی دەستی لەلایەن بەکارهێنەرەوە",
            allPendingCandidates = currentEval?.allPendingCandidates ?: emptyList()
        )
    }

    fun testWithSampleParcel() {
        viewModelScope.launch {
            val sampleBitmap = withContext(Dispatchers.IO) {
                try {
                    val stream = getApplication<Application>().assets.open("sample_parcel.jpg")
                    val bmp = BitmapFactory.decodeStream(stream)
                    stream.close()
                    bmp
                } catch (e: Exception) {
                    null
                }
            }
            if (sampleBitmap != null) {
                checkParcelByImage(sampleBitmap)
            } else {
                _userMessage.value = "وێنەی نموونە نەدۆزرایەوە"
            }
        }
    }

    fun clearScan() {
        _currentScanBitmap.value = null
        _matchEvaluation.value = null
        _isMatching.value = false
    }

    fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return repository.loadBitmapFromUri(uri)
    }

    private fun parcelMatchesQuery(parcel: ParcelEntity, query: String): Boolean {
        val q = query.lowercase().trim()
        return parcel.deliveryAddress.lowercase().contains(q) ||
                parcel.recipientPhone.contains(q) ||
                parcel.recipientName.lowercase().contains(q) ||
                parcel.price.toString().contains(q) ||
                parcel.notes.lowercase().contains(q)
    }
}
