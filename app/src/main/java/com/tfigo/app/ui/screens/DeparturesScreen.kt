package com.tfigo.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tfigo.app.data.model.Departure
import com.tfigo.app.ui.components.DepartureCard
import com.tfigo.app.ui.components.formatStopType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeparturesScreen(
    stopName: String,
    stopCode: String?,
    stopType: String,
    departures: List<Departure>,
    isLoading: Boolean,
    isFavourite: Boolean,
    lastUpdated: String,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onToggleFavourite: () -> Unit
) {
    var activeFilter by remember { mutableStateOf<String?>(null) }
    val services = remember(departures) {
        departures.map { it.serviceNumber }.distinct().sorted()
    }
    val filteredDepartures = remember(departures, activeFilter) {
        if (activeFilter == null) departures
        else departures.filter { it.serviceNumber == activeFilter }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stopName, maxLines = 1)
                        stopCode?.let {
                            Text(
                                "Stop $it · ${formatStopType(stopType)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onToggleFavourite) {
                        Icon(
                            if (isFavourite) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (isFavourite) "Remove from favourites" else "Add to favourites"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onRefresh,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Service filter chips
            if (services.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = activeFilter == null,
                        onClick = { activeFilter = null },
                        label = { Text("All") }
                    )
                    services.forEach { service ->
                        FilterChip(
                            selected = activeFilter == service,
                            onClick = {
                                activeFilter = if (activeFilter == service) null else service
                            },
                            label = { Text(service) }
                        )
                    }
                }
            }

            when {
                isLoading && departures.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Loading departures...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                filteredDepartures.isEmpty() && !isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No departures",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                "No upcoming departures found.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            filteredDepartures.take(20),
                            key = { "${it.serviceID}_${it.scheduledDeparture}_${it.destination}" }
                        ) { departure ->
                            DepartureCard(departure = departure)
                        }

                        if (lastUpdated.isNotEmpty()) {
                            item {
                                Text(
                                    "Updated $lastUpdated",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .wrapContentWidth(Alignment.CenterHorizontally)
                                )
                            }
                        }

                        // Bottom spacer for FAB
                        item { Spacer(modifier = Modifier.height(72.dp)) }
                    }
                }
            }
        }
    }
}
