package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.EmailDigestEntity
import com.example.data.local.entity.IndiaMarketEntity
import com.example.data.local.entity.NewsItemEntity
import com.example.data.local.entity.SectorInflationEntity
import com.example.ui.PrivaViewModel
import com.example.ui.theme.AlertRed
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.StarkGold
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RadarAndNewsScreen(
    viewModel: PrivaViewModel,
    initialTab: Int = 0,
    modifier: Modifier = Modifier
) {
    val marketItems by viewModel.marketItems.collectAsStateWithLifecycle()
    val allNews by viewModel.newsItems.collectAsStateWithLifecycle()
    val sectors by viewModel.sectorInflation.collectAsStateWithLifecycle()
    val emailDigests by viewModel.emailDigests.collectAsStateWithLifecycle()

    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 5)) }
    val tabs = listOf("Local City News", "India Market (₹)", "Cybersecurity", "Car Industry", "Email Radar", "Sector Inflation")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Clean Modern Tab Row with Scrollable support
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkSurface,
            contentColor = ElectricCyan,
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = ElectricCyan
                )
            },
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedTab) {
            0 -> LocalCityNewsTab(
                allNews = allNews,
                viewModel = viewModel
            )
            1 -> IndiaMarketTab(
                marketItems = marketItems,
                onRefresh = { viewModel.refreshMarketPrices() },
                onSpeak = { text -> viewModel.voiceSpeaker.speakInCharacter(text) }
            )
            2 -> NewsListTab(
                categoryName = "Global Cybersecurity",
                newsList = allNews.filter { it.category.equals("Cybersecurity", ignoreCase = true) || it.title.contains("cyber", ignoreCase = true) || it.title.contains("security", ignoreCase = true) },
                onSpeak = { news ->
                    viewModel.voiceSpeaker.speakInCharacter("${news.title}. ${news.summary} Butler AI Takeaway: ${news.aiTakeaway}")
                }
            )
            3 -> NewsListTab(
                categoryName = "Car & Automotive Industry",
                newsList = allNews.filter { it.category.equals("Car Industry", ignoreCase = true) || it.title.contains("EV", ignoreCase = true) || it.title.contains("Tata Motors", ignoreCase = true) || it.title.contains("Automotive", ignoreCase = true) || it.title.contains("Mahindra", ignoreCase = true) },
                onSpeak = { news ->
                    viewModel.voiceSpeaker.speakInCharacter("${news.title}. ${news.summary} Butler AI Takeaway: ${news.aiTakeaway}")
                }
            )
            4 -> RadarEmailTab(
                emails = emailDigests,
                viewModel = viewModel
            )
            5 -> SectorInflationTab(
                sectors = sectors,
                onNotify = { sector -> viewModel.triggerSectorInflationAlert(sector) }
            )
        }
    }
}

/**
 * Real-time Indian Financial Radar: Gold, Silver, Nifty 50, Sensex, and Key Indian Stocks
 */
@Composable
private fun IndiaMarketTab(
    marketItems: List<IndiaMarketEntity>,
    onRefresh: () -> Unit,
    onSpeak: (String) -> Unit
) {
    val metals = marketItems.filter { it.assetType == "Precious Metal" }
    val stocksAndIndices = marketItems.filter { it.assetType != "Precious Metal" }

    var calcWeightGrams by remember { mutableStateOf("10") }
    var calcMetalType by remember { mutableStateOf("Gold 24K") }

    val inrFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 2
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Market Status Header
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ElectricTeal.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(SuccessGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "INDIA FINANCIAL RADAR (₹ INR)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = "Real-time Gold, Silver, Nifty 50 & Indian Equities",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh Live Rates", tint = ElectricTeal)
                        }

                        IconButton(
                            onClick = {
                                val gold = metals.firstOrNull { it.symbol == "GOLD24K" }
                                val silver = metals.firstOrNull { it.symbol == "SILVER999" }
                                val nifty = stocksAndIndices.firstOrNull { it.symbol == "NIFTY50" }
                                val tata = stocksAndIndices.firstOrNull { it.symbol == "TATAMOTORS" }
                                onSpeak("Sir, current Indian market rates: Gold 24K is at ₹${gold?.priceInr ?: 76850}/10g, Silver is at ₹${silver?.priceInr ?: 93200}/kg, Nifty 50 trades at ${nifty?.priceInr ?: 25790.6} points, and Tata Motors is at ₹${tata?.priceInr ?: 988.5}.")
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Speak Rates", tint = ElectricCyan)
                        }
                    }
                }
            }
        }

        // Section 1: Gold & Silver Live Prices
        item {
            Text(
                text = "PRECIOUS METALS: GOLD & SILVER (INDIA SPOT & MCX)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = ElectricCyan,
                letterSpacing = 1.sp
            )
        }

        items(metals, key = { it.id }) { item ->
            MarketItemCard(item = item, inrFormat = inrFormat)
        }

        // Quick Metal Investment & GST Calculator
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Calculate, contentDescription = null, tint = ElectricTeal, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "India Gold & Silver Estimator (with 3% GST)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Gold 24K", "Gold 22K", "Silver 999").forEach { type ->
                            FilterChip(
                                selected = calcMetalType == type,
                                onClick = { calcMetalType = type },
                                label = { Text(type, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElectricCyan,
                                    selectedLabelColor = Color(0xFF003548),
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = calcWeightGrams,
                            onValueChange = { calcWeightGrams = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            label = { Text("Weight (Grams)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricTeal,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        val grams = calcWeightGrams.toDoubleOrNull() ?: 10.0
                        val ratePerGram = when (calcMetalType) {
                            "Gold 24K" -> (metals.find { it.symbol == "GOLD24K" }?.priceInr ?: 76850.0) / 10.0
                            "Gold 22K" -> (metals.find { it.symbol == "GOLD22K" }?.priceInr ?: 70450.0) / 10.0
                            else -> (metals.find { it.symbol == "SILVER999" }?.priceInr ?: 93200.0) / 1000.0
                        }

                        val baseCost = grams * ratePerGram
                        val totalWithGst = baseCost * 1.03

                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text(text = "Total Value (incl. 3% GST)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = inrFormat.format(totalWithGst),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = ElectricCyan
                            )
                            Text(text = "Base: ${inrFormat.format(baseCost)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Section 2: Indian Benchmark Stock Indices & Leading Equities
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "INDIAN INDICES & LEADING EQUITIES (NSE / BSE in ₹ INR)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = ElectricCyan,
                letterSpacing = 1.sp
            )
        }

        items(stocksAndIndices, key = { it.id }) { item ->
            MarketItemCard(item = item, inrFormat = inrFormat)
        }
    }
}

@Composable
private fun MarketItemCard(item: IndiaMarketEntity, inrFormat: NumberFormat) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (item.isPositive) SuccessGreen.copy(alpha = 0.3f) else AlertRed.copy(alpha = 0.3f),
                RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${item.marketStatus} • ${item.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (item.assetType == "Index") "%,.2f".format(item.priceInr) else "₹%,.2f".format(item.priceInr),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (item.isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = if (item.isPositive) SuccessGreen else AlertRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${if (item.isPositive) "+" else ""}${item.changePercent}% (${if (item.isPositive) "+" else ""}₹${item.changeAmount})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.isPositive) SuccessGreen else AlertRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceVariant)
                    .padding(10.dp)
            ) {
                Text(
                    text = item.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )
            }
        }
    }
}

/**
 * Filterable News List for Cybersecurity & Car Industry with British Butler Audio Readout
 */
@Composable
private fun NewsListTab(
    categoryName: String,
    newsList: List<NewsItemEntity>,
    onSpeak: (NewsItemEntity) -> Unit
) {
    val sdf = remember { SimpleDateFormat("h:mm a, MMM d", Locale.getDefault()) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = categoryName.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0F291E))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("VERIFIED FEED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                }
            }
        }

        if (newsList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No news updates recorded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        items(newsList, key = { it.id }) { news ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (categoryName.contains("Cyber", ignoreCase = true)) AlertRed.copy(alpha = 0.2f) else ElectricCyan.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = news.category.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (categoryName.contains("Cyber", ignoreCase = true)) AlertRed else ElectricCyan
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${news.source} • ${sdf.format(Date(news.timestamp))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { onSpeak(news) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Read aloud",
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = news.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = news.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceVariant)
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = ElectricTeal, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Butler AI Takeaway:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricTeal
                                )
                                Text(
                                    text = news.aiTakeaway,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dedicated Email Radar Tab directly inside Radar & News Screen
 */
@Composable
private fun RadarEmailTab(
    emails: List<EmailDigestEntity>,
    viewModel: PrivaViewModel
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var filterMode by remember { mutableStateOf("All") }

    val filteredList = remember(emails, filterMode) {
        when (filterMode) {
            "Missing" -> emails.filter { it.isMissingImportant && !it.isAddressed }
            "Resolved" -> emails.filter { it.isAddressed }
            else -> emails
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NeonIndigo.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NeonIndigo.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Mail, contentDescription = null, tint = NeonIndigo, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("EMAIL RADAR INTELLIGENCE", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Private On-Device Action Tracking", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        IconButton(
                            onClick = {
                                val count = emails.count { it.isMissingImportant && !it.isAddressed }
                                viewModel.voiceSpeaker.speakInCharacter("Sir, your Email Radar identifies $count critical missing items requiring your signature and confirmation.")
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Read aloud", tint = ElectricCyan)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color(0xFF003548), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Track Email", color = Color(0xFF003548), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.addEmailDigest(
                                    sender = "tax-compliance@incometax.gov.in",
                                    subject = "Statutory TDS Return & Form 26AS Annual Reconciliation Notice",
                                    snippet = "Please authenticate your tax credit schedule before the upcoming filing cut-off.",
                                    category = "Important Missing",
                                    summary = "Tax credit verification required for annual statutory compliance.",
                                    missingDetail = "Signed tax credit declaration document is missing."
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Restore Sample", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Filter chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "All" to "All (${emails.size})",
                    "Missing" to "Missing Items (${emails.count { it.isMissingImportant && !it.isAddressed }})",
                    "Resolved" to "Resolved (${emails.count { it.isAddressed }})"
                ).forEach { (key, label) ->
                    FilterChip(
                        selected = filterMode == key,
                        onClick = { filterMode = key },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricCyan,
                            selectedLabelColor = Color(0xFF003548),
                            containerColor = DarkSurfaceVariant,
                            labelColor = Color.White
                        )
                    )
                }
            }
        }

        if (filteredList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No email action items found.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Tap 'Track Email' or 'Restore Sample' to populate.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        items(filteredList, key = { it.id }) { email ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (email.isMissingImportant && !email.isAddressed) AlertRed.copy(alpha = 0.5f) else DarkCardBorder,
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NeonIndigo.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Mail, contentDescription = null, tint = NeonIndigo, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = email.sender, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(text = email.subject, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                            }
                        }

                        if (email.isMissingImportant) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AlertRed.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = "ACTION REQUIRED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AlertRed)
                            }
                        }
                    }

                    if (email.isMissingImportant && email.missingDetail.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF331417))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "❗ You are missing: ${email.missingDetail}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = AlertRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "AI Summary: ${email.aiSummary}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.markEmailAddressed(email.id, !email.isAddressed) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = ElectricTeal, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (email.isAddressed) "Resolved" else "Mark Addressed", fontSize = 11.sp, color = Color.White)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = { viewModel.triggerMissingEmailAlert(email) },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Alert Me", fontSize = 11.sp, color = ElectricCyan)
                            }

                            IconButton(
                                onClick = { viewModel.deleteEmailDigest(email.id) },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceVariant)
                            ) {
                                Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = AlertRed)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var sender by remember { mutableStateOf("compliance@auditors.in") }
        var subject by remember { mutableStateOf("Action Required: GST Input Credit Form Sign-off") }
        var snippet by remember { mutableStateOf("Document attached requires executive verification.") }
        var missing by remember { mutableStateOf("Missing authorized signature on Annexure 4.") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = DarkSurface,
            title = { Text("Track New Email in Radar", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = sender,
                        onValueChange = { sender = it },
                        label = { Text("Sender") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = missing,
                        onValueChange = { missing = it },
                        label = { Text("Missing Detail / Action Item") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addEmailDigest(
                            sender = sender,
                            subject = subject,
                            snippet = snippet,
                            category = "Important Missing",
                            summary = "$subject from $sender",
                            missingDetail = missing
                        )
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                ) {
                    Text("Add to Radar", color = Color(0xFF003548), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

@Composable
private fun SectorInflationTab(
    sectors: List<SectorInflationEntity>,
    onNotify: (SectorInflationEntity) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "WHICH SECTOR GONNA BE COSTLIER IN NEXT 6 MONTHS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
        }

        items(sectors, key = { it.id }) { sector ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AlertRed.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = sector.sectorName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AlertRed.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "+${sector.projectedChangePercent}% COSTLIER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AlertRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Drivers: ${sector.primaryDrivers}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "💡 Advice: ${sector.savingsAdvice}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElectricCyan
                        )
                    }
                }
            }
        }
    }
}

/**
 * Live Local City & Transit Headlines with AI Summaries & Audio Auto-Run
 */
@Composable
private fun LocalCityNewsTab(
    allNews: List<NewsItemEntity>,
    viewModel: PrivaViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isRefreshing by viewModel.isRefreshingNews.collectAsStateWithLifecycle()
    val autoRunVoice by viewModel.autoRunVoice.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf("All") }

    val filterOptions = listOf("All", "Local City", "Transit", "Tech & AI", "Business & Economy")
    val filteredList = remember(allNews, selectedFilter) {
        if (selectedFilter == "All") {
            allNews
        } else {
            allNews.filter { it.category.equals(selectedFilter, ignoreCase = true) }
        }
    }

    val sdf = remember { SimpleDateFormat("hh:mm a", Locale.ENGLISH) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Live News API Header Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ElectricCyan.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "LOCAL NEWS & AI SUMMARIES",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Live Inshorts & City Desk API • AI Executive Briefs",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Voice Auto-Run toggle pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (autoRunVoice) SuccessGreen.copy(alpha = 0.2f) else DarkSurfaceVariant)
                                .clickable { viewModel.toggleAutoRunVoice() }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Auto Run Voice",
                                    tint = if (autoRunVoice) SuccessGreen else Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (autoRunVoice) "VOICE: ON" else "VOICE: MUTED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (autoRunVoice) SuccessGreen else Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.refreshLocalNewsWithApi { list ->
                                    android.widget.Toast.makeText(context, "Refreshed ${list.size} live local news stories", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF003548), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Fetching API...", color = Color(0xFF003548), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            } else {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF003548), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Refresh News API", color = Color(0xFF003548), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                if (allNews.isNotEmpty()) {
                                    val topStory = allNews.first()
                                    viewModel.voiceSpeaker.speakInCharacter("Sir, top local news summary: ${topStory.title}. ${topStory.summary} Takeaway: ${topStory.aiTakeaway}")
                                } else {
                                    viewModel.voiceSpeaker.speakInCharacter("No local headlines cached yet, Sir. Initiating live network API sync.")
                                    viewModel.refreshLocalNewsWithApi()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Briefing", color = Color.White, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category Filter Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        filterOptions.forEach { cat ->
                            val isSel = selectedFilter == cat
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedFilter = cat },
                                label = { Text(cat, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElectricCyan.copy(alpha = 0.25f),
                                    selectedLabelColor = ElectricCyan,
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = Color.LightGray
                                )
                            )
                        }
                    }
                }
            }
        }

        if (filteredList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No news headlines found in this category.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        items(filteredList, key = { it.id }) { news ->
            val catColor = when (news.category.lowercase()) {
                "transit" -> ElectricTeal
                "tech & ai" -> NeonIndigo
                "business & economy" -> StarkGold
                else -> ElectricCyan
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(catColor.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = news.category.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = catColor
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${news.source} • ${sdf.format(Date(news.timestamp))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    viewModel.voiceSpeaker.speakInCharacter("${news.title}. ${news.summary} ${news.aiTakeaway}")
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Read aloud",
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = news.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = news.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceVariant)
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = ElectricTeal,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Butler AI Takeaway:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricTeal
                                )
                                Text(
                                    text = news.aiTakeaway,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

