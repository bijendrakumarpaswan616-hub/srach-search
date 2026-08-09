package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WikipediaResponse(
    val query: WikipediaQuery?
)

@JsonClass(generateAdapter = true)
data class WikipediaQuery(
    val search: List<WikipediaSearchItem>?
)

@JsonClass(generateAdapter = true)
data class WikipediaSearchItem(
    val title: String,
    val pageid: Long,
    val snippet: String,
    val timestamp: String
)
