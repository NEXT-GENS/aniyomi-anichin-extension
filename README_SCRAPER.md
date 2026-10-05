# Anichin Scraper - Implementation Guide

## Deskripsi

Ini adalah extension Aniyomi untuk scraping anime dari situs Anichin. Project ini menyediakan framework untuk:

- Fetch daftar anime (popular, latest, search)
- Parse detail anime (judul, sinopsis, genre, status)
- Parse episode list
- Ekstrak video sources (embed/iframe)

## Struktur Project

```
aniyomi-anichin-extension/
├── app/
│   ├── build.gradle.kts
│   └── src/main/java/com/nextgens/anichin/
│       ├── Anichin.kt              # Main source class
│       ├── AnichinApi.kt           # HTTP & parsing logic
│       └── models/
│           ├── Anime.kt            # Data model
│           ├── Episode.kt          # Data model
│           └── VideoSource.kt      # Data model
├── settings.gradle.kts
├── build.gradle.kts
├── README.md
├── DEBUGGING.md                     # Panduan debugging
└── SCRAPER_CHECKLIST.md            # Checklist implementasi
```

## Dependencies

- **OkHttp**: HTTP client
- **Jsoup**: HTML parsing
- **Retrofit**: REST client (optional untuk future use)
- **Gson**: JSON parsing (optional)
- **Coroutines**: Async operations

## Quick Start

### 1. Setup Project

```bash
git clone https://github.com/NEXT-GENS/aniyomi-anichin-extension.git
cd aniyomi-anichin-extension
```

### 2. Verify Anichin Domain

Buka `app/src/main/java/com/nextgens/anichin/AnichinApi.kt` dan verifikasi:

```kotlin
private val baseUrl = "https://www.anichin.id"
```

Jika domain berubah, update ke domain yang aktif.

### 3. Inspect HTML dan Update Selector

Ikuti langkah di `DEBUGGING.md` untuk:

1. Inspect halaman katalog
2. Inspect halaman detail
3. Inspect halaman episode
4. Update selector di `AnichinApi.kt`

### 4. Test Scraper

```kotlin
fun main() {
    val source = Anichin()
    
    // Test popular
    val popular = source.fetchPopular(1)
    println("Popular: ${popular.size} anime")
    
    // Test search
    val search = source.fetchSearch("Naruto", 1)
    println("Search: ${search.size} result")
    
    // Test detail
    if (search.isNotEmpty()) {
        val detail = source.fetchAnimeDetails(search[0])
        println("Detail: ${detail.title} - ${detail.description}")
    }
}
```

### 5. Build APK (untuk Aniyomi)

```bash
./gradlew :app:assembleRelease
```

Hasil: `app/build/outputs/apk/release/app-release.apk`

## API Reference

### CatalogSource Interface

```kotlin
interface CatalogSource {
    val id: Long              // Source ID (harus unik)
    val name: String          // Nama source
    val lang: String          // Bahasa ("id" untuk Indonesia)
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
```

### Anichin Class

Implementasi `CatalogSource` untuk Anichin.

```kotlin
val source = Anichin()
val animeList = source.fetchPopular(1)  // Fetch halaman 1 popular
```

### AnichinApi Class

Internal API untuk HTTP request dan parsing HTML.

```kotlin
val api = AnichinApi()
val animeList = api.fetchPopular(1)
val details = api.fetchAnimeDetails(anime)
val episodes = api.fetchEpisodeList(anime)
val videos = api.fetchVideoSources(episode)
```

### Data Models

#### Anime

```kotlin
data class Anime(
    val id: String,
    val title: String,
    val description: String,
    val url: String,
    val coverUrl: String,
    val genre: List<String>,
    val status: String
)
```

#### Episode

```kotlin
data class Episode(
    val id: String,
    val number: Int,
    val title: String,
    val url: String
)
```

#### VideoSource

```kotlin
data class VideoSource(
    val id: String,
    val label: String,
    val url: String,
    val quality: String
)
```

## Troubleshooting

### "No anime found"

**Penyebab**: Selector CSS tidak sesuai dengan struktur HTML saat ini.

**Solusi**:
1. Buka `https://www.anichin.id` di browser
2. Inspect element (F12)
3. Catat selector yang benar
4. Update di `parseCatalog()` di `AnichinApi.kt`

### "Cannot connect to www.anichin.id"

**Penyebab**: Domain tidak aktif atau diblokir.

**Solusi**:
1. Cek apakah situs masih online
2. Coba dengan VPN jika diblokir regional
3. Update User-Agent di `fetchHtml()`

### "Episode not found"

**Penyebab**: Struktur episode di halaman detail berbeda dari ekspektasi.

**Solusi**:
1. Inspect halaman detail anime
2. Cari selector untuk episode list
3. Update di `fetchEpisodeList()`

### "Video source empty"

**Penyebab**: Situs menggunakan format embed/streaming yang berbeda.

**Solusi**:
1. Inspect halaman episode
2. Cari `<iframe src="...">` atau script dengan URL video
3. Update selector/regex di `fetchVideoSources()`

## Performance Tips

1. **Connection Timeout**: Default 30 detik, adjust sesuai kebutuhan
2. **Read Timeout**: Default 30 detik, increase jika situs lambat
3. **Caching**: Implement caching di level Aniyomi application
4. **Rate Limiting**: Tambah delay antar request jika situs melimit

## Contributing

Jika Anda menemukan issue atau improvement:

1. Test scraper dengan berbagai anime
2. Dokumentasikan issue di `DEBUGGING.md`
3. Propose fix dengan PR
4. Jika fix berhasil, update `SCRAPER_CHECKLIST.md`

## License

MIT

## Support

Untuk bantuan, refer ke:
- `DEBUGGING.md` - Panduan debugging
- `SCRAPER_CHECKLIST.md` - Checklist implementasi
- Issue di repository
