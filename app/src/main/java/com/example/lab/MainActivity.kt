package com.example.lab

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lab.ui.theme.LABTheme
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LABTheme {
                CampingsScreen()
                }
            }
        }
    }


// CLASE
data class Camping(
    val id: String,
    val nombre: String,
    val municipio: String,
    val categoria: String,
    val plazas: Int
)


// READ JSON

fun readJsonFromRaw(context: Context, resourceId: Int): String {
    val inputStream = context.resources.openRawResource(resourceId)
    return inputStream.bufferedReader().use { it.readText() }
}


fun getCampingList(context: Context): List<Camping> {

    val campingsList = mutableListOf<Camping>()
    val jsonString = readJsonFromRaw(context, R.raw.camping)

    try {
        val rootObject = JSONObject(jsonString)
        val resultObject = rootObject.getJSONObject("result")
        val recordsArray = resultObject.getJSONArray("records")

        for (i in 0 until recordsArray.length()) {

            val jsonObject = recordsArray.getJSONObject(i)

            val id = jsonObject.getInt("_id").toString()
            val nombre = jsonObject.getString("Nombre")
            val municipio = jsonObject.getString("Municipio")
            val categoria = jsonObject.getString("Categoria")
            val plazas = jsonObject.getInt("Plazas")

            campingsList.add(
                Camping(id, nombre, municipio, categoria, plazas)
            )
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }

    return campingsList
}


// UI

@Composable
fun CampingsScreen() {

    val context = LocalContext.current
    val campings = getCampingList(context)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEDEDED))
            .padding(16.dp)
    ) {

        Text(
            text = "Campings",
            fontSize = 22.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn {
            items(campings) { camping ->
                CampingItem(camping)
            }
        }

    }
}

@Composable
fun CampingItem(camping: Camping) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFDADADA)
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "ID: ${camping.id}"
            )

            Text(
                text = "Nombre: ${camping.nombre}",
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Municipio: ${camping.municipio}"
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Categoría: ${camping.categoria}"
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Plazas: ${camping.plazas}"
            )

            Spacer(modifier = Modifier.height(4.dp))


        }
    }
}
