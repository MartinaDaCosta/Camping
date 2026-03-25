package com.example.lab.network

import retrofit2.http.GET
import retrofit2.http.Query

interface CampingApiService {

    @GET("action/datastore_search")
    suspend fun getCampings(
        @Query("id") id: String = "2ddaf823-5da4-4459-aa57-5bfe9f9eb474"
    ): CampingApiResponse
}
