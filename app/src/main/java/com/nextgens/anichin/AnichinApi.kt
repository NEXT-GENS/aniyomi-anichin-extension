package com.nextgens.anichin

import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

class AnichinApi(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()
) {
    private val baseUrl = "https://www.anichin.id"

    fun fetchPopular(page: Int): List<Anime> {
        val url = "$baseUrl/anime/page/$page/?sort=popular"
        return parseCatalog(fetchHtml(url))
    }

    fun fetchLatest(page: Int): List<Anime> {
        val url = "$baseUrl/anime/page/$page/?sort=latest"
        return parseCatalog(fetchHtml(url))
    }

    fun fetchSearch(query: String, page: Int): List<Anime> {
        val encoded = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8.toString())
        val url = "$baseUrl/?s=$encoded&page=$page"
        return parseCatalog(fetchHtml(url))
    }

    fun fetchAnimeDetails(anime: Anime): Anime {
        val document = fetchHtml(anime.url)
        val title = document.selectFirst("h1.entry-title, h1.title, h1")?.text()?.trim() ?: anime.title
        val synopsis = document.selectFirst("div.entry-content p, div.synopsis p, div.desc p")?.text()?.trim() ?: anime.description
        val cover = document.selectFirst("img.attachment-post-thumbnail, img.thumb, img.poster")?.attr("src")
            ?: document.selectFirst("img")?.attr("src")
            ?: anime.coverUrl

        val genres = document.select("div.genre a, span.genre a, a.genre")
            .mapNotNull { it.textOrNull()?.trim() }
            .ifEmpty { anime.genre }

        val status = document.selectFirst("div.status, span.status, .status")?.text()?.trim() ?: anime.status

        return anime.copy(
            title = title,
            description = synopsis,
            coverUrl = cover,
            genre = genres,
            status = status
        )
    }

    fun fetchEpisodeList(anime: Anime): List<Episode> {
        val document = fetchHtml(anime.url)
        val elements = document.select("a[href], li a, div.episode a, .episodes a")

        return elements.mapIndexedNotNull { index, element ->
            val href = normalizeUrl(element.attr("href")) ?: return@mapIndexedNotNull null
            val text = element.text().trim()
            if (href.isBlank()) return@mapIndexedNotNull null

            val episodeNumber = extractEpisodeNumber(text, index + 1)
            val title = if (text.isNotBlank()) text else "Episode $episodeNumber"

            Episode(
                id = "anime-${anime.id}-ep-$episodeNumber",
                number = episodeNumber,
                title = title,
                url = href
            )
        }.distinctBy { it.url }
    }

    fun fetchVideoSources(episode: Episode): List<VideoSource> {
        val document = fetchHtml(episode.url)
        val sources = mutableListOf<VideoSource>()

        val iframeUrls = document.select("iframe[src]")
            .mapNotNull { normalizeUrl(it.attr("src")) }
            .filter { it.contains("embed") || it.contains("stream") || it.contains("video") || it.contains("m3u8") }

        iframeUrls.forEachIndexed { index, url ->
            sources += VideoSource(
                id = "source-${index + 1}",
                label = "Embed ${index + 1}",
                url = url,
                quality = "default"
            )
        }

        if (sources.isEmpty()) {
            val scriptUrls = Regex("https?://[^\"'\\s<>]+")
                .findAll(document.select("script").joinToString(" ") { it.data() })
                .map { it.value }
                .filter { it.contains("m3u8") || it.contains("mp4") || it.contains("stream") }
                .toList()

            scriptUrls.forEachIndexed { index, url ->
                sources += VideoSource(
                    id = "source-${index + 1}",
                    label = "Script ${index + 1}",
                    url = url,
                    quality = "default"
                )
            }
        }

        return if (sources.isNotEmpty()) sources else listOf(
            VideoSource(
                id = "source-1",
                label = "Fallback",
                url = episode.url,
                quality = "default"
            )
        )
    }

    private fun parseCatalog(document: Document): List<Anime> {
        val cards = document.select("article.post, div.anime-item, div.anime-card, li.anime-item, div.item, .post-item")

        return cards.mapIndexedNotNull { index, element ->
            val titleElement = element.selectFirst("h2 a, h3 a, h4 a, a.title, a[href]")
            val imageElement = element.selectFirst("img")
            val href = normalizeUrl(titleElement?.attr("href")) ?: return@mapIndexedNotNull null
            val title = titleElement?.text()?.trim().ifBlank { "Anime ${index + 1}" }
            val cover = normalizeUrl(imageElement?.attr("src") ?: imageElement?.attr("data-src"))
                ?: ""

            Anime(
                id = "anime-${index + 1}",
                title = title ?: "Anime ${index + 1}",
                description = "",
                url = href,
                coverUrl = cover,
                genre = emptyList(),
                status = "Unknown"
            )
        }.distinctBy { it.url }
    }

    private fun fetchHtml(url: String): Document {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36")
            .header("Accept-Language", "id-ID,id;q=0.9,en-US;q=0.8,en;q=0.7")
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: ""
            return Jsoup.parse(body)
        }
    }

    private fun normalizeUrl(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return if (raw.startsWith("http://") || raw.startsWith("https://")) raw else "$baseUrl/$raw".replace("//", "/")
    }

    private fun extractEpisodeNumber(text: String, fallback: Int): Int {
        val numberMatch = Regex("(?:Ep|Episode|Episod|EP)\\s*#?\\s*(\\d+)", RegexOption.IGNORE_CASE)
            .find(text)
        val parsed = numberMatch?.groupValues?.get(1)?.toIntOrNull()
        return parsed ?: fallback
    }
}
