package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.Worker
import com.example.ui.theme.CashGreen
import com.example.ui.theme.CashGreenLight
import com.example.ui.theme.GPayBlue
import com.example.ui.theme.GPayBlueLight
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddJobDialog(
    initialWorkerName: String,
    workers: List<Worker>,
    existingClients: List<String>,
    onDismiss: () -> Unit,
    onSaveJob: (workerName: String, amount: Double, paymentMethod: String, clientName: String, serviceName: String, notes: String) -> Unit
) {
    var selectedWorker by remember {
        mutableStateOf(if (initialWorkerName.isNotBlank()) initialWorkerName else workers.firstOrNull()?.name ?: "ADNAN")
    }
    var amountText by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("CASH") }
    var clientName by remember { mutableStateOf("") }
    var serviceName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val presetAmounts = listOf(100, 150, 200, 250, 300, 400, 500)
    val saloonPresets = listOf(
        "Haircut", "Shave", "Hair Color", "Beard Trim",
        "Mustache Colour", "Head Massage", "D-Tan", "Facial", "Haircut + Shave"
    )
    val clientPresets = listOf("Walk-in Customer", "Regular Customer", "Rahul", "Vignesh", "Karthik")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp)
                .testTag("add_job_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Add Saloon Work",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Record haircut, shave, color or custom service",
                            fontSize = 12.sp,
                            color = Slate500
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_add_job_dialog")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stylist Selection Chips
                Text(
                    text = "Select Stylist / Barber",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate700
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    workers.forEach { worker ->
                        val isSelected = selectedWorker.equals(worker.name, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedWorker = worker.name },
                            label = { Text(worker.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("chip_worker_${worker.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Payment Method Selector: CASH vs GPAY
                Text(
                    text = "Payment Mode",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate700
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // CASH Option
                    val isCash = paymentMethod == "CASH"
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isCash) CashGreenLight else MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isCash) 2.dp else 1.dp,
                            color = if (isCash) CashGreen else MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { paymentMethod = "CASH" }
                            .padding(vertical = 12.dp)
                            .testTag("payment_cash_option")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = "Cash",
                                tint = if (isCash) CashGreen else Slate600,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Cash",
                                fontWeight = FontWeight.Bold,
                                color = if (isCash) CashGreen else Slate600
                            )
                        }
                    }

                    // GPAY Option
                    val isGPay = paymentMethod == "GPAY"
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isGPay) GPayBlueLight else MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isGPay) 2.dp else 1.dp,
                            color = if (isGPay) GPayBlue else MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { paymentMethod = "GPAY" }
                            .padding(vertical = 12.dp)
                            .testTag("payment_gpay_option")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = "GPay",
                                tint = if (isGPay) GPayBlue else Slate600,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "GPay / UPI",
                                fontWeight = FontWeight.Bold,
                                color = if (isGPay) GPayBlue else Slate600
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Service Name & Quick Presets
                OutlinedTextField(
                    value = serviceName,
                    onValueChange = { serviceName = it },
                    label = { Text("Service Description") },
                    placeholder = { Text("e.g. Haircut, Shave, Beard Trim, Hair Color") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.ContentCut, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    saloonPresets.forEach { preset ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { serviceName = preset }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = preset, fontSize = 11.sp, color = Slate700)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Amount Field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Service Amount (₹)") },
                    placeholder = { Text("e.g. 200") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("amount_input_field")
                )

                // Quick Amount Presets
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    presetAmounts.forEach { preset ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    val current = amountText.toDoubleOrNull() ?: 0.0
                                    amountText = (current + preset).toLong().toString()
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "+₹$preset",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { amountText = "0" }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "₹0 (Standby)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate600
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Customer / Client Name
                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Customer Name (Optional)") },
                    placeholder = { Text("e.g. Walk-in Customer, Rahul") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("client_name_field")
                )

                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    clientPresets.forEach { cName ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { clientName = cName }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = cName, fontSize = 10.sp, color = Slate600)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Button
                Button(
                    onClick = {
                        val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
                        onSaveJob(
                            selectedWorker,
                            parsedAmount,
                            paymentMethod,
                            if (clientName.isBlank()) "Walk-in Customer" else clientName,
                            if (serviceName.isBlank()) "Haircut & Grooming" else serviceName,
                            notes
                        )
                        onDismiss()
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_job_button")
                ) {
                    Icon(imageVector = Icons.Default.Receipt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save Service & Log Entry",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
