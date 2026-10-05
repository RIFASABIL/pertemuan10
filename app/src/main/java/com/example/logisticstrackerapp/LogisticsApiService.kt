package com.example.logisticstrackerapp

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface LogisticsApiService {
    @GET("api/v1/tracking/{resi}")
    suspend fun getTrackingDetail(
        @Path("resi") resiNumber: String
    ): Response<TrackingResponse>
}
