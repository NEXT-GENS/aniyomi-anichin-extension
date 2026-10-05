# Checklist Penyesuaian Scraper Anichin

## Pre-Implementation

- [ ] Verifikasi domain Anichin masih aktif
- [ ] Check apakah situs menggunakan CloudFlare atau proteksi lain
- [ ] Tentukan User-Agent yang tepat
- [ ] Cek apakah situs memerlukan JavaScript rendering

## Catalog Parsing

- [ ] Identifikasi container anime item (article/div/li)
- [ ] Tentukan selector untuk judul
- [ ] Tentukan selector untuk URL anime
- [ ] Tentukan selector untuk cover image
- [ ] Update selector di `parseCatalog()`
- [ ] Test dengan `fetchPopular(1)`
- [ ] Test dengan `fetchLatest(1)`
- [ ] Test dengan `fetchSearch("query", 1)`

## Anime Detail Parsing

- [ ] Identifikasi selector untuk judul lengkap
- [ ] Identifikasi selector untuk sinopsis/deskripsi
- [ ] Identifikasi selector untuk genre
- [ ] Identifikasi selector untuk status (ongoing/completed)
- [ ] Identifikasi selector untuk rating/score (opsional)
- [ ] Update selector di `fetchAnimeDetails()`
- [ ] Test dengan salah satu anime

## Episode List Parsing

- [ ] Identifikasi selector untuk daftar episode
- [ ] Tentukan format penomoran episode
- [ ] Identifikasi selector untuk URL episode
- [ ] Verifikasi episode number extraction logic
- [ ] Update selector di `fetchEpisodeList()`
- [ ] Test dengan anime yang memiliki banyak episode

## Video Source Parsing

- [ ] Identifikasi embed/iframe video
- [ ] Identifikasi script tag dengan URL streaming
- [ ] Tentukan format URL video (m3u8/mp4)
- [ ] Identifikasi link streaming alternatif
- [ ] Update selector di `fetchVideoSources()`
- [ ] Test dengan beberapa episode

## Code Quality

- [ ] Verifikasi error handling untuk empty results
- [ ] Verifikasi fallback selectors
- [ ] Check untuk null pointer exceptions
- [ ] Verifikasi URL normalization
- [ ] Test dengan anime/episode yang tidak standar
- [ ] Tambahkan logging untuk debugging

## Performance

- [ ] Check timeout settings (30 detik cukup?)
- [ ] Verifikasi connection pooling
- [ ] Check memory usage untuk list besar
- [ ] Monitor rate limiting dari situs

## Documentation

- [ ] Update DEBUGGING.md dengan selector yang benar
- [ ] Dokumentasikan format data yang diparsing
- [ ] Catat known issues atau limitations
- [ ] Update README dengan status scraper

## Testing

- [ ] Test `fetchPopular()` - harusnya dapat 20+ anime
- [ ] Test `fetchLatest()` - harusnya dapat anime terbaru
- [ ] Test `fetchSearch("naruto")` - harusnya dapat hasil search
- [ ] Test `fetchAnimeDetails()` - harusnya dapat detail lengkap
- [ ] Test `fetchEpisodeList()` - harusnya dapat minimal 1 episode
- [ ] Test `fetchVideoSources()` - harusnya dapat minimal 1 source

## Known Issues & Workarounds

| Issue | Workaround |
|-------|------------|
| Domain sering berubah | Maintain list of mirror domains |
| CloudFlare protection | Use browser-like User-Agent atau rotate IP |
| Dynamic content (JS) | Consider Jsoup.Connect dengan browser headless jika perlu |
| Rate limiting | Implement delay/exponential backoff |
| Regional blocking | Use VPN atau proxy |

## Future Improvements

- [ ] Implement caching untuk hasil popular/latest
- [ ] Add support untuk quality selection (720p/1080p)
- [ ] Add support untuk subtitle parsing
- [ ] Implement proxy rotation untuk stability
- [ ] Add support untuk anime series (multiple seasons)
- [ ] Monitor situs changes dengan automated testing
