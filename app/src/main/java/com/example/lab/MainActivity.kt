@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.lab

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lab.network.RetrofitClient
import com.example.lab.network.toCampings
import com.example.lab.viewmodel.FavoritesViewModel
import com.example.lab.viewmodel.toCamping
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.launch
import java.util.Locale

/* =========================
   ROUTES
   ========================= */
object Routes {
    const val LIST = "camping_list"
    const val DETAIL = "camping_detail"
    const val FAVORITES = "camping_favorites"
    const val ARG_ID = "campingId"
    const val DETAIL_ROUTE = "$DETAIL/{$ARG_ID}"
    fun detailRoute(id: String) = "$DETAIL/$id"
}

/* =========================
   SORT
   ========================= */
enum class SortOption {
    NAME_ASC, NAME_DESC,
    PLACES_ASC, PLACES_DESC,
    CATEGORY_ASC, CATEGORY_DESC,
    DISTANCE_ASC, DISTANCE_DESC
}

private fun sortOptionLabel(option: SortOption): String = when (option) {
    SortOption.NAME_ASC      -> "Nombre (A→Z)"
    SortOption.NAME_DESC     -> "Nombre (Z→A)"
    SortOption.PLACES_ASC    -> "Plazas (↑)"
    SortOption.PLACES_DESC   -> "Plazas (↓)"
    SortOption.CATEGORY_ASC  -> "Categoría (A→Z)"
    SortOption.CATEGORY_DESC -> "Categoría (Z→A)"
    SortOption.DISTANCE_ASC  -> "Distancia (cerca→lejos)"
    SortOption.DISTANCE_DESC -> "Distancia (lejos→cerca)"
}

/* =========================
   THEME (AQUA)
   ========================= */
private val AquaGreen      = Color(0xFF4DB6AC)
private val AquaGreenDark  = Color(0xFF00897B)
private val AquaGreenLight = Color(0xFFB2DFDB)
private val AquaSurface    = Color(0xFFEAF7F6)
private val White          = Color(0xFFFFFFFF)

private val AquaColorScheme = lightColorScheme(
    primary              = AquaGreen,
    onPrimary            = White,
    secondary            = AquaGreenDark,
    onSecondary          = White,
    background           = AquaSurface,
    onBackground         = Color(0xFF0F1F1E),
    surface              = AquaSurface,
    onSurface            = Color(0xFF0F1F1E),
    primaryContainer     = AquaGreenLight,
    onPrimaryContainer   = Color(0xFF0B2B28),
    secondaryContainer   = Color(0xFFCDEBE8),
    onSecondaryContainer = Color(0xFF0B2B28)
)

@Composable
private fun AppTopBarColors() = TopAppBarDefaults.topAppBarColors(
    containerColor             = MaterialTheme.colorScheme.primary,
    titleContentColor          = MaterialTheme.colorScheme.onPrimary,
    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
    actionIconContentColor     = MaterialTheme.colorScheme.onPrimary
)

/* =========================
   MAIN ACTIVITY
   ========================= */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        // ✅ Splash — debe llamarse ANTES de super.onCreate
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // Mantiene el splash visible mientras cargamos datos
        var isLoading = true
        splashScreen.setKeepOnScreenCondition { isLoading }

        setContent {
            MaterialTheme(colorScheme = AquaColorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val scope = rememberCoroutineScope()

                    var campings by remember { mutableStateOf<List<Campings>>(emptyList()) }
                    var error    by remember { mutableStateOf<String?>(null) }
                    var loading  by remember { mutableStateOf(true) }

                    fun loadData() {
                        loading = true
                        error   = null
                        scope.launch {
                            try {
                                val response = RetrofitClient.service.getCampings()
                                campings = response.result.records.map { it.toCampings() }
                            } catch (e: Exception) {
                                error = "Error al cargar los campings:\n${e.message}"
                            } finally {
                                loading   = false
                                isLoading = false  // cierra el splash
                            }
                        }
                    }

                    LaunchedEffect(Unit) { loadData() }

                    when {
                        loading       -> LoadingScreen()
                        error != null -> ErrorScreen(message = error!!, onRetry = ::loadData)
                        else          -> AppNavGraph(campings)
                    }
                }
            }
        }
    }
}

/* =========================
   ⏳ LOADING SCREEN
   ========================= */
@Composable
fun LoadingScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = AquaGreen)
            Spacer(Modifier.height(16.dp))
            Text(
                "Cargando campings...",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF0F1F1E)
            )
        }
    }
}

/* =========================
   ❌ ERROR SCREEN
   ========================= */
@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text("⚠️", style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(16.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF0F1F1E))
            Spacer(Modifier.height(24.dp))
            Button(onClick = onRetry) { Text("Reintentar") }
        }
    }
}

/* =========================
   NAV GRAPH
   ========================= */
@Composable
fun AppNavGraph(campings: List<Campings>) {
    val navController = rememberNavController()
    val favViewModel: FavoritesViewModel = viewModel()

    var campingsWithDistance by remember { mutableStateOf(campings) }

    NavHost(navController = navController, startDestination = Routes.LIST) {

        composable(Routes.LIST) {
            CampingsListScreen(
                campings = campingsWithDistance,
                onCampingsUpdated = { updatedList -> campingsWithDistance = updatedList },
                favViewModel = favViewModel,
                onCampingClick = { id -> navController.navigate(Routes.detailRoute(id)) },
                onFavoritesClick = { navController.navigate(Routes.FAVORITES) }
            )
        }

        composable(
            route = Routes.DETAIL_ROUTE,
            arguments = listOf(navArgument(Routes.ARG_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val campingId = backStackEntry.arguments?.getString(Routes.ARG_ID)
            val selected  = campingsWithDistance.firstOrNull { it.signatura == campingId }
            CampingDetailScreen(
                camping = selected,
                favViewModel = favViewModel,
                onBack = { navController.popBackStack() },
                onFavoritesClick = { navController.navigate(Routes.FAVORITES) }
            )
        }

        composable(Routes.FAVORITES) {
            FavoritesScreen(
                favViewModel = favViewModel,
                onCampingClick = { id -> navController.navigate(Routes.detailRoute(id)) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

/* =========================
   LOCATION HELPERS
   ========================= */
@SuppressLint("MissingPermission")
fun getUserLocation(
    context: android.content.Context,
    onResult: (Location?) -> Unit
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
        .addOnSuccessListener { location -> onResult(location) }
        .addOnFailureListener { onResult(null) }
}

fun geocodeCamping(
    context: android.content.Context,
    camping: Campings,
    onResult: (Double?, Double?) -> Unit
) {
    if (!Geocoder.isPresent()) { onResult(null, null); return }

    val query = listOf(camping.direccion, camping.municipio, camping.provincia, "España")
        .filter { it.isNotBlank() }.joinToString(", ")

    val geocoder = Geocoder(context, Locale.getDefault())

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        geocoder.getFromLocationName(query, 1, object : Geocoder.GeocodeListener {
            override fun onGeocode(addresses: MutableList<android.location.Address>) {
                val address = addresses.firstOrNull()
                onResult(address?.latitude, address?.longitude)
            }
            override fun onError(errorMessage: String?) { onResult(null, null) }
        })
    } else {
        try {
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocationName(query, 1)
            val address = addresses?.firstOrNull()
            onResult(address?.latitude, address?.longitude)
        } catch (e: Exception) { onResult(null, null) }
    }
}

fun calculateDistanceKm(
    userLat: Double, userLon: Double,
    campingLat: Double, campingLon: Double
): Float {
    val results = FloatArray(1)
    Location.distanceBetween(userLat, userLon, campingLat, campingLon, results)
    return results[0] / 1000f
}

/* =========================
   SCREEN 1: LIST
   ========================= */
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
                Icon(Icons.Filled.Favorite, contentDescription = "Ver favoritos", tint = White)
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            items(sortedCampings, key = { it.signatura }) { camping ->
                CampingItem(
                    c = camping,
                    isFavorite = camping.signatura in favoriteIds,
                    onToggleFavorite = { favViewModel.toggleFavorite(camping, camping.signatura in favoriteIds) },
                    onClick = { onCampingClick(camping.signatura) }
                )
            }
        }
    }
}

/* =========================
   ITEM
   ========================= */
@Composable
fun CampingItem(c: Campings, isFavorite: Boolean, onToggleFavorite: () -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(c.nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.weight(1f))
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isFavorite) "Quitar favorito" else "Añadir favorito",
                        tint = if (isFavorite) Color(0xFFE53935) else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            if (c.municipio.isNotBlank() || c.provincia.isNotBlank())
                Text("📍 ${c.municipio} (${c.provincia})", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
            if (c.direccion.isNotBlank()) {
                val addr = if (c.cp != 0) "${c.direccion} (${c.cp})" else c.direccion
                Text(addr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SmallMetric("Plazas", c.plazas.toString())
                SmallMetric("Parcelas", c.numParcelas.toString())
                if (c.categoria.isNotBlank()) {
                    Column {
                        Text("Categoría", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                        val stars = mapOf("UNA ESTRELLA" to 1, "DOS ESTRELLAS" to 2,
                            "TRES ESTRELLAS" to 3, "CUATRO ESTRELLAS" to 4, "CINCO ESTRELLAS" to 5)
                        Text("⭐".repeat(stars[c.categoria.uppercase()] ?: 0),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            if (c.distanceKm != null)
                Text("📏 %.1f km".format(c.distanceKm), style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
            else
                Text("📏 Distancia no disponible", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
            if (c.periodo.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("🗓️ ${c.periodo}", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

@Composable
private fun SmallMetric(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

/* =========================
   SCREEN 2: DETAIL
   ========================= */
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

/* =========================
   SCREEN 3: FAVORITES
   ========================= */
@Composable
fun FavoritesScreen(
    favViewModel: FavoritesViewModel,
    onCampingClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val favorites by favViewModel.favorites.collectAsState(initial = emptyList())

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = AppTopBarColors(),
                title = { Text("Mis Favoritos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (favorites.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.FavoriteBorder, contentDescription = null,
                        modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(16.dp))
                    Text("No tienes campings favoritos", style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground)
                    Text("Pulsa ❤️ en cualquier camping para añadirlo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                items(favorites, key = { it.signatura }) { fav ->
                    CampingItem(
                        c = fav.toCamping(),
                        isFavorite = true,
                        onToggleFavorite = { favViewModel.toggleFavorite(fav.toCamping(), true) },
                        onClick = { onCampingClick(fav.signatura) }
                    )
                }
            }
        }
    }
}

/* =========================
   INTENTS
   ========================= */
fun openInMaps(context: android.content.Context, c: Campings) {
    val query = listOf(c.direccion, c.municipio, c.provincia)
        .filter { it.isNotBlank() }.joinToString(", ").ifBlank { c.nombre }
    val uri = Uri.parse("geo:0,0?q=${Uri.encode(query)}")
    try { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
    catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No hay app de mapas instalada", Toast.LENGTH_SHORT).show()
    }
}

fun openWebsite(context: android.content.Context, web: String) {
    val url = web.trim()
    if (url.isBlank()) { Toast.makeText(context, "Este camping no tiene web", Toast.LENGTH_SHORT).show(); return }
    val normalized = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
    try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(normalized))) }
    catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No hay navegador disponible", Toast.LENGTH_SHORT).show()
    }
}