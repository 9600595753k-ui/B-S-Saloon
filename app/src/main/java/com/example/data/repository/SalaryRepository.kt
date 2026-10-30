package com.example.data.repository

import com.example.data.dao.SaloonServiceDao
import com.example.data.dao.ServiceEntryDao
import com.example.data.dao.WorkerDao
import com.example.data.model.SaloonService
import com.example.data.model.ServiceEntry
import com.example.data.model.Worker
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class SalaryRepository(
    private val workerDao: WorkerDao,
    private val serviceEntryDao: ServiceEntryDao,
    private val saloonServiceDao: SaloonServiceDao
) {
    fun getAllWorkers(): Flow<List<Worker>> = workerDao.getAllWorkers()

    fun getAllSaloonServices(): Flow<List<SaloonService>> = saloonServiceDao.getAllServices()

    suspend fun addSaloonService(service: SaloonService): Long = saloonServiceDao.insertService(service)

    suspend fun updateSaloonService(service: SaloonService) = saloonServiceDao.updateService(service)

    suspend fun deleteSaloonService(service: SaloonService) = saloonServiceDao.deleteService(service)

    fun getEntriesByDate(date: String): Flow<List<ServiceEntry>> =
        serviceEntryDao.getEntriesByDate(date)

    fun getAllEntries(): Flow<List<ServiceEntry>> =
        serviceEntryDao.getAllEntries()

    fun getEntriesByWorker(workerName: String): Flow<List<ServiceEntry>> =
        serviceEntryDao.getEntriesByWorker(workerName)

    fun getEntriesByClient(clientName: String): Flow<List<ServiceEntry>> =
        serviceEntryDao.getEntriesByClient(clientName)

    fun getDistinctDates(): Flow<List<String>> =
        serviceEntryDao.getDistinctDates()

    fun getDistinctClients(): Flow<List<String>> =
        serviceEntryDao.getDistinctClients()

    suspend fun getNextJobNumber(date: String, workerName: String): Int {
        val maxJob = serviceEntryDao.getMaxJobNumber(date, workerName) ?: 0
        return maxJob + 1
    }

    suspend fun addServiceEntry(entry: ServiceEntry): Long =
        serviceEntryDao.insertEntry(entry)

    suspend fun updateServiceEntry(entry: ServiceEntry) =
        serviceEntryDao.updateEntry(entry)

    suspend fun deleteServiceEntry(entry: ServiceEntry) =
        serviceEntryDao.deleteEntry(entry)

    suspend fun deleteServiceEntryById(id: Long) =
        serviceEntryDao.deleteEntryById(id)

    suspend fun clearAllEntries() =
        serviceEntryDao.clearAll()

    suspend fun addWorker(worker: Worker): Long =
        workerDao.insertWorker(worker)

    suspend fun updateWorker(worker: Worker) =
        workerDao.updateWorker(worker)

    suspend fun deleteWorker(worker: Worker) =
        workerDao.deleteWorker(worker)

    suspend fun getWorkerByName(name: String): Worker? =
        workerDao.getWorkerByName(name)

    // Export all data to JSON string for Cloud storage / drive sync
    suspend fun exportDataToJson(entries: List<ServiceEntry>, workers: List<Worker>): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportTime", System.currentTimeMillis())

        val workersArray = JSONArray()
        workers.forEach { w ->
            val wObj = JSONObject()
            wObj.put("id", w.id)
            wObj.put("name", w.name)
            wObj.put("phone", w.phone)
            wObj.put("role", w.role)
            workersArray.put(wObj)
        }
        root.put("workers", workersArray)

        val entriesArray = JSONArray()
        entries.forEach { e ->
            val eObj = JSONObject()
            eObj.put("workerName", e.workerName)
            eObj.put("date", e.date)
            eObj.put("time", e.time)
            eObj.put("jobNumber", e.jobNumber)
            eObj.put("serviceName", e.serviceName)
            eObj.put("clientName", e.clientName)
            eObj.put("amount", e.amount)
            eObj.put("paymentMethod", e.paymentMethod)
            eObj.put("invoiceNumber", e.invoiceNumber)
            eObj.put("notes", e.notes)
            entriesArray.put(eObj)
        }
        root.put("serviceEntries", entriesArray)

        return root.toString(2)
    }

    // Import from JSON
    suspend fun importDataFromJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            if (root.has("workers")) {
                val workersArray = root.getJSONArray("workers")
                for (i in 0 until workersArray.length()) {
                    val wObj = workersArray.getJSONObject(i)
                    val worker = Worker(
                        name = wObj.optString("name", "Unknown"),
                        phone = wObj.optString("phone", ""),
                        role = wObj.optString("role", "Technician")
                    )
                    workerDao.insertWorker(worker)
                }
            }

            if (root.has("serviceEntries")) {
                val entriesArray = root.getJSONArray("serviceEntries")
                val entries = mutableListOf<ServiceEntry>()
                for (i in 0 until entriesArray.length()) {
                    val eObj = entriesArray.getJSONObject(i)
                    entries.add(
                        ServiceEntry(
                            workerName = eObj.optString("workerName", "Worker"),
                            date = eObj.optString("date", "01.10.2026"),
                            time = eObj.optString("time", "9:00 am"),
                            jobNumber = eObj.optInt("jobNumber", 1),
                            serviceName = eObj.optString("serviceName", "Service"),
                            clientName = eObj.optString("clientName", "General Client"),
                            amount = eObj.optDouble("amount", 0.0),
                            paymentMethod = eObj.optString("paymentMethod", "GPAY"),
                            invoiceNumber = eObj.optString("invoiceNumber", ""),
                            notes = eObj.optString("notes", "")
                        )
                    )
                }
                serviceEntryDao.insertEntries(entries)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
