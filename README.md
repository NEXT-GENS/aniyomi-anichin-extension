# Aniyomi Extension - Anichin

Project starter untuk extension Aniyomi yang mengambil data dari situs Anichin.

Fitur yang sudah disiapkan:
- Struktur project Gradle/Kotlin
- Koneksi HTTP dengan OkHttp + Retrofit
- Model data anime, episode, dan sumber video
- Kelas source placeholder yang bisa dikembangkan ke extension Aniyomi
- Parser dasar untuk halaman katalog, detail, episode, dan video

Catatan penting:
- Repositori ini adalah starter project untuk extension Aniyomi.
- Untuk dipakai di Aniyomi/Tachiyomi, Anda perlu menambahkan dependency `source-api`/`extension-api` dari environment Aniyomi yang sesuai.
- URL, scrapping selector, dan endpoint bisa disesuaikan sesuai struktur situs Anichin yang sedang aktif.

## Struktur project

- `app/` : module extension
- `settings.gradle.kts` : konfigurasi Gradle project
- `build.gradle.kts` : root Gradle plugin

## Quick start

```bash
gradlew :app:assemble
```

## Langkah berikutnya

1. Ubah endpoint dan selector HTML sesuai situs Anichin yang aktif.
2. Mapping detail anime dan episode sesuai hasil HTML.
3. Integrasikan ke repo extension Aniyomi resmi jika Anda ingin publish.

## Contoh penggunaan kelas utama

```kotlin
val source = Anichin()
val popular = source.fetchPopular(1)
println(popular)
```
