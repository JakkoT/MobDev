package ee.ut.cs.iotbazaar.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton object to provide the Retrofit instance and API services.
 * Configures the base URL and JSON converter.
 */
object RetrofitClient {
    // Base URL for the API
    private const val BASE_URL = "https://dummyjson.com/"

    // Initialize Retrofit instance
    private val retrofit: Retrofit by lazy { // Lazy means it will be created only when first accessed
        // Build Retrofit with base URL and Gson converter
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // Add Gson converter for JSON serialization/deserialization
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Lazy-initialized instance of QuoteApiService.
     */
    val quoteApiService: QuoteApiService by lazy {
        // Create implementation of QuoteApiService using Retrofit
        retrofit.create(QuoteApiService::class.java)
    }
}
