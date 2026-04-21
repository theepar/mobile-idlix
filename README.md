# WatchMobile (IDLIX App)

Aplikasi Android Native berbasis **Kotlin** dan **Jetpack Compose** untuk me-mirror data film dari IDLIX. Aplikasi ini dilengkapi dengan sistem ekstraksi media pintar untuk mem-bypass Iframe dan memainkan video HLS murni menggunakan Media3 ExoPlayer.

## Fitur Utama
- **Native Android UI**: Dibangun sepenuhnya dengan Jetpack Compose modern.
- **Dynamic Endpoints**: Base URL dikonfigurasi melalui `local.properties` sehingga mudah diganti jika domain utama mati.
- **Media Resolver Module**: Modul ekstraksi menggunakan `Jsoup` yang menembus pemutar video pihak ketiga (Iframe) untuk mendapatkan link sumber `.m3u8` (HLS) dan teks terjemahan (`.vtt` / `.srt`).
- **Media3 ExoPlayer**: Terintegrasi langsung untuk memutar video hasil ekstraksi tanpa menggunakan WebView.
- **Retrofit & Coil**: Implementasi *best practices* untuk koneksi API dan pemuatan gambar (TMDB).

## Prasyarat
- Android Studio Iguana / Jellyfish (Atau versi yang mendukung AGP 8.2+).
- Java 17.

## Cara Menjalankan (Local Development)
1. Kloning repositori ini.
2. Buat file `local.properties` di *root directory* proyek dan tambahkan baris berikut:
   ```properties
   BASE_URL="https://z1.idlixku.com"
   ```
3. Buka proyek ini di **Android Studio**.
4. Tunggu *Gradle Sync* selesai.
5. Jalankan aplikasi di Emulator atau perangkat fisik menggunakan tombol **Run**.

## Disclaimer
Aplikasi ini ditujukan murni sebagai bahan pembelajaran (*Proof of Concept*) tentang cara melakukan scraping tingkat lanjut pada *third-party media player*, *Dynamic Environment Handling*, dan mengimplementasikan arsitektur modern Jetpack Compose.
