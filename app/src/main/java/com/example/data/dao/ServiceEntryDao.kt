package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ServiceEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceEntryDao {
    @Query("SELECT * FROM service_entries WHERE date = :date ORDER BY jobNumber ASC, id ASC")
    fun getEntriesByDate(date: String): Flow<List<ServiceEntry>>

    @Query("SELECT * FROM service_entries ORDER BY createdAt DESC")
    fun getAllEntries(): Flow<List<ServiceEntry>>

    @Query("SELECT * FROM service_entries WHERE workerName = :workerName ORDER BY createdAt DESC")
    fun getEntriesByWorker(workerName: String): Flow<List<ServiceEntry>>

    @Query("SELECT * FROM service_entries WHERE clientName = :clientName ORDER BY createdAt DESC")
    fun getEntriesByClient(clientName: String): Flow<List<ServiceEntry>>

    @Query("SELECT DISTINCT date FROM service_entries ORDER BY date DESC")
    fun getDistinctDates(): Flow<List<String>>

    @Query("SELECT DISTINCT clientName FROM service_entries WHERE clientName != '' ORDER BY clientName ASC")
    fun getDistinctClients(): Flow<List<String>>

    @Query("SELECT MAX(jobNumber) FROM service_entries WHERE date = :date AND workerName = :workerName")
    suspend fun getMaxJobNumber(date: String, workerName: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: ServiceEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<ServiceEntry>)

    @Update
    suspend fun updateEntry(entry: ServiceEntry)

    @Delete
    suspend fun deleteEntry(entry: ServiceEntry)

    @Query("DELETE FROM service_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)

    @Query("SELECT COUNT(*) FROM service_entries")
    suspend fun getCount(): Int

    @Query("DELETE FROM service_entries")
    suspend fun clearAll()
}
