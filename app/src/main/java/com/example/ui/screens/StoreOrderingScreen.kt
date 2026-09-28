package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CustomerEntity
import com.example.data.local.ProductEntity
import com.example.data.model.ProductCategory
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.FarmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreOrderingScreen(
    viewModel: FarmViewModel,
    onOpenReceipt: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    val cartMap by viewModel.cartMap.collectAsState()
    val cartLines by viewModel.cartLines.collectAsState()
    val cartSubtotal by viewModel.cartSubtotal.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val language by viewModel.language.collectAsState()

    var selectedCategory by remember { mutableStateOf<ProductCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showCartSheet by remember { mutableStateOf(false) }

    val filteredProducts = remember(products, selectedCategory, searchQuery) {
        products.filter { p ->
            val matchCat = selectedCategory == null || p.category == selectedCategory
            val matchQuery = searchQuery.isBlank() ||
                    p.nameEn.contains(searchQuery, ignoreCase = true) ||
                    p.nameTa.contains(searchQuery, ignoreCase = true)
            matchCat && matchQuery
        }
    }

    val totalItemsCount = remember(cartMap) { cartMap.values.sum() }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Farm Hero Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF1B4D2E), Color(0xFF2E7D32))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (language == AppLanguage.TAMIL) "சோலைவனம் இயற்கை அங்காடி" else "SOLAIVANAM ORGANIC STORE",
                                color = Color.White,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0x33FFFFFF)
                            ) {
                                Text(
                                    text = "100% Organic",
                                    color = Color(0xFFFFD54F),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (language == AppLanguage.TAMIL)
                                "பண்ணையில் இருந்து உங்கள் இல்லத்திற்கு நேரடி இயற்கை வரவு. 58mm ப்ளூடூத் பில் பிரிண்டர் ஒருங்கிணைப்புடன்."
                            else
                                "Farm-to-Home organic harvest delivered fresh. Bluetooth thermal billing enabled.",
                            color = Color(0xFFE8F5E9),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0x22FFFFFF)
                            ) {
                                Text(
                                    text = "🚚 Apartment Bulk Waiver",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = Color(0x22FFFFFF)
                            ) {
                                Text(
                                    text = "🌿 Early Morning Harvest",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(if (language == AppLanguage.TAMIL) "கீரை, அரிசி, எண்ணெய் தேடுக..." else "Search greens, oils, rice...")
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Category Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text(if (language == AppLanguage.TAMIL) "அனைத்தும் (All)" else "All Products") }
                    )
                    ProductCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = {
                                Text("${cat.icon} ${if (language == AppLanguage.TAMIL) cat.taName else cat.enName}")
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.TAMIL) "பண்ணை விளைபொருட்கள் (${filteredProducts.size})" else "Farm Produce (${filteredProducts.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Chennai Delivery",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Product Items List
            items(filteredProducts, key = { it.id }) { product ->
                val quantity = cartMap[product.id] ?: 0
                ProductRowCard(
                    product = product,
                    quantity = quantity,
                    language = language,
                    onAdd = { viewModel.addToCart(product) },
                    onRemove = { viewModel.removeFromCart(product.id) }
                )
            }
        }

        // Floating Cart Summary Bar
        AnimatedVisibility(
            visible = totalItemsCount > 0,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCartSheet = true }
                    .testTag("floating_cart_bar"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B4D2E)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFD54F)
                        ) {
                            Text(
                                text = "$totalItemsCount",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (language == AppLanguage.TAMIL) "கூடை மொத்தம்" else "Cart Subtotal",
                                color = Color(0xFFC8E6C9),
                                fontSize = 11.sp
                            )
                            Text(
                                text = "₹${"%.2f".format(cartSubtotal)}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (language == AppLanguage.TAMIL) "ஆர்டர் செய்ய" else "View Cart & Pay",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }
    }

    // Cart & Checkout Bottom Sheet
    if (showCartSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCartSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            CartCheckoutSheet(
                viewModel = viewModel,
                customers = customers,
                language = language,
                onDismiss = { showCartSheet = false },
                onOrderPlaced = {
                    showCartSheet = false
                    onOpenReceipt()
                }
            )
        }
    }
}

@Composable
fun ProductRowCard(
    product: ProductEntity,
    quantity: Int,
    language: AppLanguage,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Badge
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = product.category.icon, fontSize = 26.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.nameTa,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.nameEn,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "₹${"%.0f".format(product.price)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color(0xFF1B4D2E)
                    )
                    Text(
                        text = " / ${product.unit}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (product.stock > 10) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    ) {
                        Text(
                            text = if (product.stock > 0) "${product.stock} in stock" else "Sold Out",
                            color = if (product.stock > 10) Color(0xFF2E7D32) else Color(0xFFC62828),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Quantity Stepper / Add button
            if (quantity == 0) {
                Button(
                    onClick = onAdd,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E6B34)),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    enabled = product.stock > 0
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (language == AppLanguage.TAMIL) "சேர்" else "Add", fontSize = 13.sp)
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFE8F5E9),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(2.dp)
                    ) {
                        IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = "Remove", tint = Color(0xFF1B4D2E), modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = "$quantity",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                        IconButton(onClick = onAdd, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color(0xFF1B4D2E), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartCheckoutSheet(
    viewModel: FarmViewModel,
    customers: List<CustomerEntity>,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onOrderPlaced: () -> Unit
) {
    val cartLines by viewModel.cartLines.collectAsState()
    val subtotal by viewModel.cartSubtotal.collectAsState()

    val name by viewModel.checkoutName.collectAsState()
    val phone by viewModel.checkoutPhone.collectAsState()
    val apartment by viewModel.checkoutApartment.collectAsState()
    val flatNo by viewModel.checkoutFlatNo.collectAsState()
    val paymentMode by viewModel.checkoutPaymentMode.collectAsState()
    val notes by viewModel.checkoutNotes.collectAsState()

    val deliveryFee = if (subtotal >= 400.0) 0.0 else 30.0
    val total = subtotal + deliveryFee

    val apartmentOptions = listOf(
        "Olympia Opaline",
        "Green Meadows",
        "Hiranandani Parks",
        "Prestige Bella Vista",
        "Adyar Enclave",
        "Individual Villa"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (language == AppLanguage.TAMIL) "ஆர்டர் கூடை & பில் தயாரிப்பு" else "Cart & Invoice Checkout",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Cart Items
            items(cartLines) { line ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${line.product.nameTa} (${line.product.nameEn})",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${line.quantity} ${line.product.unit} x ₹${"%.0f".format(line.product.price)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Text(
                        text = "₹${"%.2f".format(line.total)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }

            // Customer Autofill Quick Pick
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (language == AppLanguage.TAMIL) "வாடிக்கையாளர் தானியங்கு தேர்வு (1-Tap Autofill):" else "Quick Customer Autofill:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    customers.forEach { c ->
                        OutlinedButton(
                            onClick = { viewModel.autofillCustomer(c) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("${c.name} (${c.apartment})", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Customer Input Fields
            item {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { viewModel.checkoutName.value = it },
                    label = { Text(if (language == AppLanguage.TAMIL) "வாடிக்கையாளர் பெயர் (Name)*" else "Customer Name*") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { viewModel.checkoutPhone.value = it },
                    label = { Text(if (language == AppLanguage.TAMIL) "வாட்ஸ்அப் எண் (Phone)*" else "WhatsApp / Phone*") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Apartment Select Chips
            item {
                Text(
                    text = if (language == AppLanguage.TAMIL) "குடியிருப்பு (Apartment / Community):" else "Apartment / Community:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    apartmentOptions.forEach { apt ->
                        FilterChip(
                            selected = apartment == apt,
                            onClick = { viewModel.checkoutApartment.value = apt },
                            label = { Text(apt, fontSize = 12.sp) }
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = flatNo,
                    onValueChange = { viewModel.checkoutFlatNo.value = it },
                    label = { Text(if (language == AppLanguage.TAMIL) "வீட்டு எண் / பிளாட் (Flat / Door No)" else "Flat / Door No (e.g. T3 704)") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { viewModel.checkoutNotes.value = it },
                    label = { Text(if (language == AppLanguage.TAMIL) "டெலிவரி குறிப்பு (Delivery Notes)" else "Delivery Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Price Summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Subtotal", fontSize = 13.sp)
                    Text("₹${"%.2f".format(subtotal)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Delivery Fee (Apt Consolidation)", fontSize = 13.sp)
                    Text(
                        if (deliveryFee == 0.0) "FREE (>= ₹400)" else "₹${"%.2f".format(deliveryFee)}",
                        fontSize = 13.sp,
                        color = if (deliveryFee == 0.0) Color(0xFF2E7D32) else Color.Black
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total (மொத்தம்)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        "₹${"%.2f".format(total)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF1B4D2E)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Submit & Print Action
        Button(
            onClick = {
                viewModel.placeOrder {
                    onOrderPlaced()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("place_order_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E6B34)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Print, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (language == AppLanguage.TAMIL)
                    "ஆர்டர் பதிவு & 58mm ரசீது பிரிண்ட்"
                else
                    "Confirm Order & Print 58mm Receipt",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}
