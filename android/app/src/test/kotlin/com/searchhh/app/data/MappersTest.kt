package com.searchhh.app.data

import com.searchhh.app.data.model.SortOption
import com.searchhh.app.data.remote.dto.EntryDto
import com.searchhh.app.data.remote.dto.SearchResponseDto
import com.searchhh.app.data.remote.dto.toDomain
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MappersTest {

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    private val wireJson = """
        {
          "id": "svc-001",
          "title": "Campus Coffee Run",
          "subtitle": "Same-day delivery",
          "description": "Barista coffee.",
          "category": "food",
          "tags": ["coffee", "delivery"],
          "price": 28.0,
          "currency": "ZAR",
          "rating": 4.7,
          "image_url": null,
          "location": "Bloemfontein",
          "url": "https://example.com",
          "score": 0.91,
          "highlights": ["Campus Coffee Run"],
          "brand_new_backend_field": "ignored"
        }
    """.trimIndent()

    @Test
    fun `decodes snake_case fields and ignores unknowns`() {
        val dto = json.decodeFromString(EntryDto.serializer(), wireJson)
        assertEquals("svc-001", dto.id)
        assertNull(dto.imageUrl)
        assertEquals(28.0, dto.price!!, 0.001)
        assertEquals(listOf("coffee", "delivery"), dto.tags)
    }

    @Test
    fun `maps dto to domain with score and highlights`() {
        val entry = json.decodeFromString(EntryDto.serializer(), wireJson).toDomain()
        assertEquals("Campus Coffee Run", entry.title)
        assertEquals(0.91, entry.score, 0.001)
        assertEquals(listOf("Campus Coffee Run"), entry.highlights)
        assertEquals("ZAR 28", entry.priceLabel)
    }

    @Test
    fun `price label handles free and missing prices`() {
        assertEquals("Free", sampleEntry.copy(price = 0.0).priceLabel)
        assertEquals("—", sampleEntry.copy(price = null).priceLabel)
        assertFalse(sampleEntry.copy(price = null).hasPrice)
        assertTrue(sampleEntry.hasPrice)
    }

    @Test
    fun `search response maps to search page`() {
        val body = """
            {"query":"coffee","total":1,"took_ms":2.4,"source":"memory",
             "results":[$wireJson],"suggestions":["coffee"]}
        """.trimIndent()
        val page = json.decodeFromString(SearchResponseDto.serializer(), body).toDomain()
        assertEquals("coffee", page.query)
        assertEquals(1, page.total)
        assertEquals(2.4, page.tookMs, 0.001)
        assertFalse(page.fromCache)
        assertEquals("svc-001", page.results.first().id)
    }

    @Test
    fun `sort options expose api values`() {
        assertEquals("relevance", SortOption.RELEVANCE.apiValue)
        assertEquals("price_asc", SortOption.PRICE_ASC.apiValue)
        assertEquals(5, SortOption.entries.size)
    }
}
