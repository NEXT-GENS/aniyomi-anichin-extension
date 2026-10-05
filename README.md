# Anichin Aniyomi Extension

Project starter untuk extension anime Aniyomi yang menargetkan sumber Anichin.

Fitur dasar yang sudah disiapkan:
- Struktur module Android/Kotlin untuk extension
- Pemetaan data anime, episode, dan source video
- HTTP fetch menggunakan OkHttp + Jsoup
- Parser katalog, detail, episode, dan video source
- Interface source umum agar mudah diadaptasi ke environment Aniyomi/Tachiyomi

Catatan penting:
- Ini adalah starter project, bukan extension resmi yang sudah terbukti compatible dengan Aniyomi.
- Untuk build di lingkungan Aniyomi/Tachiyomi, Anda perlu menambahkan dependency `source-api` dan/atau `extension-api` yang sesuai dari project upstream.
- Selector HTML dan endpoint website Anichin dapat berubah; sesuaikan sesuai struktur site yang aktif saat ini.

## Struktur utama

- `app/build.gradle.kts`
- `app/src/main/java/com/nextgens/anichin/Anichin.kt`
- `app/src/main/java/com/nextgens/anichin/AnichinApi.kt`
- `app/src/main/java/com/nextgens/anichin/models/Anime.kt`
- `app/src/main/java/com/nextgens/anichin/models/Episode.kt`
- `app/src/main/java/com/nextgens/anichin/models/VideoSource.kt`

## Cara build

```bash
gradlew :app:assemble
```

## Penggunaan singkat

```kotlin
val source = Anichin()
val popular = source.fetchPopular(1)
println(popular)
```

## Langkah berikutnya

1. Sesuaikan host Anichin yang benar (contoh: `https://www.anichin.id` atau domain lain yang aktif).
2. Periksa HTML live site Anichin dan sesuaikan selector CSS pada parser.
3. Tambahkan integrasi ke `source-api`/`extension-api` bila Anda ingin mempublish extension untuk Aniyomi.
4. Tambahkan support login, filter, dan parser video streaming yang lebih stabil.
