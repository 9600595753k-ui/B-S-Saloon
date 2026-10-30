package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FolderYellow
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700

@Composable
fun BreadcrumbBar(
    currentDate: String,
    availableDates: List<String>,
    onDateSelected: (String) -> Unit,
    onPreviousDayClick: () -> Unit = {},
    onNextDayClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showDatePickerDialog by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("breadcrumb_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // Saloon Logo / Home
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onHomeClick() }
                    .padding(2.dp)
                    .testTag("breadcrumb_home")
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.saloon_logo),
                    contentDescription = "BS Saloon Logo",
                    modifier = Modifier
                        .size(26.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Slate600,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))

            // "salary" text
            Text(
                text = "salary",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Slate700,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onHomeClick() }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .testTag("breadcrumb_salary")
            )

            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Slate600,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))

            // Previous Day Button
            IconButton(
                onClick = onPreviousDayClick,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Previous Day",
                    tint = Slate600,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Clickable Folder & Date badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    .clickable { showDatePickerDialog = true }
                    .padding(horizontal = 8.dp, vertical = 5.dp)
                    .testTag("breadcrumb_date_folder")
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = "Date Folder",
                    tint = FolderYellow,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = currentDate,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Next Day Button
            IconButton(
                onClick = onNextDayClick,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Next Day",
                    tint = Slate600,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Calendar Picker Button
            IconButton(
                onClick = { showDatePickerDialog = true },
                modifier = Modifier
                    .size(34.dp)
                    .testTag("calendar_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Select Date",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    if (showDatePickerDialog) {
        SaloonDatePickerDialog(
            currentDate = currentDate,
            availableDates = availableDates,
            onDismiss = { showDatePickerDialog = false },
            onDateSelected = { selected ->
                onDateSelected(selected)
            }
        )
    }
}
