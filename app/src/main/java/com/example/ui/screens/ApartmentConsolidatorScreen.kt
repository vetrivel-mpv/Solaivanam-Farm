package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderStatus
import com.example.data.repository.ApartmentConsolidationGroup
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.FarmViewModel

@Composable
fun ApartmentConsolidatorScreen(
    viewModel: FarmViewModel,
    onViewReceipt: (com.example.data.local.OrderEntity) -> Unit
) {
    val apartmentGroups by viewModel.apartmentConsolidation.collectAsState()
    val language by viewModel.language.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (language == AppLanguage.TAMIL) "குடியிருப்பு டெலிவரி தொகுப்பு" else "Apartment Consolidator",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (language == AppLanguage.TAMIL)
                        "ஒரே அடுக்ககத்தின் ஆர்டர்களை ஒன்றாக இணைத்து டெலிவரி செய்யும் தொகுதி"
                    else
                        "Bundled deliveries sorted by apartment community",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFE8F5E9)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Apartment, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${apartmentGroups.size} Communities",
                        color = Color(0xFF2E7D32),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (apartmentGroups.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No active apartment bundles available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(apartmentGroups, key = { it.apartmentName }) { group ->
                    ApartmentGroupCard(
                        group = group,
                        language = language,
                        onPrintOrder = { order ->
                            viewModel.printOrderReceipt(order)
                            onViewReceipt(order)
                        },
                        onShareManifest = {
                            val sb = StringBuilder().apply {
                                append("🏢 *SOLAIVANAM அடுக்கக டெலிவரி பட்டியல்* 🏢\n")
                                append("குடியிருப்பு: *${group.apartmentName}*\n")
                                append("மொத்த ஆர்டர்கள்: ${group.orderCount} | வசூல் தொகை: ₹${"%.2f".format(group.totalAmount)}\n\n")
                                append("📦 *இணைக்கப்பட்ட பொருட்கள் (Crate Summary):*\n")
                                group.bundledItemsSummary.forEach { itm ->
                                    append("• $itm\n")
                                }
                                append("\n🚪 *வாடிக்கையாளர்கள் & வீடுகள்:*\n")
                                group.orders.forEachIndexed { i, ord ->
                                    append("${i + 1}. Flat ${ord.flatNo} - ${ord.customerName} (${ord.customerPhone}) - ₹${ord.totalAmount} [${ord.status.name}]\n")
                                }
                                append("\nசோலைவனம் ஆர்கானிக் ஃபார்ம், சென்னை")
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, sb.toString())
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Delivery Manifest"))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ApartmentGroupCard(
    group: ApartmentConsolidationGroup,
    language: AppLanguage,
    onPrintOrder: (com.example.data.local.OrderEntity) -> Unit,
    onShareManifest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Apartment, contentDescription = null, tint = Color(0xFF2E7D32))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = group.apartmentName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "${group.orderCount} Customer Orders Bundled",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFD4EED6)
                ) {
                    Text(
                        text = "₹${"%.0f".format(group.totalAmount)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF1B4D2E),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Produce Matrix Crate Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAF8))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = if (language == AppLanguage.TAMIL) "📦 இந்த குடியிருப்பிற்கு தேவையான மொத்த பொருட்கள்:" else "📦 Crate Aggregation Matrix:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B4D2E)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    group.bundledItemsSummary.take(4).forEach { item ->
                        Text(text = "• $item", fontSize = 12.sp)
                    }
                    if (group.bundledItemsSummary.size > 4) {
                        Text(
                            text = "+ ${group.bundledItemsSummary.size - 4} more items",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Flat by Flat List
            Text(
                text = "Flats & Customer Packages:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(4.dp))

            group.orders.forEach { ord ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${ord.flatNo} - ${ord.customerName}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${ord.items.size} items • ₹${"%.0f".format(ord.totalAmount)} (${ord.status.name})",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    IconButton(
                        onClick = { onPrintOrder(ord) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Print 58mm", tint = Color(0xFF2E6B34), modifier = Modifier.size(16.dp))
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action
            OutlinedButton(
                onClick = onShareManifest,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (language == AppLanguage.TAMIL) "ரூட் பட்டியலை பகிர்க (WhatsApp)" else "Share Delivery Route Manifest", fontSize = 12.sp)
            }
        }
    }
}
