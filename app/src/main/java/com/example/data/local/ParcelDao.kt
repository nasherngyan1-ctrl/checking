package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ParcelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ParcelDao {

    @Query("SELECT * FROM parcels WHERE status = :status ORDER BY createdAt DESC")
    fun getParcelsByStatus(status: String): Flow<List<ParcelEntity>>

    @Query("SELECT * FROM parcels ORDER BY createdAt DESC")
    fun getAllParcels(): Flow<List<ParcelEntity>>

    @Query("SELECT * FROM parcels WHERE id = :id LIMIT 1")
    suspend fun getParcelById(id: Long): ParcelEntity?

    @Query("SELECT * FROM parcels WHERE status = 'PENDING'")
    suspend fun getPendingParcelsList(): List<ParcelEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParcel(parcel: ParcelEntity): Long

    @Update
    suspend fun updateParcel(parcel: ParcelEntity)

    @Query("UPDATE parcels SET status = 'DELIVERED', deliveredAt = :timestamp WHERE id = :id")
    suspend fun markAsDelivered(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE parcels SET status = 'PENDING', deliveredAt = NULL WHERE id = :id")
    suspend fun markAsPending(id: Long)

    @Delete
    suspend fun deleteParcel(parcel: ParcelEntity)

    @Query("DELETE FROM parcels WHERE id = :id")
    suspend fun deleteParcelById(id: Long)

    @Query("SELECT COUNT(*) FROM parcels")
    suspend fun getParcelCount(): Int

    @Query("SELECT SUM(price) FROM parcels WHERE status = 'PENDING'")
    fun getTotalPendingPrice(): Flow<Long?>

    @Query("SELECT SUM(price) FROM parcels WHERE status = 'DELIVERED'")
    fun getTotalDeliveredPrice(): Flow<Long?>
}
