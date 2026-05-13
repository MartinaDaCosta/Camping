package com.example.lab.utils

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.lab.data.Campings

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