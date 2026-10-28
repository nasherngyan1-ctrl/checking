package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parcels")
data class ParcelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val photoPath: String,
    val price: Long,
    val currency: String = "د.ع",
    val recipientName: String = "",
    val recipientPhone: String = "",
    val deliveryAddress: String = "",
    val sellerName: String = "",
    val sellerPhone: String = "",
    val notes: String = "",
    val status: String = STATUS_PENDING,
    val visualFingerprint: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val deliveredAt: Long? = null
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_DELIVERED = "DELIVERED"
    }

    val isDelivered: Boolean
        get() = status == STATUS_DELIVERED
}
