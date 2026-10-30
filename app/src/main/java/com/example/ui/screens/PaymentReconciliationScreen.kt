package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ServiceEntry
import com.example.data.model.Worker
import com.example.ui.SalaryViewModel
import com.example.ui.theme.CashGreen
import com.example.ui.theme.CashGreenLight
import com.example.ui.theme.GPayBlue
import com.example.ui.theme.GPayBlueLight
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700

@Composable
fun PaymentReconciliationScreen(
    viewModel: SalaryViewModel,
    selectedDate: String,
    dailyEntries: List<ServiceEntry>,
    workers: List<Worker>,
    modifier: Modifier = Modifier
) {
    var physicalCashInput by remember { mutableStateOf("") }

    val totalDayRevenue = dailyEntries.sumOf { it.amount }
    val totalCashLogged = dailyEntries.filter { it.paymentMethod.equals("CASH", true) }.sumOf { it.amount }
    val totalGPayLogged = dailyEntries.filter { it.paymentMethod.equals("GPAY", true) }.sumOf { it.amount }

    val physicalCash = physicalCashInput.toDoubleOrNull()
    val cashDifference = if (physicalCash != null) physicalCash - totalCashLogged else null

    val isSystemBalanced = (totalCashLogged + totalGPayLogged) == totalDayRevenue

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("reconciliation_screen")
    ) {
        Text(
            text = "Payment Balancing & Reconciler",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Cash & GPay settlement verification for $selectedDate",
            fontSize = 13.sp,
            color = Slate600
        )

        Spacer(modifier = Modifier.height(16.dp))

        // System Balance Status Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSystemBalanced) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = if (isSystemBalanced) Color(0xFF86EFAC) else Color(0xFFFCA5A5)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isSystemBalanced) CashGreen else Color(0xFFEF4444),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isSystemBalanced) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = if (isSystemBalanced) "Ledger Balanced 100%" else "Balance Discrepancy Found",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSystemBalanced) CashGreen else Color(0xFFB91C1C)
                    )
                    Text(
                        text = "Cash (₹${totalCashLogged.toLong()}) + GPay (₹${totalGPayLogged.toLong()}) = Total (₹${totalDayRevenue.toLong()})",
                        fontSize = 12.sp,
                        color = Slate700
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cash vs GPay Breakdown Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cash Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = CashGreenLight,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AttachMoney,
                                    contentDescription = null,
                                    tint = CashGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cash in Hand",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate600
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "₹${totalCashLogged.toLong()}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = CashGreen
                    )
                    Text(
                        text = "${dailyEntries.count { it.paymentMethod.equals("CASH", true) && it.amount > 0 }} Cash jobs",
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }
            }

            // GPay Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = GPayBlueLight,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = GPayBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GPay / Online",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate600
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "₹${totalGPayLogged.toLong()}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = GPayBlue
                    )
                    Text(
                        text = "${dailyEntries.count { it.paymentMethod.equals("GPAY", true) && it.amount > 0 }} GPay jobs",
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Physical Cash Drawer Verification Tool
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Daily Cash Drawer Reconciliation",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Count and enter actual physical cash handed over by technicians",
                    fontSize = 12.sp,
                    color = Slate600
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = physicalCashInput,
                    onValueChange = { physicalCashInput = it },
                    label = { Text("Actual Physical Cash Counted (₹)") },
                    placeholder = { Text("e.g. ${totalCashLogged.toLong()}") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("physical_cash_input")
                )

                if (cashDifference != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    val isExact = cashDifference == 0.0
                    val isShort = cashDifference < 0
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isExact) Color(0xFFF0FDF4) else if (isShort) Color(0xFFFEF2F2) else Color(0xFFEFF6FF)
                            )
                            .padding(10.dp)
                    ) {
                        Text(
                            text = when {
                                isExact -> "✓ Exact Match! Physical cash matches system records perfectly."
                                isShort -> "⚠️ Cash Shortage: Missing ₹${(-cashDifference).toLong()} in drawer!"
                                else -> "ℹ️ Cash Surplus: Drawer has ₹${cashDifference.toLong()} extra cash."
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = when {
                                isExact -> CashGreen
                                isShort -> Color(0xFFDC2626)
                                else -> GPayBlue
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Worker Collection Breakdown
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Worker Payment Breakdown",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Cash collected vs GPay payments received per technician",
                    fontSize = 12.sp,
                    color = Slate600
                )

                Spacer(modifier = Modifier.height(14.dp))

                val workerNames = if (workers.isNotEmpty()) workers.map { it.name } else listOf("ADNAN", "BOOPATHI", "SATHISH", "SATHIYA")
                val entriesByWorker = dailyEntries.groupBy { it.workerName }

                workerNames.forEach { name ->
                    val workerEntries = entriesByWorker[name] ?: emptyList()
                    val wTotal = workerEntries.sumOf { it.amount }
                    val wCash = workerEntries.filter { it.paymentMethod.equals("CASH", true) }.sumOf { it.amount }
                    val wGPay = workerEntries.filter { it.paymentMethod.equals("GPAY", true) }.sumOf { it.amount }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Total: ₹${wTotal.toLong()}",
                                fontSize = 12.sp,
                                color = Slate600
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Cash
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CashGreenLight)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Cash: ₹${wCash.toLong()}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CashGreen
                                )
                            }

                            // GPay
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GPayBlueLight)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "GPay: ₹${wGPay.toLong()}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GPayBlue
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
