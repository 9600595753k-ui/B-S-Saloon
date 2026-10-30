package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SaloonService
import com.example.data.model.ServiceEntry
import com.example.data.model.Worker
import com.example.ui.SalaryViewModel
import com.example.ui.components.AddJobDialog
import com.example.ui.components.BreadcrumbBar
import com.example.ui.components.FastSaloonPunchBar
import com.example.ui.components.JobDetailDialog
import com.example.ui.components.WorkerSalaryCard
import com.example.ui.components.WorkerSaloonRowCard
import com.example.ui.theme.CashGreen
import com.example.ui.theme.CashGreenLight
import com.example.ui.theme.GPayBlue
import com.example.ui.theme.GPayBlueLight
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.util.WhatsAppShareHelper

@Composable
fun DailyLedgerScreen(
    viewModel: SalaryViewModel,
    selectedDate: String,
    availableDates: List<String>,
    workers: List<Worker>,
    dailyEntries: List<ServiceEntry>,
    existingClients: List<String>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAddJobDialog by remember { mutableStateOf(false) }
    var selectedWorkerForAdd by remember { mutableStateOf("") }
    var selectedEntryForDetail by remember { mutableStateOf<ServiceEntry?>(null) }

    val saloonServices by viewModel.saloonServices.collectAsStateWithLifecycle()
    val isRowViewMode by viewModel.isRowViewMode.collectAsStateWithLifecycle()
    val selectedStylist by viewModel.selectedStylist.collectAsStateWithLifecycle()

    val totalDayRevenue = remember(dailyEntries) { dailyEntries.sumOf { it.amount } }
    val totalCash = remember(dailyEntries) { dailyEntries.filter { it.paymentMethod.equals("CASH", true) }.sumOf { it.amount } }
    val totalGPay = remember(dailyEntries) { dailyEntries.filter { it.paymentMethod.equals("GPAY", true) }.sumOf { it.amount } }

    val entriesByWorker = remember(dailyEntries) { dailyEntries.groupBy { it.workerName } }

    val displayWorkers = remember(workers) {
        if (workers.isNotEmpty()) workers.map { it.name } else listOf("ADNAN", "BOOPATHI", "SATHISH", "SATHIYA")
    }

    Box(modifier = modifier.fillMaxSize()) {
        // High-performance LazyColumn for butter-smooth 60/120fps scrolling
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Breadcrumb Bar with Day Navigation (< and >) and Date Picker Dialog
            item(key = "breadcrumb") {
                BreadcrumbBar(
                    currentDate = selectedDate,
                    availableDates = availableDates,
                    onDateSelected = { viewModel.setDate(it) },
                    onPreviousDayClick = { viewModel.setPreviousDay() },
                    onNextDayClick = { viewModel.setNextDay() },
                    onHomeClick = { /* Already on ledger */ }
                )
            }

            // 2. 1-Click Fast Saloon Punch Bar (Haircut, Shave, Hair Color, Beard Trim, etc.)
            item(key = "fast_punch_bar") {
                FastSaloonPunchBar(
                    workers = workers,
                    saloonServices = saloonServices,
                    selectedStylist = selectedStylist,
                    onStylistSelected = { viewModel.setSelectedStylist(it) },
                    onPunchService = { stylist, serviceSummary, amount, paymentMethod ->
                        viewModel.quickPunchSaloonJob(
                            workerName = stylist,
                            serviceNames = serviceSummary,
                            amount = amount,
                            paymentMethod = paymentMethod
                        )
                        Toast.makeText(context, "Added ₹${amount.toLong()} for $stylist ($paymentMethod)", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
            }

            // 3. Quick KPI Ribbon & Layout Toggle (Rows vs Columns)
            item(key = "kpi_ribbon") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Total Revenue Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                            Text(
                                text = "Total Day Earnings",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Slate600
                            )
                            Text(
                                text = "₹${totalDayRevenue.toLong()}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Cash Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CashGreenLight,
                        modifier = Modifier.weight(0.9f)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                            Text(
                                text = "Cash",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = CashGreen
                            )
                            Text(
                                text = "₹${totalCash.toLong()}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = CashGreen
                            )
                        }
                    }

                    // GPay Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GPayBlueLight,
                        modifier = Modifier.weight(0.9f)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                            Text(
                                text = "GPay",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = GPayBlue
                            )
                            Text(
                                text = "₹${totalGPay.toLong()}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = GPayBlue
                            )
                        }
                    }

                    // Toggle View Button (Row vs Columns)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.toggleViewMode() }
                            .padding(horizontal = 10.dp, vertical = 10.dp)
                            .testTag("toggle_view_mode_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isRowViewMode) Icons.Default.ViewWeek else Icons.Default.TableRows,
                                contentDescription = "Switch View",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 4. Stylist Ledgers
            if (isRowViewMode) {
                // High-performance vertical items
                items(displayWorkers, key = { it }) { workerName ->
                    val workerObj = workers.find { it.name.equals(workerName, true) }
                    val entries = entriesByWorker[workerName] ?: emptyList()

                    WorkerSaloonRowCard(
                        workerName = workerName,
                        worker = workerObj,
                        entries = entries,
                        isSelectedForPunch = selectedStylist.equals(workerName, true),
                        onSelectForPunch = { viewModel.setSelectedStylist(workerName) },
                        onAddJobClick = {
                            selectedWorkerForAdd = workerName
                            showAddJobDialog = true
                        },
                        onJobClick = { entry ->
                            selectedEntryForDetail = entry
                        },
                        onWhatsAppShareClick = {
                            WhatsAppShareHelper.shareWorkerSalarySlip(
                                context = context,
                                worker = workerObj,
                                workerName = workerName,
                                date = selectedDate,
                                entries = entries
                            )
                        },
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                }
            } else {
                // Column Cards Mode
                item(key = "column_cards_row") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        displayWorkers.forEach { workerName ->
                            val workerObj = workers.find { it.name.equals(workerName, true) }
                            val entries = entriesByWorker[workerName] ?: emptyList()

                            WorkerSalaryCard(
                                workerName = workerName,
                                worker = workerObj,
                                entries = entries,
                                onAddJobClick = {
                                    selectedWorkerForAdd = workerName
                                    showAddJobDialog = true
                                },
                                onJobClick = { entry ->
                                    selectedEntryForDetail = entry
                                },
                                onWhatsAppShareClick = {
                                    WhatsAppShareHelper.shareWorkerSalarySlip(
                                        context = context,
                                        worker = workerObj,
                                        workerName = workerName,
                                        date = selectedDate,
                                        entries = entries
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // 5. WhatsApp Helper Card
            item(key = "whatsapp_info_card") {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF25D366),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "💬", fontSize = 18.sp)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Daily WhatsApp Clarification Slip",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tap the WhatsApp share icon on any stylist's card/row to send their complete daily job breakdown PDF.",
                                fontSize = 11.sp,
                                color = Slate600
                            )
                        }
                    }
                }
            }

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(90.dp))
            }
        }

        // Custom Service Work Dialog
        if (showAddJobDialog) {
            AddJobDialog(
                initialWorkerName = selectedWorkerForAdd,
                workers = workers,
                existingClients = existingClients,
                onDismiss = { showAddJobDialog = false },
                onSaveJob = { workerName, amount, paymentMethod, clientName, serviceName, notes ->
                    viewModel.addServiceJob(
                        workerName = workerName,
                        amount = amount,
                        paymentMethod = paymentMethod,
                        clientName = clientName,
                        serviceName = serviceName,
                        notes = notes
                    )
                }
            )
        }

        // Job Detail Dialog with Edit Date & Rate option
        selectedEntryForDetail?.let { entry ->
            JobDetailDialog(
                entry = entry,
                onDismiss = { selectedEntryForDetail = null },
                onShareInvoice = {
                    WhatsAppShareHelper.shareClientInvoice(context, entry)
                },
                onSaveEdit = { updatedEntry ->
                    viewModel.updateServiceJob(updatedEntry)
                    Toast.makeText(context, "Saved changes for Job #${updatedEntry.jobNumber}", Toast.LENGTH_SHORT).show()
                },
                onDelete = {
                    viewModel.deleteServiceJob(entry)
                    Toast.makeText(context, "Deleted Job #${entry.jobNumber}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}
