package ee.ut.cs.iotbazaar.api

import ee.ut.cs.iotbazaar.model.Quote
import retrofit2.Call
import retrofit2.http.GET

/**
 * API service interface for fetching quotes.
 * Defined for use with Retrofit.
 */
interface QuoteApiService {
    /**
     * Fetches a random quote from the "quotes/random" endpoint.
     * @return A Call object yielding a [Quote].
     */
    @GET("quotes/random")
    fun getRandomQuote(): Call<Quote>
}
