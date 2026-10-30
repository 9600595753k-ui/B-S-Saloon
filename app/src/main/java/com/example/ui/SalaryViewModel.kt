package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.SaloonService
import com.example.data.model.ServiceEntry
import com.example.data.model.Worker
import com.example.data.repository.SalaryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ScreenTab {
    LEDGER,      // Salon Service & Daily Staff Ledger
    SERVICES,    // Quick Click Salon Service Menu & Staff Management
    MONTHLY,     // Monthly revenue growth reports and charts
    CLIENTS,     // Client folders & invoices
    PAYMENTS,    // Cash vs GPay balance reconciler
    SETTINGS     // Cloud sync JSON export/import
}

data class MonthlyReportData(
    val selectedMonthLabel: String,
    val totalRevenue: Double,
    val totalCash: Double,
    val totalGPay: Double,
    val totalJobs: Int,
    val dailyRevenueMap: Map<String, Double>,
    val workerTotals: Map<String, Double>
)

class SalaryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SalaryRepository
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = SalaryRepository(
            database.workerDao(),
            database.serviceEntryDao(),
            database.saloonServiceDao()
        )
    }

    private val _selectedDate = MutableStateFlow(SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date()))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _currentTab = MutableStateFlow(ScreenTab.LEDGER)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    // View layout preference: true = Ergonomic Rows (ultra easy on mobile), false = 4-column cards
    private val _isRowViewMode = MutableStateFlow(true)
    val isRowViewMode: StateFlow<Boolean> = _isRowViewMode.asStateFlow()

    // Active stylist filter for fast entry
    private val _selectedStylist = MutableStateFlow("ADNAN")
    val selectedStylist: StateFlow<String> = _selectedStylist.asStateFlow()

    val workers: StateFlow<List<Worker>> = repository.getAllWorkers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val saloonServices: StateFlow<List<SaloonService>> = repository.getAllSaloonServices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableDates: StateFlow<List<String>> = repository.getDistinctDates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val clientFolders: StateFlow<List<String>> = repository.getDistinctClients()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyEntries: StateFlow<List<ServiceEntry>> = _selectedDate.flatMapLatest { date ->
        repository.getEntriesByDate(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEntries: StateFlow<List<ServiceEntry>> = repository.getAllEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Monthly Report Data calculation derived from allEntries and selectedDate
    val monthlyReport: StateFlow<MonthlyReportData> = combine(allEntries, selectedDate) { entries, date ->
        val monthSuffix = if (date.contains(".") && date.length >= 10) {
            date.substring(3)
        } else {
            SimpleDateFormat("MM.yyyy", Locale.getDefault()).format(Date())
        }

        val monthEntries = entries.filter { it.date.endsWith(monthSuffix) }
        val totalRevenue = monthEntries.sumOf { it.amount }
        val totalCash = monthEntries.filter { it.paymentMethod.equals("CASH", true) }.sumOf { it.amount }
        val totalGPay = monthEntries.filter { it.paymentMethod.equals("GPAY", true) }.sumOf { it.amount }
        val totalJobs = monthEntries.count { it.amount > 0 }

        val dailyMap = mutableMapOf<String, Double>()
        monthEntries.forEach { entry ->
            val d = entry.date.substringBefore(".")
            dailyMap[d] = (dailyMap[d] ?: 0.0) + entry.amount
        }

        val workerTotals = mutableMapOf<String, Double>()
        monthEntries.forEach { entry ->
            workerTotals[entry.workerName] = (workerTotals[entry.workerName] ?: 0.0) + entry.amount
        }

        MonthlyReportData(
            selectedMonthLabel = monthSuffix,
            totalRevenue = totalRevenue,
            totalCash = totalCash,
            totalGPay = totalGPay,
            totalJobs = totalJobs,
            dailyRevenueMap = dailyMap.toSortedMap(),
            workerTotals = workerTotals
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MonthlyReportData(SimpleDateFormat("MM.yyyy", Locale.getDefault()).format(Date()), 0.0, 0.0, 0.0, 0, emptyMap(), emptyMap())
    )

    fun setTab(tab: ScreenTab) {
        _currentTab.value = tab
    }

    fun setDate(date: String) {
        _selectedDate.value = date
    }

    fun toggleViewMode() {
        _isRowViewMode.value = !_isRowViewMode.value
    }

    fun setSelectedStylist(stylist: String) {
        _selectedStylist.value = stylist
    }

    fun setTodayDate() {
        _selectedDate.value = dateFormat.format(Date())
    }

    fun setPreviousDay() {
        try {
            val parsed = dateFormat.parse(_selectedDate.value) ?: Date()
            val cal = Calendar.getInstance().apply {
                time = parsed
                add(Calendar.DAY_OF_YEAR, -1)
            }
            _selectedDate.value = dateFormat.format(cal.time)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setNextDay() {
        try {
            val parsed = dateFormat.parse(_selectedDate.value) ?: Date()
            val cal = Calendar.getInstance().apply {
                time = parsed
                add(Calendar.DAY_OF_YEAR, 1)
            }
            _selectedDate.value = dateFormat.format(cal.time)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Ultra-fast 1-tap quick punch for salon service (Haircut, Shave, Hair Color, Beard Trim, etc.)
     */
    fun quickPunchSaloonJob(
        workerName: String,
        serviceNames: String,
        amount: Double,
        paymentMethod: String,
        clientName: String = "Walk-in Client"
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val date = _selectedDate.value
            val nextJob = repository.getNextJobNumber(date, workerName)
            val currentTime = timeFormat.format(Date()).lowercase()

            val worker = repository.getWorkerByName(workerName)
            val workerId = worker?.id ?: 0L
            val invNum = "INV-${date.replace(".", "")}-${workerName.take(2).uppercase()}$nextJob"

            val entry = ServiceEntry(
                workerId = workerId,
                workerName = workerName,
                date = date,
                time = currentTime,
                jobNumber = nextJob,
                serviceName = if (serviceNames.isBlank()) "Haircut / Grooming" else serviceNames,
                clientName = clientName,
                amount = amount,
                paymentMethod = paymentMethod,
                invoiceNumber = invNum
            )
            repository.addServiceEntry(entry)
        }
    }

    fun addServiceJob(
        workerName: String,
        amount: Double,
        paymentMethod: String,
        clientName: String,
        serviceName: String,
        notes: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val date = _selectedDate.value
            val nextJob = repository.getNextJobNumber(date, workerName)
            val currentTime = timeFormat.format(Date()).lowercase()

            val worker = repository.getWorkerByName(workerName)
            val workerId = worker?.id ?: 0L
            val invNum = "INV-${date.replace(".", "")}-${workerName.take(2).uppercase()}$nextJob"

            val entry = ServiceEntry(
                workerId = workerId,
                workerName = workerName,
                date = date,
                time = currentTime,
                jobNumber = nextJob,
                serviceName = if (serviceName.isBlank()) "Haircut & Shave #$nextJob" else serviceName,
                clientName = if (clientName.isBlank()) "Walk-in Customer" else clientName,
                amount = amount,
                paymentMethod = paymentMethod,
                invoiceNumber = invNum,
                notes = notes
            )
            repository.addServiceEntry(entry)
        }
    }

    fun updateServiceJob(entry: ServiceEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateServiceEntry(entry)
        }
    }

    fun updateJobDate(entry: ServiceEntry, newDate: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateServiceEntry(entry.copy(date = newDate))
        }
    }

    fun deleteServiceJob(entry: ServiceEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteServiceEntry(entry)
        }
    }

    fun addWorker(name: String, phone: String, role: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (name.isNotBlank()) {
                repository.addWorker(
                    Worker(
                        name = name.trim().uppercase(),
                        phone = phone.trim(),
                        role = if (role.isBlank()) "Hair Stylist" else role.trim()
                    )
                )
            }
        }
    }

    fun updateWorker(worker: Worker) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateWorker(worker)
        }
    }

    fun deleteWorker(worker: Worker) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteWorker(worker)
        }
    }

    fun addSaloonService(name: String, price: Double, emoji: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (name.isNotBlank()) {
                repository.addSaloonService(
                    SaloonService(
                        name = name.trim(),
                        price = price,
                        iconEmoji = if (emoji.isBlank()) "✂️" else emoji.trim()
                    )
                )
            }
        }
    }

    fun updateSaloonService(service: SaloonService) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSaloonService(service)
        }
    }

    fun deleteSaloonService(service: SaloonService) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSaloonService(service)
        }
    }

    suspend fun exportDataJson(): String {
        return repository.exportDataToJson(allEntries.value, workers.value)
    }

    suspend fun importDataJson(json: String): Boolean {
        return repository.importDataFromJson(json)
    }

    fun resetToNewAccount() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllEntries()
            _selectedDate.value = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
        }
    }
}
