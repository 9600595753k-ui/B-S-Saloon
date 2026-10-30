package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.SalaryViewModel
import com.example.ui.ScreenTab
import com.example.ui.screens.ClientFoldersScreen
import com.example.ui.screens.DailyLedgerScreen
import com.example.ui.screens.MonthlyRevenueScreen
import com.example.ui.screens.PaymentReconciliationScreen
import com.example.ui.screens.SaloonServicesScreen
import com.example.ui.screens.SettingsAndSyncScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: SalaryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SalaryAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SalaryAppContent(viewModel: SalaryViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val availableDates by viewModel.availableDates.collectAsStateWithLifecycle()
    val workers by viewModel.workers.collectAsStateWithLifecycle()
    val dailyEntries by viewModel.dailyEntries.collectAsStateWithLifecycle()
    val allEntries by viewModel.allEntries.collectAsStateWithLifecycle()
    val clientFolders by viewModel.clientFolders.collectAsStateWithLifecycle()
    val monthlyReport by viewModel.monthlyReport.collectAsStateWithLifecycle()
    val saloonServices by viewModel.saloonServices.collectAsStateWithLifecycle()

    // Handle back button to return to Daily Ledger if in subtab
    BackHandler(enabled = currentTab != ScreenTab.LEDGER) {
        viewModel.setTab(ScreenTab.LEDGER)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == ScreenTab.LEDGER,
                    onClick = { viewModel.setTab(ScreenTab.LEDGER) },
                    icon = {
                        Icon(imageVector = Icons.Default.TableRows, contentDescription = "Saloon Ledger")
                    },
                    label = {
                        Text(
                            text = "Ledger",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == ScreenTab.LEDGER) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("nav_tab_ledger")
                )

                NavigationBarItem(
                    selected = currentTab == ScreenTab.SERVICES,
                    onClick = { viewModel.setTab(ScreenTab.SERVICES) },
                    icon = {
                        Icon(imageVector = Icons.Default.ContentCut, contentDescription = "Salon Rates")
                    },
                    label = {
                        Text(
                            text = "Services",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == ScreenTab.SERVICES) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("nav_tab_services")
                )

                NavigationBarItem(
                    selected = currentTab == ScreenTab.MONTHLY,
                    onClick = { viewModel.setTab(ScreenTab.MONTHLY) },
                    icon = {
                        Icon(imageVector = Icons.Default.ShowChart, contentDescription = "Monthly Growth")
                    },
                    label = {
                        Text(
                            text = "Monthly",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == ScreenTab.MONTHLY) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("nav_tab_monthly")
                )

                NavigationBarItem(
                    selected = currentTab == ScreenTab.PAYMENTS,
                    onClick = { viewModel.setTab(ScreenTab.PAYMENTS) },
                    icon = {
                        Icon(imageVector = Icons.Default.AccountBalance, contentDescription = "Balance Reconciler")
                    },
                    label = {
                        Text(
                            text = "Balancing",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == ScreenTab.PAYMENTS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("nav_tab_balancing")
                )

                NavigationBarItem(
                    selected = currentTab == ScreenTab.SETTINGS,
                    onClick = { viewModel.setTab(ScreenTab.SETTINGS) },
                    icon = {
                        Icon(imageVector = Icons.Default.CloudSync, contentDescription = "Cloud & Staff")
                    },
                    label = {
                        Text(
                            text = "Sync",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == ScreenTab.SETTINGS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("nav_tab_sync")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                ScreenTab.LEDGER -> {
                    DailyLedgerScreen(
                        viewModel = viewModel,
                        selectedDate = selectedDate,
                        availableDates = availableDates,
                        workers = workers,
                        dailyEntries = dailyEntries,
                        existingClients = clientFolders
                    )
                }

                ScreenTab.SERVICES -> {
                    SaloonServicesScreen(
                        viewModel = viewModel,
                        saloonServices = saloonServices,
                        workers = workers
                    )
                }

                ScreenTab.MONTHLY -> {
                    MonthlyRevenueScreen(
                        viewModel = viewModel,
                        monthlyData = monthlyReport
                    )
                }

                ScreenTab.CLIENTS -> {
                    ClientFoldersScreen(
                        viewModel = viewModel,
                        allEntries = allEntries,
                        clientFolders = clientFolders
                    )
                }

                ScreenTab.PAYMENTS -> {
                    PaymentReconciliationScreen(
                        viewModel = viewModel,
                        selectedDate = selectedDate,
                        dailyEntries = dailyEntries,
                        workers = workers
                    )
                }

                ScreenTab.SETTINGS -> {
                    SettingsAndSyncScreen(
                        viewModel = viewModel,
                        workers = workers
                    )
                }
            }
        }
    }
}
