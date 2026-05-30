package com.example.data

import retrofit2.Response
import retrofit2.http.*

interface GasApiService {
    @GET
    suspend fun pullTasks(
        @Url url: String,
        @Query("action") action: String = "PULL"
    ): Response<GasSyncResponse>

    @POST
    suspend fun pushTasks(
        @Url url: String,
        @Body tasks: List<GasTask>
    ): Response<GasSyncResponse>
}
