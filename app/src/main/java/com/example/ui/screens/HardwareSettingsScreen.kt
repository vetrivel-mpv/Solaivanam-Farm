package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.OrderEntity
import com.example.data.local.ProductEntity
import com.example.data.model.OrderItem
import com.example.data.model.OrderStatus
import com.example.data.model.ProductCategory
import com.example.printer.BluetoothPrinterDevice
import com.example.printer.PrinterConnectionState
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.FarmViewModel
import com.example.ui.viewmodel.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HardwareSettingsScreen(
    viewModel: FarmViewModel,
    onOpenVirtualPrinter: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit = {}
) {
    val language by viewModel.language.collectAsState()
    val role by viewModel.role.collectAsState()
    val products by viewModel.products.collectAsState()
    val printerState by viewModel.printerManager.connectionState.collectAsState()
    val pairedDevices by viewModel.printerManager.pairedDevices.collectAsState()
    val isVirtualMode by viewModel.printerManager.isVirtualMode.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddProductDialog by remember { mutableStateOf(false) }

    // Admin PIN prompt state
    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    // OTP Auth mockup modal
    var showOtpDialog by remember { mutableStateOf(false) }
    var otpPhone by remember { mutableStateOf("9840123456") }
    var otpCode by remember { mutableStateOf("") }
    var otpVerified by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tab row for Hardware vs Inventory vs Auth
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("🖨️ " + if (language == AppLanguage.TAMIL) "பிரிண்டர்" else "Printer") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("📦 " + if (language == AppLanguage.TAMIL) "இருப்பு" else "Inventory") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("🔐 " + if (language == AppLanguage.TAMIL) "பாதுகாப்பு" else "Roles & Auth") }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedTab) {
            0 -> PrinterHardwareTab(
                viewModel = viewModel,
                printerState = printerState,
                pairedDevices = pairedDevices,
                isVirtualMode = isVirtualMode,
                language = language,
                onOpenVirtualPrinter = onOpenVirtualPrinter
            )
            1 -> InventoryManagementTab(
                products = products,
                language = language,
                onUpdateStock = { id, stock -> viewModel.updateStock(id, stock) },
                onToggleAvailability = { id, avail -> viewModel.toggleAvailability(id, avail) },
                onAddProduct = { showAddProductDialog = true }
            )
            2 -> RolesAndAuthTab(
                currentRole = role,
                language = language,
                onSelectRole = { newRole ->
                    if (newRole == UserRole.ADMIN) {
                        showPinDialog = true
                    } else {
                        viewModel.setRole(newRole)
                    }
                },
                onOpenOtp = { showOtpDialog = true },
                onOpenPrivacyPolicy = onOpenPrivacyPolicy
            )
        }
    }

    // Add Product Dialog
    if (showAddProductDialog) {
        AddNewProductDialog(
            language = language,
            onDismiss = { showAddProductDialog = false },
            onConfirm = { nameEn, nameTa, cat, price, unit, stock, descEn, descTa ->
                viewModel.addNewProduct(nameEn, nameTa, cat, price, unit, stock, descEn, descTa)
                showAddProductDialog = false
            }
        )
    }

    // Admin PIN Dialog
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = {
                showPinDialog = false
                pinInput = ""
                pinError = false
            },
            title = { Text(if (language == AppLanguage.TAMIL) "நிர்வாகி PIN உள்ளிடவும்" else "Enter Admin PIN") },
            text = {
                Column {
                    Text(
                        text = if (language == AppLanguage.TAMIL) "இயல்புநிலை PIN: 2026" else "Default Manager PIN: 2026",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            pinInput = it
                            pinError = false
                        },
                        label = { Text("PIN") },
                        isError = pinError,
                        singleLine = true
                    )
                    if (pinError) {
                        Text("Incorrect PIN. Use 2026.", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ok = viewModel.unlockAdmin(pinInput)
                        if (ok) {
                            showPinDialog = false
                            pinInput = ""
                        } else {
                            pinError = true
                        }
                    }
                ) {
                    Text("Unlock")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // WhatsApp OTP Dialog
    if (showOtpDialog) {
        AlertDialog(
            onDismissRequest = { showOtpDialog = false },
            title = { Text("WhatsApp OTP Login (வாட்ஸ்அப் சரிபார்ப்பு)") },
            text = {
                Column {
                    Text("Direct phone login for farm deliveries:", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = otpPhone,
                        onValueChange = { otpPhone = it },
                        label = { Text("Phone Number") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = { otpCode = it },
                        label = { Text("OTP (Use 8888)") },
                        singleLine = true
                    )
                    if (otpVerified) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("✅ Verified as Senthil Kumaran (Olympia Opaline)", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (otpCode == "8888" || otpCode.length >= 4) {
                            otpVerified = true
                            viewModel.userNotice.value = "WhatsApp verified! Welcome Solaivanam Customer"
                            showOtpDialog = false
                        }
                    }
                ) {
                    Text("Verify OTP")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOtpDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun PrinterHardwareTab(
    viewModel: FarmViewModel,
    printerState: PrinterConnectionState,
    pairedDevices: List<BluetoothPrinterDevice>,
    isVirtualMode: Boolean,
    language: AppLanguage,
    onOpenVirtualPrinter: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Status Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (printerState) {
                                            is PrinterConnectionState.Connected -> Color(0xFF00E676)
                                            is PrinterConnectionState.Printing -> Color(0xFFFFB300)
                                            else -> Color(0xFF1976D2)
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "58mm ESC/POS Thermal Engine",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        IconButton(onClick = { viewModel.printerManager.refreshPairedDevices() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Scan Bluetooth")
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = when (printerState) {
                            is PrinterConnectionState.Connected -> "Connected to ${printerState.deviceName} (${printerState.address})"
                            is PrinterConnectionState.Connecting -> "Connecting to ${printerState.deviceName}..."
                            is PrinterConnectionState.Printing -> "Rendering Canvas & rasterizing GS v 0 (${printerState.invoiceNo})..."
                            is PrinterConnectionState.Error -> "Printer error: ${printerState.message}"
                            PrinterConnectionState.Disconnected -> "Virtual Simulator Active (384-dot Tamil Typography)"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Virtual Paper Roll Simulator",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Show on-screen thermal paper tear",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Switch(
                            checked = isVirtualMode,
                            onCheckedChange = { viewModel.printerManager.toggleVirtualMode(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF2E6B34))
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Test Print Button
                    Button(
                        onClick = {
                            val sampleOrder = OrderEntity(
                                invoiceNo = "TEST-SV-58MM",
                                customerName = "Solaivanam Farm Test",
                                customerPhone = "9840123456",
                                apartment = "Olympia Opaline",
                                flatNo = "Demonstration Roll",
                                deliveryAddress = "58mm Niyama BT-58 / Standard ESC-POS",
                                items = listOf(
                                    OrderItem(1, "Sirukeerai", "சிறுகீரை", 30.0, "கட்டு", 2),
                                    OrderItem(12, "Chekku Nallennai", "மரச்செக்கு நல்லெண்ணெய்", 260.0, "500 ml", 1)
                                ),
                                subtotal = 320.0,
                                deliveryFee = 0.0,
                                totalAmount = 320.0,
                                status = OrderStatus.PENDING,
                                paymentMode = "Test Voucher",
                                notes = "Canvas-to-Receipt Tamil Typography Verified"
                            )
                            viewModel.printOrderReceipt(sampleOrder)
                            onOpenVirtualPrinter()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_print_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E6B34)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Print Test 58mm Receipt (தமிழ் ரசீது)")
                    }
                }
            }
        }

        // Paired Bluetooth Devices Section
        item {
            Text(
                text = "Paired Bluetooth POS Printers (புளூடூத் கருவிகள்):",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        items(pairedDevices) { device ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.printerManager.connectToDevice(device) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bluetooth, contentDescription = null, tint = Color(0xFF1976D2))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = device.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = device.address, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }

                    OutlinedButton(
                        onClick = { viewModel.printerManager.connectToDevice(device) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("Connect", fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF1E7))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Technical Note: Tamil Typography on ESC/POS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF1B4D2E)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Standard 58mm thermal printers lack Tamil Unicode font ROM. Solaivanam renders Tamil & English text onto a 384-dot Android Canvas, converts to 1-bit monochrome, and streams raster data via GS v 0. Flawless Tamil rendering on any 58mm printer (Niyama, TVS, Everycom, Pos-58).",
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = Color(0xFF2E4E35)
                    )
                }
            }
        }
    }
}

@Composable
fun InventoryManagementTab(
    products: List<ProductEntity>,
    language: AppLanguage,
    onUpdateStock: (Long, Int) -> Unit,
    onToggleAvailability: (Long, Boolean) -> Unit,
    onAddProduct: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (language == AppLanguage.TAMIL) "பண்ணை இருப்பு விவரம் (${products.size})" else "Farm Stock Levels (${products.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Button(
                    onClick = onAddProduct,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E6B34)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (language == AppLanguage.TAMIL) "புதிய பொருள்" else "Add Produce", fontSize = 12.sp)
                }
            }
        }

        items(products, key = { it.id }) { product ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${product.nameTa} (${product.nameEn})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "₹${"%.0f".format(product.price)} / ${product.unit} • ${product.category.enName}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    // Stock adjustments (+5 / -5)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onUpdateStock(product.id, (product.stock - 5).coerceAtLeast(0)) },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Text("-5", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFC62828))
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (product.stock > 10) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = "${product.stock}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = { onUpdateStock(product.id, product.stock + 5) },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Text("+5", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF2E7D32))
                        }

                        Switch(
                            checked = product.isAvailable,
                            onCheckedChange = { onToggleAvailability(product.id, it) },
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RolesAndAuthTab(
    currentRole: UserRole,
    language: AppLanguage,
    onSelectRole: (UserRole) -> Unit,
    onOpenOtp: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select Operational Role (பயனர் பொறுப்பு):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                UserRole.entries.forEach { r ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelectRole(r) }
                            .background(if (currentRole == r) Color(0xFFE8F5E9) else Color.Transparent)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${r.titleTa} (${r.titleEn})",
                                fontWeight = if (currentRole == r) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                            Text(
                                text = when (r) {
                                    UserRole.CUSTOMER -> "Place orders, select apartment, view catalog"
                                    UserRole.ADMIN -> "Full access to inventory, harvest sheet, PIN protected"
                                    UserRole.PACKER -> "Focused crate packing and delivery receipt printing"
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        if (currentRole == r) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF2E7D32))
                        }
                    }
                    HorizontalDivider()
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Authentication & Quick Login",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "WhatsApp OTP verification for community customers & Admin PIN protection for farm manager operations.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onOpenOtp,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)) // WhatsApp green
                ) {
                    Text("Simulate WhatsApp OTP Login")
                }
            }
        }

        // Google Play Store & Privacy Policy Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.VerifiedUser,
                        contentDescription = "Privacy Policy",
                        tint = Color(0xFF1B4D2E)
                    )
                    Text(
                        text = if (language == AppLanguage.TAMIL) "தனியுரிமைக் கொள்கை & பாதுகாப்பு" else "Google Play Policy & Privacy",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF1B4D2E)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (language == AppLanguage.TAMIL)
                        "SOLAIVANAM கூகுள் ப்ளே ஸ்டோர் வழிகாட்டுதல்களுக்கு முழுமையாக இணங்குகிறது. ப்ளூடூத் அச்சிட மட்டுமே பயன்படுகிறது (இருப்பிடம் அறியாது). அனைத்து தரவுகளும் உள்ளூர் சாதனத்தில் மட்டுமே இருக்கும்."
                    else
                        "SOLAIVANAM strictly adheres to Google Play developer program guidelines. Bluetooth is scoped to 58mm thermal printing (neverForLocation). Local-first Room DB with zero ad tracking.",
                    fontSize = 12.sp,
                    color = Color(0xFF2E4E35),
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onOpenPrivacyPolicy,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4D2E))
                ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (language == AppLanguage.TAMIL) "முழு கொள்கையை காண்க" else "View Privacy Policy")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewProductDialog(
    language: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (String, String, ProductCategory, Double, String, Int, String, String) -> Unit
) {
    var nameEn by remember { mutableStateOf("") }
    var nameTa by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ProductCategory.KEERAI) }
    var priceStr by remember { mutableStateOf("35") }
    var unit by remember { mutableStateOf("கட்டு (bunch)") }
    var stockStr by remember { mutableStateOf("30") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (language == AppLanguage.TAMIL) "புதிய விளைபொருள் சேர்க்க" else "Add New Produce") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nameTa,
                    onValueChange = { nameTa = it },
                    label = { Text("தமிழ் பெயர் (e.g. பொன்னாங்கண்ணி)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text("English Name (e.g. Ponnanganni Keerai)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("விலை (Price ₹)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("அலகு (Unit e.g. கட்டு / 1 kg / 500 ml)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = stockStr,
                    onValueChange = { stockStr = it },
                    label = { Text("ஆரம்ப கையிருப்பு (Initial Stock)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameEn.isNotBlank() && nameTa.isNotBlank()) {
                        val p = priceStr.toDoubleOrNull() ?: 30.0
                        val s = stockStr.toIntOrNull() ?: 25
                        onConfirm(nameEn, nameTa, category, p, unit, s, "", "")
                    }
                }
            ) {
                Text("சேர் (Add)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
