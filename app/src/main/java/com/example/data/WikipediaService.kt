package com.example.data

import retrofit2.http.GET
import retrofit2.http.Query

interface WikipediaService {
    @GET("w/api.php?action=query&list=search&utf8=&format=json")
    suspend fun search(@Query("srsearch") query: String): WikipediaResponse
}
