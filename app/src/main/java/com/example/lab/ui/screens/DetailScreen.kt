@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.lab.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lab.data.Campings
import com.example.lab.utils.openInMaps
import com.example.lab.utils.openWebsite
import com.example.lab.viewmodel.FavoritesViewModel
import com.example.lab.ui.theme.AppTopBarColors
import androidx.compose.ui.graphics.Color.Companion.White

@Composable
fun CampingDetailScreen(
    camping: Campings?,
    favViewModel: FavoritesViewModel,
    onBack: () -> Unit,
    onFavoritesClick: () -> Unit
) {
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }

    val isFavorite by if (camping != null)
        favViewModel.isFavorite(camping.signatura).collectAsState(initial = false)
    else
        remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = AppTopBarColors(),
                title = { Text(camping?.nombre ?: "Detalle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (camping != null) {
                        IconButton(onClick = { favViewModel.toggleFavorite(camping, isFavorite) }) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = if (isFavorite) "Quitar favorito" else "Añadir favorito"
                            )
                        }
                    }
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(text = { Text("Abrir en Maps") }, onClick = {
                            camping?.let { openInMaps(context, it) }; menuExpanded = false
                        })
                        DropdownMenuItem(text = { Text("Abrir web") }, onClick = {
                            camping?.let { openWebsite(context, it.web) }; menuExpanded = false
                        })
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onFavoritesClick, containerColor = MaterialTheme.colorScheme.secondary) {
                Icon(Icons.Filled.Favorite, contentDescription = "Ver favoritos", tint = White)
            }
        }
    ) { padding ->
        if (camping == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Camping no encontrado", color = Color(0xFF0F1F1E))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Column(Modifier.padding(16.dp)) {
                            Text(camping.nombre, style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            if (camping.municipio.isNotBlank() || camping.provincia.isNotBlank())
                                Text("📍 ${camping.municipio} (${camping.provincia})",
                                    color = MaterialTheme.colorScheme.onSecondaryContainer)
                            if (camping.direccion.isNotBlank()) {
                                val addr = if (camping.cp != 0) "${camping.direccion} (${camping.cp})" else camping.direccion
                                Text(addr, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                            Spacer(Modifier.height(8.dp))
                            if (camping.distanceKm != null)
                                Text("📏 %.1f km".format(camping.distanceKm),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer)
                            else
                                Text("📏 Distancia no disponible",
                                    color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            DetailRow("Signatura", camping.signatura)
                            DetailRow("Categoría", camping.categoria)
                            DetailRow("Plazas", camping.plazas.toString())
                            DetailRow("Parcelas", camping.numParcelas.toString())
                            DetailRow("Periodo", camping.periodo)
                            DetailRow("Web", camping.web)
                            DetailRow("Email", camping.email)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(if (value.isBlank()) "-" else value, style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer)
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
    }
}