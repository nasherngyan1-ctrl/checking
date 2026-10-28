package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.data.local.ParcelDao
import com.example.data.model.ParcelEntity
import com.example.util.VisualMatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ParcelRepository(
    private val context: Context,
    private val parcelDao: ParcelDao
) {
    val pendingParcels: Flow<List<ParcelEntity>> =
        parcelDao.getParcelsByStatus(ParcelEntity.STATUS_PENDING)

    val deliveredParcels: Flow<List<ParcelEntity>> =
        parcelDao.getParcelsByStatus(ParcelEntity.STATUS_DELIVERED)

    val allParcels: Flow<List<ParcelEntity>> =
        parcelDao.getAllParcels()

    val totalPendingPrice: Flow<Long?> = parcelDao.getTotalPendingPrice()
    val totalDeliveredPrice: Flow<Long?> = parcelDao.getTotalDeliveredPrice()

    suspend fun getPendingParcelsList(): List<ParcelEntity> = withContext(Dispatchers.IO) {
        parcelDao.getPendingParcelsList()
    }

    suspend fun insertParcel(
        bitmap: Bitmap,
        price: Long,
        recipientName: String,
        recipientPhone: String,
        deliveryAddress: String,
        sellerName: String,
        sellerPhone: String,
        notes: String
    ): Long = withContext(Dispatchers.IO) {
        val photoPath = saveBitmapToFile(bitmap)
        val fingerprint = VisualMatcher.generateFingerprint(bitmap)

        val parcel = ParcelEntity(
            photoPath = photoPath,
            price = price,
            currency = "د.ع",
            recipientName = recipientName.trim(),
            recipientPhone = recipientPhone.trim(),
            deliveryAddress = deliveryAddress.trim(),
            sellerName = sellerName.trim(),
            sellerPhone = sellerPhone.trim(),
            notes = notes.trim(),
            status = ParcelEntity.STATUS_PENDING,
            visualFingerprint = fingerprint,
            createdAt = System.currentTimeMillis()
        )
        parcelDao.insertParcel(parcel)
    }

    suspend fun markAsDelivered(id: Long) = withContext(Dispatchers.IO) {
        parcelDao.markAsDelivered(id)
    }

    suspend fun markAsPending(id: Long) = withContext(Dispatchers.IO) {
        parcelDao.markAsPending(id)
    }

    suspend fun deleteParcel(parcel: ParcelEntity) = withContext(Dispatchers.IO) {
        // Also remove image file
        try {
            val file = File(parcel.photoPath)
            if (file.exists()) {
                file.delete()
            }
        } catch (_: Exception) {}
        parcelDao.deleteParcel(parcel)
    }

    suspend fun seedSampleParcelIfEmpty() = withContext(Dispatchers.IO) {
        val count = parcelDao.getParcelCount()
        if (count == 0) {
            try {
                val assetManager = context.assets
                val inputStream: InputStream? = try {
                    assetManager.open("sample_parcel.jpg")
                } catch (e: Exception) {
                    null
                }

                if (inputStream != null) {
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream.close()
                    if (bitmap != null) {
                        insertParcel(
                            bitmap = bitmap,
                            price = 47000L,
                            recipientName = "کڕیاری مام زاوا",
                            recipientPhone = "07507496271",
                            deliveryAddress = "هەولێر / مام زاوا",
                            sellerName = "فۆڕ بەیروت",
                            sellerPhone = "07704753999",
                            notes = "دلی فۆڕ بەیروت - وەرگیراوە"
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveBitmapToFile(bitmap: Bitmap): String {
        val parcelsDir = File(context.filesDir, "parcels")
        if (!parcelsDir.exists()) {
            parcelsDir.mkdirs()
        }
        val file = File(parcelsDir, "parcel_${System.currentTimeMillis()}_${(1000..9999).random()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        return file.absolutePath
    }

    fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun loadBitmapFromFile(path: String): Bitmap? {
        return try {
            val file = File(path)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
