package ee.ut.cs.iotbazaar.model

import com.google.gson.annotations.SerializedName

/**
 * Data class representing a Quote fetched from the external API.
 *
 * @property id The unique ID of the quote.
 * @property quote The text content of the quote.
 * @property author The author of the quote.
 */
data class Quote(
    // SerializedName annotation maps JSON keys to Kotlin properties
    @SerializedName("id")
    val id: Int,
    @SerializedName("quote")
    val quote: String,
    @SerializedName("author")
    val author: String
)
