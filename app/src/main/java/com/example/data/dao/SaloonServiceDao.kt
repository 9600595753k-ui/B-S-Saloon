package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SaloonService
import kotlinx.coroutines.flow.Flow

@Dao
interface SaloonServiceDao {
    @Query("SELECT * FROM saloon_services ORDER BY sortOrder ASC, id ASC")
    fun getAllServices(): Flow<List<SaloonService>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: SaloonService): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServices(services: List<SaloonService>)

    @Update
    suspend fun updateService(service: SaloonService)

    @Delete
    suspend fun deleteService(service: SaloonService)

    @Query("SELECT COUNT(*) FROM saloon_services")
    suspend fun getCount(): Int
}
