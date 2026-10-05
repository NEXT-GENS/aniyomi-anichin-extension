# Panduan Debugging Scraper Anichin

## Tujuan
File ini berisi langkah-langkah untuk memvalidasi dan menyesuaikan scraper Anichin agar bekerja dengan struktur HTML situs yang sedang aktif.

## Langkah 1: Identifikasi URL Anichin yang Aktif

1. Buka browser dan kunjungi: `https://www.anichin.id`
2. Periksa apakah domain masih aktif atau apakah sudah pindah ke domain lain
3. Jika domain berubah, update `baseUrl` di `AnichinApi.kt`

```kotlin
private val baseUrl = "https://www.anichin.id"  // Ganti dengan domain yang aktif
```

## Langkah 2: Inspect HTML Halaman Katalog

1. Buka halaman katalog Anichin (misalnya: `https://www.anichin.id/anime/page/1`)
2. Buka DevTools (F12)
3. Inspect elemen anime (klik kanan → Inspect)
4. Cari struktur HTML untuk:
   - Container anime item (article, div, li)
   - Judul anime
   - URL anime
   - Gambar/poster

### Contoh struktur yang mungkin ditemukan:

```html
<!-- Kemungkinan 1 -->
<article class="post">
  <h2><a href="/anime/judul-anime">Judul Anime</a></h2>
  <img src="/images/poster.jpg" alt="Poster">
</article>

<!-- Kemungkinan 2 -->
<div class="anime-item">
  <img src="/images/poster.jpg">
  <h3><a href="/anime/judul-anime">Judul Anime</a></h3>
</div>

<!-- Kemungkinan 3 -->
<li class="anime-card">
  <a href="/anime/judul-anime">
    <img src="/images/poster.jpg">
    <span class="title">Judul Anime</span>
  </a>
</li>
```

5. Catat selector CSS yang cocok
6. Update di `parseCatalog()` di `AnichinApi.kt`

## Langkah 3: Test Selector CSS

Gunakan DevTools Console untuk test selector:

```javascript
// Test selector container
document.querySelectorAll('article.post, div.anime-item, li.anime-card')

// Test selector judul
document.querySelectorAll('h2 a, h3 a, span.title')

// Test selector gambar
document.querySelectorAll('img')
```

## Langkah 4: Inspect Halaman Detail Anime

1. Buka halaman detail anime (klik salah satu anime)
2. Inspect elemen untuk:
   - Judul lengkap (h1, h2)
   - Sinopsis/deskripsi
   - Genre
   - Status (ongoing/completed)
   - List episode

### Contoh struktur detail:

```html
<!-- Judul -->
<h1 class="entry-title">Judul Anime Lengkap</h1>

<!-- Sinopsis -->
<div class="entry-content">
  <p>Sinopsis anime...</p>
</div>

<!-- Genre -->
<div class="genre">
  <a href="#">Action</a>
  <a href="#">Adventure</a>
</div>

<!-- Episode List -->
<div class="episodes">
  <a href="/episode/1">Episode 1</a>
  <a href="/episode/2">Episode 2</a>
</div>
```

3. Update selector di `fetchAnimeDetails()` dan `fetchEpisodeList()`

## Langkah 5: Inspect Halaman Episode

1. Buka halaman episode
2. Inspect untuk menemukan:
   - Embed/iframe video
   - Script yang berisi URL streaming (m3u8, mp4)
   - Tombol play/link alternatif

### Contoh struktur episode:

```html
<!-- Iframe embed -->
<iframe src="https://embed-player.com/video?id=123"></iframe>

<!-- Script dengan URL -->
<script>
  var videoUrl = 'https://stream.example.com/video.mp4';
  var m3u8Url = 'https://stream.example.com/playlist.m3u8';
</script>

<!-- Link alternatif -->
<a href="https://alternative-stream.com/video">Download/Stream</a>
```

3. Update selector di `fetchVideoSources()`

## Langkah 6: Update AnichinApi.kt

Setelah mengidentifikasi selector yang benar, update kode:

```kotlin
private fun parseCatalog(document: Document): List<Anime> {
    // GANTI selector ini sesuai hasil inspection
    val cards = document.select("YOUR_SELECTOR_HERE")
    
    return cards.mapIndexedNotNull { index, element ->
        val titleElement = element.selectFirst("YOUR_TITLE_SELECTOR")
        val imageElement = element.selectFirst("YOUR_IMAGE_SELECTOR")
        
        // ... rest of code
    }
}

fun fetchAnimeDetails(anime: Anime): Anime {
    val document = fetchHtml(anime.url)
    
    val title = document.selectFirst("YOUR_TITLE_SELECTOR")?.text()?.trim() ?: anime.title
    val description = document.selectFirst("YOUR_DESC_SELECTOR")?.text()?.trim() ?: anime.description
    val cover = document.selectFirst("YOUR_COVER_SELECTOR")?.attr("src") ?: anime.coverUrl
    
    // ... rest of code
}

fun fetchEpisodeList(anime: Anime): List<Episode> {
    val document = fetchHtml(anime.url)
    val elements = document.select("YOUR_EPISODE_SELECTOR")
    
    // ... rest of code
}

fun fetchVideoSources(episode: Episode): List<VideoSource> {
    val document = fetchHtml(episode.url)
    
    val iframeUrls = document.select("YOUR_IFRAME_SELECTOR")
        .mapNotNull { normalizeUrl(it.attr("src")) }
    
    // ... rest of code
}
```

## Langkah 7: Test dengan Kotlin Script

Buat file test sederhana untuk verify scraper:

```kotlin
fun main() {
    val api = AnichinApi()
    
    // Test fetch popular
    try {
        val popular = api.fetchPopular(1)
        println("Popular anime count: ${popular.size}")
        popular.forEach { println("  - ${it.title}") }
    } catch (e: Exception) {
        println("Error fetching popular: ${e.message}")
    }
    
    // Test fetch search
    try {
        val search = api.fetchSearch("Naruto", 1)
        println("Search result count: ${search.size}")
    } catch (e: Exception) {
        println("Error searching: ${e.message}")
    }
}
```

## Langkah 8: Handling Error Umum

### Error: "No anime found"
- **Penyebab**: Selector tidak cocok dengan struktur HTML
- **Solusi**: Re-inspect HTML dan update selector

### Error: "Cannot fetch page"
- **Penyebab**: Domain tidak aktif atau diblokir
- **Solusi**: Cek apakah URL masih aktif, update User-Agent jika diperlukan

### Error: "Episode tidak terdeteksi"
- **Penyebab**: Struktur episode berbeda dari ekspektasi
- **Solusi**: Inspect halaman detail dan update selector episode

### Error: "Video source tidak ditemukan"
- **Penyebab**: Embed/iframe tidak ditemukan atau berbeda format
- **Solusi**: Inspect halaman episode dan update regex/selector video

## Langkah 9: Optimasi Regex Episode Number

Jika format episode di Anichin berbeda, update regex di `extractEpisodeNumber()`:

```kotlin
private fun extractEpisodeNumber(text: String, fallback: Int): Int {
    // Format saat ini: "Ep 1", "Episode 1", "EP 01"
    val match = Regex("(?:Ep|Episode|Episod|EP)\\s*#?\\s*(\\d+)", RegexOption.IGNORE_CASE)
        .find(text)
    
    // Jika format berbeda, tambah regex lain:
    // Format contoh: "1 - Judul", "[01]"
    val altMatch = Regex("\\[(\\d+)\\]|^(\\d+)\\s-")
        .find(text)
    
    val parsed = match?.groupValues?.getOrNull(1)?.toIntOrNull()
        ?: altMatch?.groupValues?.getOrNull(1)?.toIntOrNull()
        ?: altMatch?.groupValues?.getOrNull(2)?.toIntOrNull()
    
    return parsed ?: fallback
}
```

## Langkah 10: Dokumentasi Perubahan

Setelah sukses fix scraper, dokumentasikan di file ini:

```markdown
## Status Scraper Anichin

**Last Updated**: [Tanggal]
**Domain**: https://www.anichin.id
**Status**: ✅ Working / ❌ Not Working

### Selector yang digunakan:
- Catalog container: `article.post`
- Title: `h2 a`
- Image: `img.attachment-post-thumbnail`
- Anime detail h1: `h1.entry-title`
- Episode list: `div.episodes a`
- Video iframe: `iframe[src]

### Catatan:
- Domain pernah diblokir, pastikan update User-Agent jika diperlukan
- Format episode mengikuti pola: "Ep 1", "Episode 01"
- Video sources bisa dari iframe atau script m3u8
```

## Tips Debugging Lanjutan

1. **Gunakan HTTP Interceptor** untuk log request/response
2. **Save HTML** hasil fetch untuk inspection offline
3. **Batch test** beberapa anime untuk validasi konsistensi
4. **Monitor** perubahan struktur situs secara berkala
5. **Gunakan VPN** jika situs memblokir akses dari region tertentu
