package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import coil.compose.AsyncImage
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.RechargeRequestEntity
import com.example.ui.components.LocationSelectorDialog
import com.example.ui.components.SafetyTipsCard
import com.example.ui.components.formatRupee
import com.example.ui.theme.BazaarGold
import com.example.ui.theme.BazaarGoldLight
import com.example.ui.theme.BazaarGreen
import com.example.ui.theme.BazaarGreenLight
import com.example.ui.theme.BazaarOrange
import com.example.ui.theme.BazaarOrangeDark
import com.example.ui.theme.BazaarOrangeLight
import com.example.ui.theme.BazaarTeal
import com.example.ui.theme.BazaarTealLight
import com.example.ui.theme.Red500
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.White
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.BazaarViewModel

@Composable
fun AccountScreen(
    viewModel: BazaarViewModel,
    modifier: Modifier = Modifier
) {
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val userPhone by viewModel.userPhone.collectAsStateWithLifecycle()
    val userCity by viewModel.userCity.collectAsStateWithLifecycle()
    val isProUser by viewModel.isProUser.collectAsStateWithLifecycle()
    val locations by viewModel.locations.collectAsStateWithLifecycle()
    val selectedLocation by viewModel.selectedLocation.collectAsStateWithLifecycle()

    var showLocationDialog by remember { mutableStateOf(false) }

    if (showLocationDialog) {
        LocationSelectorDialog(
            locations = locations,
            selectedLocation = selectedLocation,
            onSelectLocation = { viewModel.selectLocation(it) },
            onDismiss = { showLocationDialog = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 90.dp)
    ) {
        // Top App Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "My Profile & Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Meri Local Bazaar Account",
                    fontSize = 12.sp,
                    color = Slate500
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profile Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(BazaarOrange),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.take(1).uppercase(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = White
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = userName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified",
                            tint = BazaarTeal,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "+91 $userPhone",
                        fontSize = 13.sp,
                        color = Slate500
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (isProUser) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BazaarGold
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = White, modifier = Modifier.size(11.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("PRO MEMBER", fontSize = 9.sp, fontWeight = FontWeight.Black, color = White)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // PRO Upgrade Banner Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BazaarGoldLight),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { viewModel.navigateTo(AppScreen.PRO_MEMBERSHIP) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(BazaarGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = White, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Upgrade to PRO Seller",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "Get verified badge & top ad placement for ₹50",
                            fontSize = 11.sp,
                            color = Slate700
                        )
                    }
                }
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = BazaarGold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Options List Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column {
                AccountOptionRow(
                    icon = Icons.Default.ListAlt,
                    title = "My Posted Ads",
                    subtitle = "Manage, view & mark sold",
                    onClick = { viewModel.navigateTo(AppScreen.MY_ADS) }
                )
                HorizontalDivider(color = Slate200, modifier = Modifier.padding(horizontal = 16.dp))

                AccountOptionRow(
                    icon = Icons.Default.Favorite,
                    title = "Saved Ads",
                    subtitle = "Bookmarked items & wishlist",
                    onClick = { viewModel.navigateTo(AppScreen.FAVORITES) }
                )
                HorizontalDivider(color = Slate200, modifier = Modifier.padding(horizontal = 16.dp))

                AccountOptionRow(
                    icon = Icons.Default.LocationOn,
                    title = "Selected Location",
                    subtitle = userCity,
                    onClick = { showLocationDialog = true }
                )
                HorizontalDivider(color = Slate200, modifier = Modifier.padding(horizontal = 16.dp))

                AccountOptionRow(
                    icon = Icons.Default.AdminPanelSettings,
                    title = "Admin & Moderation Panel",
                    subtitle = "Review recharge requests & categories",
                    onClick = { viewModel.navigateTo(AppScreen.ADMIN_PANEL) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Safety Tips Section
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            SafetyTipsCard()
        }

        Spacer(modifier = Modifier.height(20.dp))

        // App Info & Version
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Meri Local Bazaar v2.0", fontSize = 12.sp, color = Slate400, fontWeight = FontWeight.SemiBold)
            Text("Empowering Local Indian Trade & Neighborhood Markets", fontSize = 11.sp, color = Slate400)
        }
    }
}

@Composable
private fun AccountOptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(BazaarOrangeLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = BazaarOrangeDark, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                Text(text = subtitle, fontSize = 11.sp, color = Slate500)
            }
        }
        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Slate400)
    }
}

@Composable
fun ProMembershipScreen(
    viewModel: BazaarViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var utrNumber by remember { mutableStateOf("") }
    val upiId = "merilocalbazaar@upi"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 60.dp)
    ) {
        // Top App Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo(AppScreen.ACCOUNT) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Slate700)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "PRO Membership Plan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Supercharge your sales with top visibility",
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // PRO Plan Pricing Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BazaarGold
                        ) {
                            Text("POPULAR PLAN", fontSize = 10.sp, fontWeight = FontWeight.Black, color = White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("PRO Seller Monthly", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("₹50", fontSize = 28.sp, fontWeight = FontWeight.Black, color = BazaarOrangeDark)
                        Text("/ 30 Days", fontSize = 11.sp, color = Slate500)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Slate200)
                Spacer(modifier = Modifier.height(16.dp))

                ProFeatureItem(text = "👑 Verified Golden PRO Seller Badge on Profile")
                ProFeatureItem(text = "🚀 10x More Views & Top Pinned Ads")
                ProFeatureItem(text = "📱 Direct WhatsApp Chat Button on all Listings")
                ProFeatureItem(text = "⚡ Instant Buyer Lead Notifications")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // UPI Payment Box with QR Code Scanner
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BazaarTealLight),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null, tint = BazaarTeal)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scan QR Code to Pay", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BazaarTeal)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Scan with Google Pay, PhonePe, Paytm, BHIM or any UPI app",
                    fontSize = 11.sp,
                    color = Slate600,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // QR Code Frame
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = White,
                    border = androidx.compose.foundation.BorderStroke(2.dp, BazaarTeal.copy(alpha = 0.3f)),
                    shadowElevation = 2.dp,
                    modifier = Modifier.size(190.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        val qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=220x220&margin=8&data=" +
                                Uri.encode("upi://pay?pa=${upiId.trim()}&pn=${Uri.encode("Meri Local Bazaar")}&am=50&cu=INR&tn=${Uri.encode("PRO Membership")}")
                        AsyncImage(
                            model = qrUrl,
                            contentDescription = "UPI Payment QR Code",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Accepted Apps & Amount Badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BazaarTeal.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "Pay ₹50",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BazaarTeal,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text("•", color = Slate400, fontSize = 10.sp)
                    Text("GPay", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4285F4))
                    Text("PhonePe", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5F259F))
                    Text("Paytm", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF00B9F5))
                    Text("BHIM", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("UPI ID to Pay", fontSize = 11.sp, color = Slate500)
                            Text(upiId, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate900)
                        }

                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("UPI ID", upiId)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "UPI ID copied: $upiId", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = BazaarOrange)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // UTR Input & Submit Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Submit Transaction UTR / Ref No.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "After paying ₹50, enter the 12-digit UTR from your UPI app receipt to activate.",
                    fontSize = 12.sp,
                    color = Slate500
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = utrNumber,
                    onValueChange = { utrNumber = it.filter { ch -> ch.isLetterOrDigit() }.take(16) },
                    placeholder = { Text("e.g. 412356789012") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("utr_input_field")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (utrNumber.length < 8) {
                            viewModel.showSnackbar("Please enter a valid 12-digit UPI UTR number")
                            return@Button
                        }
                        viewModel.submitRecharge("PRO Seller Plan (30 Days)", 50.0, utrNumber)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_utr_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = BazaarOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Activate PRO Membership", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = White)
                }
            }
        }
    }
}

@Composable
private fun ProFeatureItem(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = Slate700,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun AdminPanelScreen(
    viewModel: BazaarViewModel,
    modifier: Modifier = Modifier
) {
    val rechargeRequests by viewModel.rechargeRequests.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val listings by viewModel.filteredListings.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val isConnected by viewModel.isFirebaseConnected.collectAsStateWithLifecycle()

    var newCategoryName by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo(AppScreen.HOME) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Slate700)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Admin & Moderation Panel",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Realtime Database & Marketplace Moderation",
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats Overview
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminStatCard(title = "Total Ads", value = "${listings.size}", color = BazaarOrange, modifier = Modifier.weight(1f))
                    AdminStatCard(title = "Categories", value = "${categories.size}", color = BazaarTeal, modifier = Modifier.weight(1f))
                    AdminStatCard(title = "Recharges", value = "${rechargeRequests.size}", color = BazaarGold, modifier = Modifier.weight(1f))
                }
            }

            // Firebase Cloud Realtime Database Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BazaarTealLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (isSyncing) Icons.Default.CloudSync else Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = BazaarTeal,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Firebase Realtime DB",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BazaarTeal
                                    )
                                    Text(
                                        text = "localbazar-cff07",
                                        fontSize = 11.sp,
                                        color = Slate500
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isConnected) BazaarGreenLight else Red500.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = if (isConnected) "CONNECTED" else "OFFLINE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isConnected) BazaarGreen else Red500,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "URL: https://localbazar-cff07-default-rtdb.firebaseio.com",
                            fontSize = 11.sp,
                            color = Slate700
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.syncFirebaseData(silent = false) },
                            enabled = !isSyncing,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BazaarTeal)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSyncing) "Syncing with Cloud..." else "Sync Cloud Database Now",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = White
                            )
                        }
                    }
                }
            }

            // Category Management Section
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Manage Categories (${categories.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newCategoryName,
                                onValueChange = { newCategoryName = it },
                                placeholder = { Text("New category name...", fontSize = 13.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newCategoryName.isNotBlank()) {
                                        viewModel.addCategory(newCategoryName.trim(), "Tag")
                                        newCategoryName = ""
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BazaarOrange)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = White)
                            }
                        }
                    }
                }
            }

            // Pending Recharge Requests Section
            item {
                Text(
                    text = "PRO Recharge Requests",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            }

            if (rechargeRequests.isEmpty()) {
                item {
                    Text("No pending recharge requests.", fontSize = 13.sp, color = Slate500)
                }
            } else {
                items(rechargeRequests, key = { it.id }) { req ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
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
                                    text = if (req.userName.isNotBlank() && req.userName != "Unknown user") req.userName else "User",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                if (req.userEmail.isNotBlank()) {
                                    Text(text = req.userEmail, fontSize = 11.sp, color = Slate500)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = "${req.planName} • ${formatRupee(req.amount)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BazaarOrangeDark)
                                Text(text = "UTR: ${req.utrNumber}", fontSize = 11.sp, color = Slate700)
                                Text(text = "Status: ${req.status}", fontSize = 11.sp, color = if (req.status == "Approved") BazaarGreen else BazaarOrange)
                            }

                            if (req.status == "Pending") {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    IconButton(
                                        onClick = { viewModel.approveRecharge(req.id) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(BazaarGreenLight, CircleShape)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = "Approve", tint = BazaarGreen, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { viewModel.rejectRecharge(req.id) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Red500.copy(alpha = 0.1f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Reject", tint = Red500, modifier = Modifier.size(18.dp))
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

@Composable
private fun AdminStatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = color)
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Slate700)
        }
    }
}
