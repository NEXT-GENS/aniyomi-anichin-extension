package com.nextgens.anichin

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

// Catatan:
// Kelas ini merupakan starter untuk extension Aniyomi yang terintegrasi
// dengan Aniyomi/Tachiyomi source API. Sesuaikan dengan dependency source-api
// yang tersedia di repo extension Anda.

class Anichin : CatalogSource {
    override val id: Long = 202406L
    override val name: String = "Anichin"
    override val lang: String = "id"
    override val supportsLatest: Boolean = true
    override val supportsSearch: Boolean = true

    private val api = AnichinApi()

    override fun fetchPopular(page: Int): List<Anime>
        = api.fetchPopular(page)

    override fun fetchLatest(page: Int): List<Anime>
        = api.fetchLatest(page)

    override fun fetchSearch(query: String, page: Int): List<Anime>
        = api.fetchSearch(query, page)

    override fun fetchAnimeDetails(anime: Anime): Anime
        = api.fetchAnimeDetails(anime)

    override fun fetchEpisodeList(anime: Anime): List<Episode>
        = api.fetchEpisodeList(anime)

    override fun fetchVideoSources(episode: Episode): List<VideoSource>
        = api.fetchVideoSources(episode)

    override fun fetchPagesFromVideoSource(videoSource: VideoSource): List<String>
        = listOf(videoSource.url)

    private fun parseHtml(html: String): Document = Jsoup.parse(html)
}

interface CatalogSource {
    val id: Long
    val name: String
    val lang: String
    val supportsLatest: Boolean
    val supportsSearch: Boolean

    fun fetchPopular(page: Int): List<Anime>
    fun fetchLatest(page: Int): List<Anime>
    fun fetchSearch(query: String, page: Int): List<Anime>
    fun fetchAnimeDetails(anime: Anime): Anime
    fun fetchEpisodeList(anime: Anime): List<Episode>
    fun fetchVideoSources(episode: Episode): List<VideoSource>
    fun fetchPagesFromVideoSource(videoSource: VideoSource): List<String>
}
