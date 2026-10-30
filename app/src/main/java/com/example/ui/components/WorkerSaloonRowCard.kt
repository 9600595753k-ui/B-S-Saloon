package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ServiceEntry
import com.example.data.model.Worker
import com.example.ui.theme.CashGreen
import com.example.ui.theme.CashGreenLight
import com.example.ui.theme.GPayBlue
import com.example.ui.theme.GPayBlueLight
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700

@Composable
fun WorkerSaloonRowCard(
    workerName: String,
    worker: Worker?,
    entries: List<ServiceEntry>,
    isSelectedForPunch: Boolean,
    onSelectForPunch: () -> Unit,
    onAddJobClick: () -> Unit,
    onJobClick: (ServiceEntry) -> Unit,
    onWhatsAppShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }

    val totalEarnings = entries.sumOf { it.amount }
    val totalCash = entries.filter { it.paymentMethod.equals("CASH", true) }.sumOf { it.amount }
    val totalGPay = entries.filter { it.paymentMethod.equals("GPAY", true) }.sumOf { it.amount }
    val timeLabel = entries.firstOrNull()?.time ?: "9:00 am"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelectedForPunch) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (isSelectedForPunch) 2.dp else 1.dp,
                color = if (isSelectedForPunch) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("worker_row_${workerName.lowercase()}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Main Row Header: Avatar, Name, Total, WhatsApp, and Quick Add
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Stylist Avatar & Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onSelectForPunch() }
                        .weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isSelectedForPunch) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = workerName.take(1),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (isSelectedForPunch) Color.White else MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = workerName.uppercase(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelectedForPunch) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Active",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${entries.size} Services • Started $timeLabel",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                }

                // Middle: Total Earnings Pill
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "₹${totalEarnings.toLong()}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "💵 ₹${totalCash.toLong()}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CashGreen
                        )
                        Text(text = "•", fontSize = 10.sp, color = Slate500)
                        Text(
                            text = "📱 ₹${totalGPay.toLong()}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GPayBlue
                        )
                    }
                }

                // Right: Action buttons (WhatsApp + Add Job)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // WhatsApp PDF Slip Button
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF25D366),
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .clickable { onWhatsAppShareClick() }
                            .testTag("row_whatsapp_btn_${workerName.lowercase()}"),
                        shadowElevation = 1.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Send WhatsApp Slip",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Direct Add Button
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .clickable { onAddJobClick() }
                            .testTag("row_add_btn_${workerName.lowercase()}"),
                        shadowElevation = 1.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Job",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand / Collapse",
                            tint = Slate500
                        )
                    }
                }
            }

            // Expanded Services Row: Numbered items (1. 2590, 2. 0, 3. 1820 ...)
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    if (entries.isEmpty()) {
                        Text(
                            text = "No services logged today for $workerName. Tap Quick Entry above to add.",
                            fontSize = 12.sp,
                            color = Slate500,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        // Horizontal scrollable list of service chips with job numbers
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            entries.forEach { entry ->
                                val isCash = entry.paymentMethod.equals("CASH", true)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (entry.amount == 0.0) MaterialTheme.colorScheme.surfaceVariant else if (isCash) CashGreenLight.copy(alpha = 0.7f) else GPayBlueLight.copy(alpha = 0.7f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = 1.dp,
                                        color = if (entry.amount == 0.0) Color.Transparent else if (isCash) CashGreen.copy(alpha = 0.4f) else GPayBlue.copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onJobClick(entry) }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${entry.jobNumber}. ",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Slate700
                                            )
                                            Text(
                                                text = if (entry.amount == 0.0) "₹0 (Off)" else "₹${entry.amount.toLong()}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (entry.amount == 0.0) Slate500 else if (isCash) CashGreen else GPayBlue
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isCash) "Cash" else "GPay",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCash) CashGreen else GPayBlue
                                            )
                                        }
                                        if (entry.serviceName.isNotBlank() && entry.serviceName != "Service Work") {
                                            Text(
                                                text = entry.serviceName,
                                                fontSize = 10.sp,
                                                color = Slate600,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
