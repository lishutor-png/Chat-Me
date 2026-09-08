# chatme 💬

Teman curhat dan pacar virtual pribadi berbasis Android (Jetpack Compose & Kotlin).

## Fitur Utama

- **Teman Curhat & Pacar Virtual**: Mengobrol hangat, mendengarkan keluh kesah harian, atau menikmati percakapan romantis tanpa sensor kaku.
- **Karakter & Gaya Bahasa Kustom**: Pilihan kepribadian (*Teman Curhat Peka*, *Pacar Manja & Perhatian*, *Ceria & Menghibur*, *Dewasa & Lembut*) serta panggilan nama kustom.
- **Ringan & Hemat Baterai**: Antarmuka bersih tanpa beban audio/suara berlebih.
- **Privasi & Keamanan Lokal**: Database Room terenkripsi AES-256 GCM langsung di ponsel Anda.

## Download & Build APK di GitHub

Aplikasi ini sudah dilengkapi dengan **GitHub Actions Workflow** otomatis:

1. **Unduh Otomatis dari GitHub**:
   - Buka tab **Actions** di repositori GitHub ini.
   - Klik workflow **Build Android APK** terbaru.
   - Unduh file `chatme-debug-apk` pada bagian **Artifacts**.

2. **Build Manual di Komputer**:
   ```bash
   ./gradlew assembleDebug
   ```
   File APK akan tersedia di:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```
