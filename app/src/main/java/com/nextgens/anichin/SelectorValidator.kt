package com.nextgens.anichin

import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit

/**
 * Test script untuk memvalidasi selector CSS pada situs Anichin.
 * Jalankan script ini untuk mendapatkan feedback tentang selector mana yang bekerja.
 */

class SelectorValidator {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "https://www.anichin.id"

    /**
     * Fetch HTML dari URL dan return Jsoup Document
     */
    private fun fetchHtml(url: String): org.jsoup.nodes.Document? {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return null
            response.close()
            Jsoup.parse(body)
        } catch (e: Exception) {
            println("❌ Error fetching HTML: ${e.message}")
            null
        }
    }

    /**
     * Test selector untuk container anime
     */
    fun testCatalogSelectors() {
        println("\n=== Testing Catalog Page Selectors ===")
        val url = "$baseUrl/anime/page/1"

        val document = fetchHtml(url) ?: return

        val selectors = listOf(
            "article.post",
            "div.anime-item",
            "div.anime-card",
            "li.anime-item",
            "div.item",
            ".post-item"
        )

        selectors.forEach { selector ->
            val count = document.select(selector).size
            if (count > 0) {
                println("✅ '$selector' -> Found $count items")
            } else {
                println("❌ '$selector' -> No items found")
            }
        }
    }

    /**
     * Test selector untuk judul anime
     */
    fun testTitleSelectors() {
        println("\n=== Testing Title Selectors ===")
        val url = "$baseUrl/anime/page/1"

        val document = fetchHtml(url) ?: return
        val firstItem = document.selectFirst("article.post, div.anime-item, li.anime-item") ?: run {
            println("❌ Could not find any anime item")
            return
        }

        val titleSelectors = listOf(
            "h2 a",
            "h3 a",
            "h4 a",
            "a.title",
            "span.title",
            ".title a"
        )

        titleSelectors.forEach { selector ->
            val title = firstItem.selectFirst(selector)?.text()?.trim()
            if (!title.isNullOrBlank()) {
                println("✅ '$selector' -> Found: '$title'")
            } else {
                println("❌ '$selector' -> Not found")
            }
        }
    }

    /**
     * Test selector untuk URL anime
     */
    fun testUrlSelectors() {
        println("\n=== Testing URL Selectors ===")
        val url = "$baseUrl/anime/page/1"

        val document = fetchHtml(url) ?: return
        val firstItem = document.selectFirst("article.post, div.anime-item, li.anime-item") ?: return

        val urlSelectors = listOf(
            "h2 a" to "href",
            "h3 a" to "href",
            "a.title" to "href",
            "a[href]" to "href"
        )

        urlSelectors.forEach { (selector, attr) ->
            val href = firstItem.selectFirst(selector)?.attr(attr)?.trim()
            if (!href.isNullOrBlank()) {
                println("✅ '$selector' -> Found: '$href'")
            } else {
                println("❌ '$selector' -> Not found")
            }
        }
    }

    /**
     * Test selector untuk gambar/poster
     */
    fun testImageSelectors() {
        println("\n=== Testing Image Selectors ===")
        val url = "$baseUrl/anime/page/1"

        val document = fetchHtml(url) ?: return
        val firstItem = document.selectFirst("article.post, div.anime-item, li.anime-item") ?: return

        val imageSelectors = listOf(
            "img" to "src",
            "img.thumbnail" to "src",
            "img.thumb" to "src",
            "img[src]" to "src",
            "img" to "data-src"
        )

        imageSelectors.forEach { (selector, attr) ->
            val src = firstItem.selectFirst(selector)?.attr(attr)?.trim()
            if (!src.isNullOrBlank()) {
                println("✅ '$selector' (attr: $attr) -> Found: '$src'")
            }
        }
    }

    /**
     * Test selector untuk halaman detail
     */
    fun testDetailPageSelectors() {
        println("\n=== Testing Detail Page Selectors ===")

        // First, get an anime URL from catalog
        val catalogDoc = fetchHtml("$baseUrl/anime/page/1") ?: return
        val firstAnimeUrl = catalogDoc.selectFirst("h2 a, h3 a, a.title")?.attr("href") ?: run {
            println("❌ Could not find any anime URL")
            return
        }

        val fullUrl = if (firstAnimeUrl.startsWith("http")) firstAnimeUrl else "$baseUrl$firstAnimeUrl"
        println("Testing detail page: $fullUrl")

        val detailDoc = fetchHtml(fullUrl) ?: return

        // Test title selectors
        println("\nTitle selectors:")
        val titleSelectors = listOf("h1.entry-title", "h1.title", "h1")
        titleSelectors.forEach { selector ->
            val title = detailDoc.selectFirst(selector)?.text()?.trim()
            if (!title.isNullOrBlank()) {
                println("✅ '$selector' -> Found: '$title'")
            }
        }

        // Test description selectors
        println("\nDescription selectors:")
        val descSelectors = listOf(
            "div.entry-content p",
            "div.synopsis p",
            "div.desc p",
            ".description p"
        )
        descSelectors.forEach { selector ->
            val desc = detailDoc.selectFirst(selector)?.text()?.trim()?.take(50)
            if (!desc.isNullOrBlank()) {
                println("✅ '$selector' -> Found: '$desc...'")
            }
        }

        // Test genre selectors
        println("\nGenre selectors:")
        val genreSelectors = listOf(
            "div.genre a",
            "span.genre a",
            "a.genre"
        )
        genreSelectors.forEach { selector ->
            val genres = detailDoc.select(selector).map { it.text() }
            if (genres.isNotEmpty()) {
                println("✅ '$selector' -> Found: ${genres.take(3)}")
            }
        }

        // Test status selectors
        println("\nStatus selectors:")
        val statusSelectors = listOf(
            "div.status",
            "span.status",
            ".status"
        )
        statusSelectors.forEach { selector ->
            val status = detailDoc.selectFirst(selector)?.text()?.trim()
            if (!status.isNullOrBlank()) {
                println("✅ '$selector' -> Found: '$status'")
            }
        }
    }

    /**
     * Test selector untuk episode list
     */
    fun testEpisodeSelectors() {
        println("\n=== Testing Episode List Selectors ===")

        // Get anime URL from catalog
        val catalogDoc = fetchHtml("$baseUrl/anime/page/1") ?: return
        val firstAnimeUrl = catalogDoc.selectFirst("h2 a, h3 a, a.title")?.attr("href") ?: return
        val fullUrl = if (firstAnimeUrl.startsWith("http")) firstAnimeUrl else "$baseUrl$firstAnimeUrl"

        val detailDoc = fetchHtml(fullUrl) ?: return

        val episodeSelectors = listOf(
            "a[href]",
            "li a",
            ".episode a",
            ".episodes a",
            "div.episode-list a"
        )

        episodeSelectors.forEach { selector ->
            val episodes = detailDoc.select(selector)
            if (episodes.isNotEmpty()) {
                val sample = episodes.take(3).map { it.text() }
                println("✅ '$selector' -> Found ${episodes.size} items. Sample: $sample")
            }
        }
    }

    /**
     * Test selector untuk iframe video
     */
    fun testVideoSourceSelectors() {
        println("\n=== Testing Video Source Selectors ===")

        // Get anime URL
        val catalogDoc = fetchHtml("$baseUrl/anime/page/1") ?: return
        val firstAnimeUrl = catalogDoc.selectFirst("h2 a, h3 a, a.title")?.attr("href") ?: return
        val animeUrl = if (firstAnimeUrl.startsWith("http")) firstAnimeUrl else "$baseUrl$firstAnimeUrl"

        val detailDoc = fetchHtml(animeUrl) ?: return

        // Get first episode
        val firstEpisodeUrl = detailDoc.selectFirst("a[href]")?.attr("href") ?: run {
            println("❌ Could not find episode URL")
            return
        }
        val episodeUrl = if (firstEpisodeUrl.startsWith("http")) firstEpisodeUrl else "$baseUrl$firstEpisodeUrl"
        println("Testing episode page: $episodeUrl")

        val episodeDoc = fetchHtml(episodeUrl) ?: return

        // Test iframe selectors
        println("\nIframe selectors:")
        val iframeSelectors = listOf(
            "iframe[src]",
            "iframe"
        )
        iframeSelectors.forEach { selector ->
            val iframes = episodeDoc.select(selector).map { it.attr("src") }
            if (iframes.isNotEmpty()) {
                println("✅ '$selector' -> Found ${iframes.size} iframes")
                iframes.take(2).forEach { println("   - $it") }
            }
        }

        // Test script tag for video URLs
        println("\nScript tag analysis:")
        val scripts = episodeDoc.select("script").map { it.data() }.joinToString(" ")
        val m3u8Urls = Regex("https?://[^\"'\\s<>]+\\.m3u8[^\"'\\s<>]*").findAll(scripts).map { it.value }.toList()
        val mp4Urls = Regex("https?://[^\"'\\s<>]+\\.mp4[^\"'\\s<>]*").findAll(scripts).map { it.value }.toList()

        if (m3u8Urls.isNotEmpty()) {
            println("✅ Found ${m3u8Urls.size} m3u8 URLs")
            m3u8Urls.take(2).forEach { println("   - $it") }
        } else {
            println("❌ No m3u8 URLs found")
        }

        if (mp4Urls.isNotEmpty()) {
            println("✅ Found ${mp4Urls.size} mp4 URLs")
            mp4Urls.take(2).forEach { println("   - $it") }
        } else {
            println("❌ No mp4 URLs found")
        }
    }

    /**
     * Run all tests
     */
    fun runAllTests() {
        println("\n" + "=".repeat(60))
        println("Anichin Selector Validator")
        println("=".repeat(60))

        try {
            testCatalogSelectors()
            testTitleSelectors()
            testUrlSelectors()
            testImageSelectors()
            testDetailPageSelectors()
            testEpisodeSelectors()
            testVideoSourceSelectors()
        } catch (e: Exception) {
            println("\n❌ Error during testing: ${e.message}")
            e.printStackTrace()
        }

        println("\n" + "=".repeat(60))
        println("Test complete! Update selectors in AnichinApi.kt based on results above.")
        println("=".repeat(60) + "\n")
    }
}

/**
 * Main function untuk menjalankan test
 */
fun main() {
    val validator = SelectorValidator()
    validator.runAllTests()
}
