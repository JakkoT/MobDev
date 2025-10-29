package ee.ut.cs.iotbazaar.model

import com.google.gson.annotations.SerializedName

// Data class representing a Quote fetched from the API
data class Quote(
    // SerializedName annotation maps JSON keys to Kotlin properties
    @SerializedName("id")
    val id: Int,
    @SerializedName("quote")
    val quote: String,
    @SerializedName("author")
    val author: String
)

