package ee.ut.cs.iotbazaar.api

import ee.ut.cs.iotbazaar.model.Quote
import retrofit2.Call
import retrofit2.http.GET

interface QuoteApiService {
    @GET("quotes/random")
    fun getRandomQuote(): Call<Quote>
}

