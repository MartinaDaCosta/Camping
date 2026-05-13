package com.example.lab.navigation

object Routes {
    const val LIST = "camping_list"
    const val DETAIL = "camping_detail"
    const val FAVORITES = "camping_favorites"
    const val ARG_ID = "campingId"
    const val DETAIL_ROUTE = "$DETAIL/{$ARG_ID}"
    fun detailRoute(id: String) = "$DETAIL/$id"
}
