package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.SaloonServiceDao
import com.example.data.dao.ServiceEntryDao
import com.example.data.dao.WorkerDao
import com.example.data.model.SaloonService
import com.example.data.model.ServiceEntry
import com.example.data.model.Worker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Worker::class, ServiceEntry::class, SaloonService::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workerDao(): WorkerDao
    abstract fun serviceEntryDao(): ServiceEntryDao
    abstract fun saloonServiceDao(): SaloonServiceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "salary_daily_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.workerDao(), database.serviceEntryDao(), database.saloonServiceDao())
                }
            }
        }

        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
            super.onDestructiveMigration(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.workerDao(), database.serviceEntryDao(), database.saloonServiceDao())
                }
            }
        }
    }
}

suspend fun populateInitialData(
    workerDao: WorkerDao,
    entryDao: ServiceEntryDao,
    saloonServiceDao: SaloonServiceDao
) {
    // 1. Saloon Staff / Stylists
    val workers = listOf(
        Worker(id = 1, name = "ADNAN", phone = "+919876543210", role = "Senior Hair Stylist"),
        Worker(id = 2, name = "BOOPATHI", phone = "+919876543211", role = "Beard & Shave Specialist"),
        Worker(id = 3, name = "SATHISH", phone = "+919876543212", role = "Hair Colorist"),
        Worker(id = 4, name = "SATHIYA", phone = "+919876543213", role = "Stylist & Spa Expert")
    )
    workerDao.insertWorkers(workers)

    // 2. Quick Click Saloon Services with Indian Rupee rates
    val defaultServices = listOf(
        SaloonService(name = "Haircut", price = 200.0, iconEmoji = "💇‍♂️", sortOrder = 1),
        SaloonService(name = "Shave", price = 100.0, iconEmoji = "🪒", sortOrder = 2),
        SaloonService(name = "Hair Color", price = 400.0, iconEmoji = "🎨", sortOrder = 3),
        SaloonService(name = "Beard Trim", price = 100.0, iconEmoji = "✂️", sortOrder = 4),
        SaloonService(name = "Mustache Colour", price = 120.0, iconEmoji = "🧔", sortOrder = 5),
        SaloonService(name = "Head Massage", price = 200.0, iconEmoji = "💆‍♂️", sortOrder = 6),
        SaloonService(name = "D-Tan / Bleach", price = 350.0, iconEmoji = "✨", sortOrder = 7),
        SaloonService(name = "Facial", price = 600.0, iconEmoji = "🧖‍♂️", sortOrder = 8),
        SaloonService(name = "Hair Spa / Wash", price = 300.0, iconEmoji = "🧴", sortOrder = 9),
        SaloonService(name = "Haircut + Shave", price = 280.0, iconEmoji = "⚡", sortOrder = 10),
        SaloonService(name = "Haircut + Beard", price = 280.0, iconEmoji = "⚡", sortOrder = 11),
        SaloonService(name = "Full Grooming Combo", price = 750.0, iconEmoji = "🌟", sortOrder = 12)
    )
    saloonServiceDao.insertServices(defaultServices)

    // 3. New Account: Clear all service entries so it starts fresh with 0 jobs!
    entryDao.clearAll()
}
