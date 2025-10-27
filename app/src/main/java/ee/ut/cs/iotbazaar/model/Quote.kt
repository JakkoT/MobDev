package ee.ut.cs.iotbazaar.model

import com.google.gson.annotations.SerializedName

data class Quote(
    @SerializedName("id")
    val id: Int,
    @SerializedName("quote")
    val quote: String,
    @SerializedName("author")
    val author: String
)

