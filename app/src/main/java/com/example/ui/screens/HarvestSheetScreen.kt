package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductCategory
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.FarmViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HarvestSheetScreen(
    viewModel: FarmViewModel
) {
    val harvestDemands by viewModel.harvestDemand.collectAsState()
    val checkedItems by viewModel.harvestCheckedItems.collectAsState()
    val language by viewModel.language.collectAsState()
    val context = LocalContext.current

    val totalItemsCount = remember(harvestDemands) { harvestDemands.sumOf { it.totalQuantityNeeded } }
    val keeraiBunches = remember(harvestDemands) {
        harvestDemands.filter { it.category == ProductCategory.KEERAI }.sumOf { it.totalQuantityNeeded }
    }
    val oilLiters = remember(harvestDemands) {
        harvestDemands.filter { it.category == ProductCategory.OILS }.sumOf { it.totalQuantityNeeded }
    }
    val grainsKg = remember(harvestDemands) {
        harvestDemands.filter { it.category == ProductCategory.GRAINS }.sumOf { it.totalQuantityNeeded }
    }

    val todayStr = remember {
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Harvest Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (language == AppLanguage.TAMIL) "பண்ணை அறுவடை பட்டியல்" else "Farm Harvest Demand Sheet",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Batch Date: $todayStr",
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
                    Icon(Icons.Default.Grass, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${checkedItems.size}/${harvestDemands.size} Ready",
                        color = Color(0xFF2E7D32),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Aggregate Metrics Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HarvestStatCard(
                label = if (language == AppLanguage.TAMIL) "கீரைகள்" else "Greens",
                value = "$keeraiBunches கட்டுகள்",
                icon = "🌿",
                modifier = Modifier.weight(1f)
            )
            HarvestStatCard(
                label = if (language == AppLanguage.TAMIL) "செக்கு எண்ணெய்" else "Oils",
                value = "$oilLiters அலகுகள்",
                icon = "🫒",
                modifier = Modifier.weight(1f)
            )
            HarvestStatCard(
                label = if (language == AppLanguage.TAMIL) "தானியங்கள்" else "Grains",
                value = "$grainsKg kg",
                icon = "🌾",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Checklist of Items to Harvest
        if (harvestDemands.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (language == AppLanguage.TAMIL) "தற்போது அறுவடை தேவை இல்லை (Pending orders empty)" else "No active harvest demand",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(harvestDemands, key = { it.productId }) { demand ->
                    val isChecked = checkedItems.contains(demand.productId)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleHarvestChecked(demand.productId) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isChecked) Color(0xFFF1F8E9) else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { viewModel.toggleHarvestChecked(demand.productId) },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2E6B34))
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = demand.nameTa,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "${demand.nameEn} • ${demand.orderCount} orders",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isChecked) Color(0xFFC8E6C9) else Color(0xFFFFECB3)
                            ) {
                                Text(
                                    text = "${demand.totalQuantityNeeded} ${demand.unit}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isChecked) Color(0xFF1B4D2E) else Color(0xFF8D6E63),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Share button to WhatsApp for Farm Crew
        Button(
            onClick = {
                val sb = StringBuilder().apply {
                    append("🌾 *SOLAIVANAM பண்ணை அறுவடை அறிக்கை* 🌾\n")
                    append("தேதி: $todayStr\n\n")
                    append("அறுவடை செய்ய வேண்டிய பொருட்கள்:\n")
                    harvestDemands.forEachIndexed { i, d ->
                        val check = if (checkedItems.contains(d.productId)) "✅" else "⏳"
                        append("$check ${i + 1}. ${d.nameTa} (${d.nameEn}) - *${d.totalQuantityNeeded} ${d.unit}* (${d.orderCount} orders)\n")
                    }
                    append("\nமொத்தம்: $totalItemsCount பொருட்கள்\n")
                    append("சோலைவனம் இயற்கை பண்ணை, செங்கல்பட்டு")
                }
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, sb.toString())
                }
                context.startActivity(Intent.createChooser(sendIntent, "Share Harvest Sheet via"))
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E6B34)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (language == AppLanguage.TAMIL) "அறுவடை பட்டியலை பகிர்க (WhatsApp)" else "Share Harvest Sheet with Crew",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun HarvestStatCard(
    label: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF1E7))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 20.sp)
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1B4D2E))
            Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
        }
    }
}
