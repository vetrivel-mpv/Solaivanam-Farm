package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import com.example.data.local.OrderEntity
import com.example.data.model.OrderStatus
import com.example.printer.InvoiceExporter
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.FarmViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrderBoardScreen(
    viewModel: FarmViewModel,
    onViewReceipt: (OrderEntity) -> Unit
) {
    val orders by viewModel.orders.collectAsState()
    val language by viewModel.language.collectAsState()
    val context = LocalContext.current

    var selectedStatusFilter by remember { mutableStateOf<OrderStatus?>(null) }

    val filteredOrders = remember(orders, selectedStatusFilter) {
        if (selectedStatusFilter == null) orders else orders.filter { it.status == selectedStatusFilter }
    }

    val pendingCount = remember(orders) { orders.count { it.status == OrderStatus.PENDING } }
    val packedCount = remember(orders) { orders.count { it.status == OrderStatus.PACKED } }
    val deliveredCount = remember(orders) { orders.count { it.status == OrderStatus.DELIVERED } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Order Board Summary Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusStatCard(
                title = if (language == AppLanguage.TAMIL) "புதியவை" else "Pending",
                count = pendingCount,
                color = Color(0xFFE65100),
                modifier = Modifier.weight(1f)
            )
            StatusStatCard(
                title = if (language == AppLanguage.TAMIL) "பேக் ஆனது" else "Packed",
                count = packedCount,
                color = Color(0xFF1976D2),
                modifier = Modifier.weight(1f)
            )
            StatusStatCard(
                title = if (language == AppLanguage.TAMIL) "முடிந்தது" else "Delivered",
                count = deliveredCount,
                color = Color(0xFF2E7D32),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedStatusFilter == null,
                onClick = { selectedStatusFilter = null },
                label = { Text("All (${orders.size})", fontSize = 12.sp) }
            )
            FilterChip(
                selected = selectedStatusFilter == OrderStatus.PENDING,
                onClick = { selectedStatusFilter = OrderStatus.PENDING },
                label = { Text("Pending ($pendingCount)", fontSize = 12.sp) }
            )
            FilterChip(
                selected = selectedStatusFilter == OrderStatus.PACKED,
                onClick = { selectedStatusFilter = OrderStatus.PACKED },
                label = { Text("Packed ($packedCount)", fontSize = 12.sp) }
            )
            FilterChip(
                selected = selectedStatusFilter == OrderStatus.DELIVERED,
                onClick = { selectedStatusFilter = OrderStatus.DELIVERED },
                label = { Text("Delivered ($deliveredCount)", fontSize = 12.sp) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (language == AppLanguage.TAMIL) "இந்த பிரிவில் ஆர்டர்கள் இல்லை" else "No orders in this status",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredOrders, key = { it.id }) { order ->
                    OrderBoardCard(
                        order = order,
                        language = language,
                        onPrint = {
                            viewModel.printOrderReceipt(order)
                            onViewReceipt(order)
                        },
                        onShare = {
                            InvoiceExporter.shareOrder(context, order)
                        },
                        onPrintPdf = {
                            InvoiceExporter.printPdfInvoice(context, order)
                        },
                        onCall = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${order.customerPhone}"))
                            context.startActivity(intent)
                        },
                        onUpdateStatus = { nextStatus ->
                            viewModel.updateOrderStatus(order.id, nextStatus)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StatusStatCard(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "$count", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = title, fontSize = 11.sp, color = color, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun OrderBoardCard(
    order: OrderEntity,
    language: AppLanguage,
    onPrint: () -> Unit,
    onShare: () -> Unit,
    onPrintPdf: () -> Unit,
    onCall: () -> Unit,
    onUpdateStatus: (OrderStatus) -> Unit
) {
    val statusColor = when (order.status) {
        OrderStatus.PENDING -> Color(0xFFE65100)
        OrderStatus.PACKED -> Color(0xFF1976D2)
        OrderStatus.OUT_FOR_DELIVERY -> Color(0xFF8E24AA)
        OrderStatus.DELIVERED -> Color(0xFF2E7D32)
        OrderStatus.CANCELLED -> Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_card_${order.invoiceNo}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Invoice No + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.invoiceNo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(order.orderTimestamp)),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (language == AppLanguage.TAMIL) order.status.taName else order.status.enName,
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Customer & Apartment
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.customerName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "🏢 ${order.apartment} • ${order.flatNo}",
                        fontSize = 12.sp,
                        color = Color(0xFF1B4D2E),
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(onClick = onCall, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Phone, contentDescription = "Call Customer", tint = Color(0xFF2E6B34))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Items Preview
            order.items.forEach { itm ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "• ${itm.quantity}x ${itm.nameTa} (${itm.nameEn})",
                        fontSize = 13.sp,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "₹${"%.2f".format(itm.totalAmount)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Amount & Payment Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pay: ${order.paymentMode}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "Total: ₹${"%.2f".format(order.totalAmount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF1B4D2E)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 1-Tap Thermal Print
                Button(
                    onClick = onPrint,
                    modifier = Modifier.weight(1.3f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E6B34)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (language == AppLanguage.TAMIL) "பிரிண்ட்" else "Print 58mm", fontSize = 12.sp)
                }

                // Status Transition Button
                if (order.status == OrderStatus.PENDING) {
                    OutlinedButton(
                        onClick = { onUpdateStatus(OrderStatus.PACKED) },
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (language == AppLanguage.TAMIL) "பேக் செய்" else "Mark Packed", fontSize = 12.sp)
                    }
                } else if (order.status == OrderStatus.PACKED) {
                    OutlinedButton(
                        onClick = { onUpdateStatus(OrderStatus.DELIVERED) },
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (language == AppLanguage.TAMIL) "டெலிவரி செய்" else "Delivered", fontSize = 12.sp)
                    }
                }

                IconButton(onClick = onShare, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.outline)
                }

                IconButton(onClick = onPrintPdf, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Download, contentDescription = "PDF", tint = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}
