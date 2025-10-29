package ee.ut.cs.iotbazaar.api

import ee.ut.cs.iotbazaar.model.Quote
import retrofit2.Call
import retrofit2.http.GET

// API service interface for fetching quotes
interface QuoteApiService {
    // GET request to fetch a random quote from "quotes/random" endpoint
    @GET("quotes/random")
    fun getRandomQuote(): Call<Quote>
}

