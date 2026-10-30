package com.example

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.model.SaloonService
import com.example.data.model.ServiceEntry
import com.example.data.model.Worker
import com.example.data.repository.SalaryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SalaryDailyUnitTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: SalaryRepository

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        repository = SalaryRepository(
            database.workerDao(),
            database.serviceEntryDao(),
            database.saloonServiceDao()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testWorkerAndSaloonEntryPersistence() = runBlocking {
        val worker = Worker(id = 1, name = "ADNAN", phone = "+919876543210")
        repository.addWorker(worker)

        val workers = repository.getAllWorkers().first()
        assertEquals(1, workers.size)
        assertEquals("ADNAN", workers[0].name)

        val service = SaloonService(name = "Haircut", price = 200.0, iconEmoji = "💇‍♂️")
        repository.addSaloonService(service)

        val services = repository.getAllSaloonServices().first()
        assertEquals(1, services.size)
        assertEquals("Haircut", services[0].name)
        assertEquals(200.0, services[0].price, 0.01)

        val entry = ServiceEntry(
            workerId = 1,
            workerName = "ADNAN",
            date = "01.10.2026",
            time = "9:11 am",
            jobNumber = 1,
            serviceName = "Haircut + Shave",
            clientName = "Rahul Kumar",
            amount = 300.0,
            paymentMethod = "CASH"
        )
        repository.addServiceEntry(entry)

        val dailyEntries = repository.getEntriesByDate("01.10.2026").first()
        assertEquals(1, dailyEntries.size)
        assertEquals(300.0, dailyEntries[0].amount, 0.01)
        assertEquals("CASH", dailyEntries[0].paymentMethod)

        val nextJob = repository.getNextJobNumber("01.10.2026", "ADNAN")
        assertEquals(2, nextJob)
    }

    @Test
    fun testJsonExportAndImport() = runBlocking {
        val workers = listOf(Worker(id = 1, name = "BOOPATHI", phone = "+919876543211"))
        val entries = listOf(
            ServiceEntry(
                workerName = "BOOPATHI",
                date = "01.10.2026",
                jobNumber = 1,
                amount = 1760.0,
                paymentMethod = "CASH"
            )
        )

        val json = repository.exportDataToJson(entries, workers)
        assertNotNull(json)
        assertTrue(json.contains("BOOPATHI"))
        assertTrue(json.contains("1760"))

        val imported = repository.importDataFromJson(json)
        assertTrue(imported)
    }

    @Test
    fun testClearAllEntriesForFreshAccount() = runBlocking {
        repository.addServiceEntry(
            ServiceEntry(
                workerName = "SATHISH",
                date = "01.10.2026",
                jobNumber = 1,
                amount = 500.0,
                paymentMethod = "GPAY"
            )
        )
        val beforeClear = repository.getAllEntries().first()
        assertEquals(1, beforeClear.size)

        repository.clearAllEntries()

        val afterClear = repository.getAllEntries().first()
        assertEquals(0, afterClear.size)
    }
}
