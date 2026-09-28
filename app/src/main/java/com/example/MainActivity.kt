package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.core.content.ContextCompat
import com.example.data.model.OrderStatus
import com.example.printer.PrinterConnectionState
import com.example.ui.components.PrivacyPolicyDialog
import com.example.ui.components.VirtualThermalPrinterDialog
import com.example.ui.screens.ApartmentConsolidatorScreen
import com.example.ui.screens.HardwareSettingsScreen
import com.example.ui.screens.HarvestSheetScreen
import com.example.ui.screens.OrderBoardScreen
import com.example.ui.screens.StoreOrderingScreen
import com.example.ui.theme.SolaivanamTheme
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.FarmViewModel
import com.example.ui.viewmodel.UserRole

class MainActivity : ComponentActivity() {
    private val viewModel: FarmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SolaivanamTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: FarmViewModel) {
    val context = LocalContext.current
    var selectedScreenIndex by remember { mutableIntStateOf(0) }

    val language by viewModel.language.collectAsState()
    val role by viewModel.role.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val printerState by viewModel.printerManager.connectionState.collectAsState()
    val lastPrintedBitmap by viewModel.printerManager.lastPrintedBitmap.collectAsState()
    val lastPrintedOrder by viewModel.printerManager.lastPrintedOrder.collectAsState()
    val userNotice by viewModel.userNotice.collectAsState()

    var showPrinterModal by remember { mutableStateOf(false) }
    var showPrivacyPolicyModal by remember { mutableStateOf(false) }

    // Request Bluetooth permissions on Android 12+ (API 31+)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        viewModel.printerManager.refreshPairedDevices()
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val hasConnect = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            val hasScan = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
            if (!hasConnect || !hasScan) {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN
                    )
                )
            }
        }
    }

    // Show toast notices
    LaunchedEffect(userNotice) {
        userNotice?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.userNotice.value = null
        }
    }

    val pendingCount = remember(orders) { orders.count { it.status == OrderStatus.PENDING } }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (language == AppLanguage.TAMIL) "சோலைவனம் • SOLAIVANAM" else "SOLAIVANAM • சோலைவனம்",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF1B4D2E)
                        )
                        Text(
                            text = "Farm-to-Home & 58mm Thermal Print",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                navigationIcon = {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE8F5E9),
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.TAMIL) role.titleTa else role.titleEn,
                            color = Color(0xFF1B4D2E),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                },
                actions = {
                    // Privacy Policy
                    IconButton(
                        onClick = { showPrivacyPolicyModal = true },
                        modifier = Modifier.testTag("privacy_policy_button")
                    ) {
                        Icon(
                            Icons.Default.VerifiedUser,
                            contentDescription = "Privacy Policy",
                            tint = Color(0xFF1B4D2E)
                        )
                    }

                    // Language Switcher
                    IconButton(
                        onClick = { viewModel.toggleLanguage() },
                        modifier = Modifier.testTag("language_switch_button")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE8F5E9),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (language == AppLanguage.TAMIL) "EN" else "தமிழ்",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B4D2E)
                                )
                            }
                        }
                    }

                    // Bluetooth Status & Simulator trigger
                    IconButton(
                        onClick = { showPrinterModal = true },
                        modifier = Modifier.testTag("printer_modal_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Print,
                                contentDescription = "Thermal Printer",
                                tint = when (printerState) {
                                    is PrinterConnectionState.Connected -> Color(0xFF2E7D32)
                                    is PrinterConnectionState.Printing -> Color(0xFFE65100)
                                    else -> Color(0xFF1976D2)
                                }
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (printerState) {
                                            is PrinterConnectionState.Connected -> Color(0xFF00E676)
                                            is PrinterConnectionState.Printing -> Color(0xFFFF9100)
                                            else -> Color(0xFF29B6F6)
                                        }
                                    )
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                // 1. Store
                NavigationBarItem(
                    selected = selectedScreenIndex == 0,
                    onClick = { selectedScreenIndex = 0 },
                    icon = { Icon(Icons.Default.Storefront, contentDescription = "Store") },
                    label = { Text(if (language == AppLanguage.TAMIL) "அங்காடி" else "Store", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF1B4D2E), indicatorColor = Color(0xFFD4EED6))
                )

                // 2. Orders Board
                NavigationBarItem(
                    selected = selectedScreenIndex == 1,
                    onClick = { selectedScreenIndex = 1 },
                    icon = {
                        if (pendingCount > 0) {
                            BadgedBox(badge = { Badge { Text("$pendingCount") } }) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = "Orders")
                            }
                        } else {
                            Icon(Icons.Default.ReceiptLong, contentDescription = "Orders")
                        }
                    },
                    label = { Text(if (language == AppLanguage.TAMIL) "ஆர்டர்கள்" else "Orders", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF1B4D2E), indicatorColor = Color(0xFFD4EED6))
                )

                // 3. Harvest Sheet
                NavigationBarItem(
                    selected = selectedScreenIndex == 2,
                    onClick = { selectedScreenIndex = 2 },
                    icon = { Icon(Icons.Default.Grass, contentDescription = "Harvest") },
                    label = { Text(if (language == AppLanguage.TAMIL) "அறுவடை" else "Harvest", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF1B4D2E), indicatorColor = Color(0xFFD4EED6))
                )

                // 4. Apartment Consolidator
                NavigationBarItem(
                    selected = selectedScreenIndex == 3,
                    onClick = { selectedScreenIndex = 3 },
                    icon = { Icon(Icons.Default.Apartment, contentDescription = "Apartments") },
                    label = { Text(if (language == AppLanguage.TAMIL) "அடுக்ககம்" else "Routes", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF1B4D2E), indicatorColor = Color(0xFFD4EED6))
                )

                // 5. Operations & Hardware Hub
                NavigationBarItem(
                    selected = selectedScreenIndex == 4,
                    onClick = { selectedScreenIndex = 4 },
                    icon = { Icon(Icons.Default.Tune, contentDescription = "Settings") },
                    label = { Text(if (language == AppLanguage.TAMIL) "மேலாண்மை" else "Hardware", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF1B4D2E), indicatorColor = Color(0xFFD4EED6))
                )
            }
        },
        floatingActionButton = {
            if (lastPrintedBitmap != null && selectedScreenIndex != 4) {
                FloatingActionButton(
                    onClick = { showPrinterModal = true },
                    containerColor = Color(0xFF2E6B34),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.testTag("floating_printer_fab")
                ) {
                    Icon(Icons.Default.Print, contentDescription = "Open 58mm Printer Paper Roll")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedScreenIndex) {
                0 -> StoreOrderingScreen(
                    viewModel = viewModel,
                    onOpenReceipt = { showPrinterModal = true }
                )
                1 -> OrderBoardScreen(
                    viewModel = viewModel,
                    onViewReceipt = { showPrinterModal = true }
                )
                2 -> HarvestSheetScreen(
                    viewModel = viewModel
                )
                3 -> ApartmentConsolidatorScreen(
                    viewModel = viewModel,
                    onViewReceipt = { showPrinterModal = true }
                )
                4 -> HardwareSettingsScreen(
                    viewModel = viewModel,
                    onOpenVirtualPrinter = { showPrinterModal = true },
                    onOpenPrivacyPolicy = { showPrivacyPolicyModal = true }
                )
            }
        }
    }

    // 58mm Virtual Thermal Printer Dialog
    if (showPrinterModal) {
        VirtualThermalPrinterDialog(
            bitmap = lastPrintedBitmap,
            order = lastPrintedOrder,
            printerManager = viewModel.printerManager,
            connectionState = printerState,
            onDismiss = { showPrinterModal = false }
        )
    }

    // Privacy Policy Dialog
    if (showPrivacyPolicyModal) {
        PrivacyPolicyDialog(
            language = language,
            onDismiss = { showPrivacyPolicyModal = false },
            onClearData = {
                viewModel.clearAllData()
                showPrivacyPolicyModal = false
            }
        )
    }
}
