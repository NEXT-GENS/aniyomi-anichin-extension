package com.nextgens.anichin

data class Anime(
    val id: String,
    val title: String,
    val description: String,
    val url: String,
    val coverUrl: String,
    val genre: List<String>,
    val status: String
) {
    fun copy(
        title: String = this.title,
        description: String = this.description,
        url: String = this.url,
        coverUrl: String = this.coverUrl,
        genre: List<String> = this.genre,
        status: String = this.status
    ): Anime = Anime(id, title, description, url, coverUrl, genre, status)
}
