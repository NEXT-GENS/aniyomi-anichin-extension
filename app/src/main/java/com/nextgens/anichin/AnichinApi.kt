package com.nextgens.anichin

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit

class AnichinFactory {
    fun create(): Anichin = Anichin()
}

class AnichinApi(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build(),
    private val gson: Gson = Gson()
) {
    private val baseUrl = "https://www.anichin.id"

    fun fetchPopular(page: Int): List<Anime> {
        val url = "$baseUrl/anime/page/$page?sort=popular"
        val document = getHtml(url)
        return parseCatalog(document)
    }

    fun fetchLatest(page: Int): List<Anime> {
        val url = "$baseUrl/anime/page/$page?sort=latest"
        val document = getHtml(url)
        return parseCatalog(document)
    }

    fun fetchSearch(query: String, page: Int): List<Anime> {
        val url = "$baseUrl/?s=${query.trim().replace(" ", "+")}&page=$page"
        val document = getHtml(url)
        return parseCatalog(document)
    }

    fun fetchAnimeDetails(anime: Anime): Anime {
        val document = getHtml(anime.href)
        val title = document.selectFirst("h1.entry-title")?.text()?.trim() ?: anime.title
        val synopsis = document.selectFirst("div.entry-content p")?.text()?.trim() ?: anime.description
        val cover = document.selectFirst("img.attachment-post-thumbnail")?.attr("src") ?: anime.coverUrl

        return anime.copy(
            title = title,
            description = synopsis,
            coverUrl = cover
        )
    }

    fun fetchEpisodeList(anime: Anime): List<Episode> {
        val document = getHtml(anime.href)
        return document.select("div.eps a")
            .mapIndexed { index, element ->
                val href = element.attr("href")
                val title = element.text().trim().ifEmpty { "Episode ${index + 1}" }
                Episode(
                    id = "${anime.id}-ep-${index + 1}",
                    number = index + 1,
                    title = title,
                    href = href
                )
            }
    }

    fun fetchVideoSources(episode: Episode): List<VideoSource> {
        val document = getHtml(episode.href)
        val sources = mutableListOf<VideoSource>()

        document.select("script").forEach { script ->
            val html = script.html()
            if (html.contains("https://") || html.contains("http://")) {
                val regex = Regex("https?://[^\"'\\s<>]+")
                regex.findAll(html).forEach { match ->
                    val url = match.value
                    if (url.contains("mp4") || url.contains("m3u8") || url.contains("stream")) {
                        sources += VideoSource(
                            id = "source-${sources.size + 1}",
                            label = "Auto",
                            url = url,
                            quality = "default"
                        )
                    }
                }
            }
        }

        if (sources.isEmpty()) {
            // fallback sementara jika selector tidak cocok
            val fallbackUrl = document.selectFirst("iframe")?.attr("src")
            if (!fallbackUrl.isNullOrBlank()) {
                sources += VideoSource(
                    id = "source-1",
                    label = "Fallback",
                    url = fallbackUrl,
                    quality = "default"
                )
            }
        }

        return sources
    }

    private fun getHtml(url: String): org.jsoup.nodes.Document {
        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36")
            .addHeader("Accept-Language", "id-ID,id;q=0.9,en-US;q=0.8,en;q=0.7")
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: ""
        response.close()
        return Jsoup.parse(body)
    }

    private fun parseCatalog(document: org.jsoup.nodes.Document): List<Anime> {
        return document.select("article.post, div.anime-item, li.post-item")
            .mapIndexed { index, element ->
                val titleEl = element.selectFirst("h2 a, h3 a, a.title")
                val imageEl = element.selectFirst("img")
                val href = titleEl?.attr("href") ?: ""
                val title = titleEl?.text()?.trim() ?: "Anime ${index + 1}"
                val coverUrl = imageEl?.attr("src") ?: ""
                Anime(
                    id = "anime-${index + 1}",
                    title = title,
                    description = "",
                    href = href,
                    coverUrl = coverUrl,
                    genre = emptyList(),
                    status = "Unknown"
                )
            }
            .filter { it.href.isNotBlank() }
    }
}
