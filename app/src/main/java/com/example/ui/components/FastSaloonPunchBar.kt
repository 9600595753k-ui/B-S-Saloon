package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.model.SaloonService
import com.example.data.model.Worker
import com.example.ui.theme.CashGreen
import com.example.ui.theme.CashGreenLight
import com.example.ui.theme.GPayBlue
import com.example.ui.theme.GPayBlueLight
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FastSaloonPunchBar(
    workers: List<Worker>,
    saloonServices: List<SaloonService>,
    selectedStylist: String,
    onStylistSelected: (String) -> Unit,
    onPunchService: (workerName: String, serviceNames: String, amount: Double, paymentMethod: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var paymentMethod by remember { mutableStateOf("CASH") } // Cash is standard in saloons, 1-tap toggle to GPay
    val selectedServiceItems = remember { mutableStateListOf<SaloonService>() }
    var customAmountText by remember { mutableStateOf("") }
    var showCustomInput by remember { mutableStateOf(false) }

    // Fallback if services list is loading
    val effectiveServices = if (saloonServices.isNotEmpty()) {
        saloonServices
    } else {
        listOf(
            SaloonService(name = "Haircut", price = 200.0, iconEmoji = "💇‍♂️"),
            SaloonService(name = "Shave", price = 100.0, iconEmoji = "🪒"),
            SaloonService(name = "Hair Color", price = 400.0, iconEmoji = "🎨"),
            SaloonService(name = "Beard Trim", price = 100.0, iconEmoji = "✂️"),
            SaloonService(name = "Mustache Colour", price = 120.0, iconEmoji = "🧔"),
            SaloonService(name = "Head Massage", price = 200.0, iconEmoji = "💆‍♂️"),
            SaloonService(name = "D-Tan / Bleach", price = 350.0, iconEmoji = "✨"),
            SaloonService(name = "Facial", price = 600.0, iconEmoji = "🧖‍♂️"),
            SaloonService(name = "Hair Spa", price = 300.0, iconEmoji = "🧴")
        )
    }

    val totalAmount = if (customAmountText.isNotBlank()) {
        customAmountText.toDoubleOrNull() ?: 0.0
    } else {
        selectedServiceItems.sumOf { it.price }
    }

    val stylistList = if (workers.isNotEmpty()) workers.map { it.name } else listOf("ADNAN", "BOOPATHI", "SATHISH", "SATHIYA")

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            .testTag("fast_saloon_punch_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Title & Payment toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFEF08A),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "⚡", fontSize = 16.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Quick Saloon Entry",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "1-click add for haircut, shave, color...",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                }

                // 1-Tap Payment Toggle: CASH or GPAY
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(2.dp)
                ) {
                    val isCash = paymentMethod == "CASH"
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (isCash) CashGreen else Color.Transparent,
                        modifier = Modifier
                            .clickable { paymentMethod = "CASH" }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("fast_punch_cash_toggle")
                    ) {
                        Text(
                            text = "💵 Cash",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCash) Color.White else Slate600
                        )
                    }

                    val isGPay = paymentMethod == "GPAY"
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (isGPay) GPayBlue else Color.Transparent,
                        modifier = Modifier
                            .clickable { paymentMethod = "GPAY" }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("fast_punch_gpay_toggle")
                    ) {
                        Text(
                            text = "📱 GPay",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isGPay) Color.White else Slate600
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 1. Stylist Selector Chips (ADNAN, BOOPATHI, SATHISH, SATHIYA)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                stylistList.forEach { name ->
                    val isSelected = selectedStylist.equals(name, true)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onStylistSelected(name) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("stylist_chip_${name.lowercase()}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = name,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Quick Click Saloon Services (Haircut, Shave, Hair Color, Beard Trim, Mustache Colour...)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                effectiveServices.forEach { service ->
                    val isSelected = selectedServiceItems.any { it.name == service.name }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                if (isSelected) {
                                    selectedServiceItems.removeAll { it.name == service.name }
                                } else {
                                    selectedServiceItems.add(service)
                                }
                                customAmountText = ""
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .testTag("quick_service_${service.name.lowercase().replace(" ", "_")}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = service.iconEmoji, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = service.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "₹${service.price.toLong()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Slate600
                            )
                        }
                    }
                }
            }

            // Custom Amount row
            AnimatedVisibility(visible = showCustomInput) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    OutlinedTextField(
                        value = customAmountText,
                        onValueChange = {
                            customAmountText = it
                            if (it.isNotBlank()) selectedServiceItems.clear()
                        },
                        label = { Text("Custom Amount (₹)") },
                        placeholder = { Text("e.g. 250") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Quick Action Row: Big Punch Button + Custom toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Custom Amount toggle button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showCustomInput = !showCustomInput }
                        .padding(horizontal = 10.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = if (showCustomInput) "✕ Cancel" else "₹ Custom",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate700
                    )
                }

                // Big One-Click Punch Button
                val hasSelection = totalAmount > 0
                val summaryLabel = if (selectedServiceItems.isNotEmpty()) {
                    selectedServiceItems.joinToString(" + ") { it.name }
                } else if (customAmountText.isNotBlank()) {
                    "Custom Saloon Service"
                } else {
                    "Select a Service"
                }

                Button(
                    onClick = {
                        if (hasSelection) {
                            onPunchService(
                                selectedStylist,
                                summaryLabel,
                                totalAmount,
                                paymentMethod
                            )
                            // Reset selections for next fast punch!
                            selectedServiceItems.clear()
                            customAmountText = ""
                            showCustomInput = false
                        }
                    },
                    enabled = hasSelection,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (paymentMethod == "CASH") CashGreen else GPayBlue,
                        disabledContainerColor = Slate500.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("fast_punch_action_button")
                ) {
                    Icon(imageVector = Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (hasSelection) "ADD ₹${totalAmount.toLong()} • $selectedStylist ($paymentMethod)" else "Tap Service to Add",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
