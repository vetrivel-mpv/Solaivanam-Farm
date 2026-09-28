package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.viewmodel.AppLanguage

@Composable
fun PrivacyPolicyDialog(
    language: AppLanguage,
    onDismiss: () -> Unit,
    onClearData: () -> Unit
) {
    val context = LocalContext.current
    val isTamil = language == AppLanguage.TAMIL

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .testTag("privacy_policy_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFE8F5E9),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.VerifiedUser,
                                    contentDescription = "Privacy Shield",
                                    tint = Color(0xFF1B4D2E)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = if (isTamil) "தனியுரிமைக் கொள்கை" else "Privacy Policy",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF1B4D2E)
                            )
                            Text(
                                text = "SOLAIVANAM • சோலைவனம் v2.0",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_privacy_policy_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Effective Date
                    Surface(
                        color = Color(0xFFF1F8E9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isTamil)
                                "கடைசியாக புதுப்பிக்கப்பட்டது: செப்டம்பர் 2026 | Google Play கொள்கை இணக்கம்"
                            else
                                "Effective Date: September 2026 | Google Play Policy Compliant",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }

                    // Section 1: Introduction
                    PolicySection(
                        icon = Icons.Default.Info,
                        title = if (isTamil) "1. அறிமுகம் மற்றும் நோக்கம்" else "1. Introduction & Overview",
                        body = if (isTamil)
                            "சோலைவனம் (SOLAIVANAM) என்பது இயற்கை விவசாய பண்ணையிலிருந்து நுகர்வோர் இல்லங்களுக்கு நேரடி விநியோகம் மற்றும் ப்ளூடூத் ரசீது அச்சு அமைப்பாகும். உங்கள் தனிப்பட்ட தரவின் பாதுகாப்பை நாங்கள் பெரிதும் மதிக்கிறோம். இந்த ஆப் பயனர்களின் தனிப்பட்ட தகவல்களை விளம்பரதாரர்களுக்கோ அல்லது பிற மூன்றாம் தரப்பினருக்கோ விற்பனை செய்வதில்லை."
                        else
                            "SOLAIVANAM is a Farm-to-Home organic ordering and operations management application with integrated 58mm Bluetooth ESC/POS thermal printing. We are committed to transparency and safeguarding your personal information. We do not sell or monetize personal user data."
                    )

                    // Section 2: Data Collected
                    PolicySection(
                        icon = Icons.Default.AccountCircle,
                        title = if (isTamil) "2. சேகரிக்கப்படும் தகவல்கள்" else "2. Information We Collect",
                        body = if (isTamil)
                            "ஆர்டர் மேலாண்மை மற்றும் இல்ல விநியோகத்தை பூர்த்தி செய்ய கீழ்கண்ட தகவல்கள் மட்டுமே பயன்படுத்தப்படுகின்றன:\n" +
                                    "• வாடிக்கையாளர் பெயர் மற்றும் தொடர்பு தொலைபேசி எண்.\n" +
                                    "• குடியிருப்பின் பெயர் (Apartment), தொகுதி, கதவு எண் மற்றும் முகவரி.\n" +
                                    "• ஆர்டர் விவரங்கள், பொருட்கள் மற்றும் மொத்த கட்டண பட்டியல்."
                        else
                            "To process customer orders and optimize community deliveries, we collect:\n" +
                                    "• Customer Name and Phone Number (for dispatch and delivery coordination).\n" +
                                    "• Delivery Address & Apartment Community (to group route deliveries efficiently).\n" +
                                    "• Order History, product selections, and billing invoices."
                    )

                    // Section 3: Bluetooth & Hardware Permissions
                    PolicySection(
                        icon = Icons.Default.Bluetooth,
                        title = if (isTamil) "3. புளூடூத் மற்றும் சாதன அனுமதி" else "3. Bluetooth & Hardware Permissions",
                        body = if (isTamil)
                            "• BLUETOOTH_CONNECT & BLUETOOTH_SCAN (neverForLocation):\n" +
                                    "இவ்வனுமதி 58mm ESC/POS வெப்ப ரசீது பிரிண்டர்களுடன் (Niyama BT-58, Everycom, Zjiang) இணைத்து விவசாய அறுவடை பட்டியல் மற்றும் பில் ரசீதுகளை அச்சிட மட்டுமே பயன்படுகிறது.\n" +
                                    "• உங்கள் இருப்பிடத்தை (GPS/Location) நாங்கள் ஒருபோதும் கண்காணிப்பதோ அல்லது பதிவு செய்வதோ இல்லை."
                        else
                            "• BLUETOOTH_CONNECT & BLUETOOTH_SCAN (Flagged as 'neverForLocation'):\n" +
                                    "These permissions are strictly used to pair and transmit ESC/POS raster print bytes to 58mm Bluetooth thermal printers for generating delivery slips, Tamil language typography receipts, and batch harvest sheets.\n" +
                                    "• We DO NOT track, collect, or store your GPS location."
                    )

                    // Section 4: Data Storage & Security
                    PolicySection(
                        icon = Icons.Default.Storage,
                        title = if (isTamil) "4. தரவு சேமிப்பு & பாதுகாப்பு" else "4. Data Storage & Security",
                        body = if (isTamil)
                            "பயனரின் அனைத்து தகவல்களும் சாதனத்தில் உள்நாட்டில் உள்ள Room SQLite தரவுத்தளத்தில் மட்டுமே பாதுகாப்பாக சேமிக்கப்படுகின்றன. வெளிப்புற விளம்பர சேவையகங்களுக்கு எந்த தகவலும் பகிரப்படுவதில்லை."
                        else
                            "All order logs, customer profiles, and inventory statuses are persisted locally on-device using Android Room SQLite database. Data is not shared with external ad networks or commercial brokers."
                    )

                    // Section 5: Third-Party Services
                    PolicySection(
                        icon = Icons.Default.Share,
                        title = if (isTamil) "5. மூன்றாம் தரப்பு பயன்பாடுகள்" else "5. Third-Party Integrations",
                        body = if (isTamil)
                            "• WhatsApp பகிர்தல்: பயனர் விருப்பத்தின் பேரில் மட்டுமே ஆர்டர் சுருக்கம் மற்றும் ரசீது WhatsApp செயலி மூலம் பகிரப்படுகிறது.\n" +
                                    "• Google Identity: நிர்வாகி அல்லது வாடிக்கையாளர் உள்நுழைவுக்கான பாதுகாப்பான அங்கீகரிப்புக்கு மட்டுமே பயன்படுகிறது."
                        else
                            "• WhatsApp Share: Order summaries and digital receipt texts are only passed to the official WhatsApp application when initiated by the user.\n" +
                                    "• Google Identity Services: Facilitates secure single sign-on authentication without collecting unneeded personal scopes."
                    )

                    // Section 6: Data Retention & User Rights
                    PolicySection(
                        icon = Icons.Default.DeleteOutline,
                        title = if (isTamil) "6. பயனர் உரிமைகள் மற்றும் தரவு நீக்கம்" else "6. User Rights & Data Deletion",
                        body = if (isTamil)
                            "உங்கள் தகவல்களை எந்த நேரத்திலும் முழுமையாக நீக்க உங்களுக்கு உரிமை உண்டு. கீழே உள்ள 'Clear Local Data' பொத்தானை அழுத்தி சாதனத்தில் உள்ள அனைத்து தரவுகளையும் அழிக்கலாம், அல்லது எங்கள் ஆதரவு மின்னஞ்சலைத் தொடர்பு கொள்ளலாம்."
                        else
                            "You have the right to inspect or permanently erase all your stored data. You can tap 'Clear Local Data' at any time, or email the developer to request assistance with data removal."
                    )

                    // Section 7: Developer Contact
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isTamil) "தொடர்பு & கொள்கை அதிகாரி:" else "Developer & Contact Info:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "SOLAIVANAM Organic Initiative",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Email: vetrivelmpv02@gmail.com",
                                fontSize = 12.sp,
                                color = Color(0xFF1976D2)
                            )
                            Text(
                                text = "Package: com.aistudio.solaivanam.orgfm",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onClearData,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("clear_data_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F))
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isTamil) "தரவு நீக்கு" else "Clear Data", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("accept_privacy_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4D2E))
                    ) {
                        Text(if (isTamil) "ஏற்கப்பட்டது" else "Got It", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PolicySection(
    icon: ImageVector,
    title: String,
    body: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color(0xFF1B4D2E),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF1B4D2E)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = body,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp,
            modifier = Modifier.padding(start = 26.dp)
        )
    }
}
