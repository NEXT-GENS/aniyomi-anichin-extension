# Test Script Guide

## Overview

Script test `SelectorValidator.kt` digunakan untuk memvalidasi selector CSS pada situs Anichin yang sedang aktif. Script ini akan otomatis test berbagai selector dan menampilkan hasil untuk setiap kategori parsing (catalog, detail, episode, video source).

## Cara Menggunakan

### Opsi 1: Jalankan dari IDE (Android Studio)

1. Buka file `app/src/main/java/com/nextgens/anichin/SelectorValidator.kt`
2. Klik tombol "Run" di sebelah fungsi `main()`
3. Lihat output di Logcat/Console

### Opsi 2: Jalankan dari Command Line

```bash
# Build project terlebih dahulu
./gradlew build

# Jalankan test script
./gradlew :app:run -Pmain=com.nextgens.anichin.SelectorValidatorKt
```

### Opsi 3: Jalankan sebagai Unit Test

Atau buat file test di `app/src/test/java/com/nextgens/anichin/SelectorValidatorTest.kt`:

```kotlin
import org.junit.Test

class SelectorValidatorTest {
    @Test
    fun testSelectors() {
        val validator = SelectorValidator()
        validator.runAllTests()
    }
}
```

Lalu jalankan:

```bash
./gradlew :app:test
```

## Output Yang Diharapkan

### Contoh Output Sukses:

```
============================================================
Anichin Selector Validator
============================================================

=== Testing Catalog Page Selectors ===
✅ 'article.post' -> Found 20 items
✅ 'div.anime-item' -> Found 0 items
❌ 'div.anime-card' -> No items found

=== Testing Title Selectors ===
✅ 'h2 a' -> Found: 'Naruto Shippuden'
❌ 'h3 a' -> Not found

=== Testing URL Selectors ===
✅ 'h2 a' -> Found: '/anime/naruto-shippuden'

=== Testing Image Selectors ===
✅ 'img' (attr: src) -> Found: 'https://example.com/poster.jpg'

=== Testing Detail Page Selectors ===
Testing detail page: https://www.anichin.id/anime/naruto-shippuden

Title selectors:
✅ 'h1.entry-title' -> Found: 'Naruto Shippuden - Full Series'

Description selectors:
✅ 'div.entry-content p' -> Found: 'Naruto is a shinobi...'

Genre selectors:
✅ 'div.genre a' -> Found: [Action, Adventure, Shounen]

Status selectors:
✅ '.status' -> Found: 'Completed'

=== Testing Episode List Selectors ===
✅ 'a[href]' -> Found 500 items. Sample: [Episode 1, Episode 2, Episode 3]
✅ '.episode a' -> Found 500 items. Sample: [Episode 1, Episode 2, Episode 3]

=== Testing Video Source Selectors ===
Testing episode page: https://www.anichin.id/episode/naruto-1

Iframe selectors:
✅ 'iframe[src]' -> Found 2 iframes
   - https://player.example.com/embed?id=123
   - https://alternate-stream.com/video?id=456

Script tag analysis:
✅ Found 1 m3u8 URLs
   - https://stream.example.com/playlist.m3u8

============================================================
Test complete! Update selectors in AnichinApi.kt based on results above.
============================================================
```

## Interpretasi Hasil

### ✅ (Berhasil)
Selector berhasil menemukan elemen yang dicari. Update `AnichinApi.kt` dengan selector ini.

### ❌ (Gagal)
Selector tidak menemukan elemen. Gunakan browser DevTools untuk menemukan selector yang benar.

## Langkah-Langkah Setelah Testing

### 1. Identifikasi Selector yang Berhasil

Catat semua selector dengan tanda ✅. Contoh:
- Catalog container: `article.post`
- Title: `h2 a`
- URL: `h2 a` (attr: href)
- Image: `img` (attr: src)
- Detail title: `h1.entry-title`
- Genre: `div.genre a`
- Episode list: `.episode a`
- Video iframe: `iframe[src]`

### 2. Update AnichinApi.kt

Update fungsi-fungsi berikut dengan selector yang berhasil:

```kotlin
// Di parseCatalog()
val cards = document.select("article.post")  // Ganti dengan selector berhasil

val titleElement = element.selectFirst("h2 a")  // Ganti dengan selector berhasil
val imageElement = element.selectFirst("img")  // Ganti dengan selector berhasil

// Di fetchAnimeDetails()
val title = document.selectFirst("h1.entry-title")?.text()  // Ganti dengan selector berhasil
val genres = document.select("div.genre a")  // Ganti dengan selector berhasil

// Di fetchEpisodeList()
val elements = document.select(".episode a")  // Ganti dengan selector berhasil

// Di fetchVideoSources()
val iframeUrls = document.select("iframe[src]")  // Ganti dengan selector berhasil
```

### 3. Test Ulang

Setelah update, jalankan script test lagi untuk verifikasi:

```bash
./gradlew :app:run -Pmain=com.nextgens.anichin.SelectorValidatorKt
```

### 4. Test Functional

Setelah selector diupdate, test functionality:

```kotlin
fun main() {
    val source = Anichin()
    
    // Test popular
    println("Fetching popular anime...")
    val popular = source.fetchPopular(1)
    println("Found ${popular.size} anime")
    popular.take(3).forEach { println("  - ${it.title}") }
    
    // Test detail
    if (popular.isNotEmpty()) {
        println("\nFetching detail for: ${popular[0].title}")
        val detail = source.fetchAnimeDetails(popular[0])
        println("  Title: ${detail.title}")
        println("  Description: ${detail.description.take(100)}...")
        println("  Genre: ${detail.genre}")
        
        // Test episode
        println("\nFetching episodes...")
        val episodes = source.fetchEpisodeList(popular[0])
        println("Found ${episodes.size} episodes")
        episodes.take(3).forEach { println("  - ${it.title}") }
        
        // Test video
        if (episodes.isNotEmpty()) {
            println("\nFetching video sources for: ${episodes[0].title}")
            val videos = source.fetchVideoSources(episodes[0])
            println("Found ${videos.size} sources")
            videos.forEach { println("  - ${it.label}: ${it.url}") }
        }
    }
}
```

## Troubleshooting

### "Cannot connect to www.anichin.id"

**Solusi**:
1. Verifikasi koneksi internet Anda
2. Cek apakah domain masih aktif di browser
3. Jika diblokir regional, gunakan VPN
4. Update domain di `SelectorValidator.kt` jika sudah berubah

### Semua selector menampilkan ❌

**Solusi**:
1. Cek apakah halaman memerlukan JavaScript rendering
2. Update User-Agent di `fetchHtml()`
3. Cek apakah situs menggunakan proteksi CloudFlare
4. Gunakan browser DevTools untuk inspect struktur HTML sebenarnya

### Script crash/timeout

**Solusi**:
1. Increase timeout di `OkHttpClient.Builder()`
2. Check firewall/proxy settings
3. Coba dengan koneksi yang lebih stabil

## Tips

1. **Jalankan di saat yang tepat**: Jalankan test ketika situs Anichin sedang aktif dan responsif
2. **Save output**: Copy-paste output ke file untuk referensi nanti
3. **Test berkala**: Jalankan test secara berkala (mingguan/bulanan) untuk detect perubahan struktur situs
4. **Dokumentasi**: Update `DEBUGGING.md` dengan selector yang bekerja dan kapan terakhir diverifikasi

## Next Steps

Setelah validation selesai:
1. Update selectors di `AnichinApi.kt`
2. Test functional dengan data real
3. Update `SCRAPER_CHECKLIST.md` dengan status
4. Commit changes ke repository
