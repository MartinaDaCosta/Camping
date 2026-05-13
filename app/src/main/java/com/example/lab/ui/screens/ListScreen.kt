@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.lab.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.lab.ui.components.CampingItem
import com.example.lab.utils.calculateDistanceKm
import com.example.lab.data.Campings
import com.example.lab.utils.geocodeCamping
import com.example.lab.utils.getUserLocation
import com.example.lab.ui.state.SortOption
import com.example.lab.ui.state.sortOptionLabel
import com.example.lab.viewmodel.FavoritesViewModel
import kotlin.collections.forEach
import com.example.lab.ui.theme.AppTopBarColors
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField

@Composable
fun CampingsListScreen(
    campings: List<Campings>,
    onCampingsUpdated: (List<Campings>) -> Unit,
    favViewModel: FavoritesViewModel,
    onCampingClick: (String) -> Unit,
    onFavoritesClick: () -> Unit
) {
    var expanded   by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf(SortOption.NAME_ASC) }
    var searchText by remember { mutableStateOf("") }
    val context = LocalContext.current

    var campingsWithDistance by remember { mutableStateOf(campings) }
    var userLocation by remember { mutableStateOf<Location?>(null) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasLocationPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        else getUserLocation(context) { location -> userLocation = location }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission && userLocation == null)
            getUserLocation(context) { location -> userLocation = location }
    }

    LaunchedEffect(userLocation) {
        val loc = userLocation ?: return@LaunchedEffect
        campings.forEach { camping ->
            geocodeCamping(context, camping) { lat, lon ->
                val distance = if (lat != null && lon != null)
                    calculateDistanceKm(loc.latitude, loc.longitude, lat, lon)
                else null
                val updatedList = campingsWithDistance.map {
                    if (it.signatura == camping.signatura)
                        it.copy(latitude = lat, longitude = lon, distanceKm = distance)
                    else it
                }
                campingsWithDistance = updatedList
                onCampingsUpdated(updatedList)
            }
        }
    }

    val favorites  by favViewModel.favorites.collectAsState(initial = emptyList())
    val favoriteIds = remember(favorites) { favorites.map { it.signatura }.toSet() }

    val sortedCampings = remember(campingsWithDistance, sortOption) {
        when (sortOption) {
            SortOption.NAME_ASC      -> campingsWithDistance.sortedBy { it.nombre.lowercase() }
            SortOption.NAME_DESC     -> campingsWithDistance.sortedByDescending { it.nombre.lowercase() }
            SortOption.PLACES_ASC    -> campingsWithDistance.sortedBy { it.plazas }
            SortOption.PLACES_DESC   -> campingsWithDistance.sortedByDescending { it.plazas }
            SortOption.CATEGORY_ASC  -> campingsWithDistance.sortedBy { it.categoria.lowercase() }
            SortOption.CATEGORY_DESC -> campingsWithDistance.sortedByDescending { it.categoria.lowercase() }
            SortOption.DISTANCE_ASC  -> campingsWithDistance.sortedBy { it.distanceKm ?: Float.MAX_VALUE }
            SortOption.DISTANCE_DESC -> campingsWithDistance.sortedByDescending { it.distanceKm ?: -1f }
        }
    }

    val filteredCampings = remember(sortedCampings, searchText) {
        if (searchText.isBlank()) {
            sortedCampings
        } else {
            sortedCampings.filter {
                it.nombre.contains(searchText, ignoreCase = true)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = AppTopBarColors(),
                title = {
                    Column {
                        Text("CAMPINGS GVA", fontWeight = FontWeight.Bold)
                        Text("Orden: ${sortOptionLabel(sortOption)}", style = MaterialTheme.typography.bodySmall)
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Sort menu")
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(text = { Text("Nombre (A→Z)") },           onClick = { sortOption = SortOption.NAME_ASC;      expanded = false })
                            DropdownMenuItem(text = { Text("Nombre (Z→A)") },           onClick = { sortOption = SortOption.NAME_DESC;     expanded = false })
                            DropdownMenuItem(text = { Text("Plazas (menor→mayor)") },   onClick = { sortOption = SortOption.PLACES_ASC;    expanded = false })
                            DropdownMenuItem(text = { Text("Plazas (mayor→menor)") },   onClick = { sortOption = SortOption.PLACES_DESC;   expanded = false })
                            DropdownMenuItem(text = { Text("Categoría (A→Z)") },        onClick = { sortOption = SortOption.CATEGORY_ASC;  expanded = false })
                            DropdownMenuItem(text = { Text("Categoría (Z→A)") },        onClick = { sortOption = SortOption.CATEGORY_DESC; expanded = false })
                            DropdownMenuItem(text = { Text("Distancia (cerca→lejos)") },onClick = { sortOption = SortOption.DISTANCE_ASC;  expanded = false })
                            DropdownMenuItem(text = { Text("Distancia (lejos→cerca)") },onClick = { sortOption = SortOption.DISTANCE_DESC; expanded = false })
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onFavoritesClick, containerColor = MaterialTheme.colorScheme.secondary) {
                Icon(Icons.Filled.Favorite, contentDescription = "Ver favoritos", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Buscar camping por nombre") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredCampings, key = { it.signatura }) { camping ->
                    CampingItem(
                        c = camping,
                        isFavorite = camping.signatura in favoriteIds,
                        onToggleFavorite = {
                            favViewModel.toggleFavorite(
                                camping,
                                camping.signatura in favoriteIds
                            )
                        },
                        onClick = { onCampingClick(camping.signatura) }
                    )
                }
            }
        }
    }
}