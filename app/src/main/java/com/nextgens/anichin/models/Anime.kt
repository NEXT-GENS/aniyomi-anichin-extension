package com.nextgens.anichin

data class Anime(
    val id: String,
    val title: String,
    val description: String,
    val href: String,
    val coverUrl: String,
    val genre: List<String>,
    val status: String
) {
    fun copy(
        title: String = this.title,
        description: String = this.description,
        href: String = this.href,
        coverUrl: String = this.coverUrl,
        genre: List<String> = this.genre,
        status: String = this.status
    ): Anime = Anime(id, title, description, href, coverUrl, genre, status)
}
