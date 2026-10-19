package com.example.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

data class DramaLink(
    val title: String,
    val url: String,
    val image: String,
    val episodeNum: Int
)

object QDramaScraper {

    private const val BASE_URL = "https://a.q-drama.com"

    private val client: OkHttpClient by lazy {
        try {
            val trustManager = object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf(trustManager), SecureRandom())
            OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustManager)
                .hostnameVerifier { _, _ -> true }
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .followRedirects(true)
                .build()
        } catch (_: Throwable) {
            OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()
        }
    }

    private fun absoluteUrl(url: String): String {
        val trimmed = unescapeHtml(url).trim()
        if (trimmed.isEmpty()) return ""
        if (trimmed.startsWith("//")) return "https:$trimmed"
        if (trimmed.startsWith("/")) return BASE_URL.trimEnd('/') + trimmed
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed
        return BASE_URL.trimEnd('/') + "/" + trimmed.trimStart('/')
    }

    private fun cleanText(raw: String): String {
        val stripped = raw.replace(Regex("<[^>]+>"), " ")
        val unescaped = unescapeHtml(stripped)
        return unescaped.replace(Regex("\\s+"), " ").trim()
    }

    private fun unescapeHtml(input: String): String {
        return input
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#039;", "'")
            .replace("&apos;", "'")
            .replace("&nbsp;", " ")
    }

    private fun extractEpisodeNumber(title: String): Int {
        val patterns = listOf(
            Regex("""(?:الحلقة|الحلقه)\s*(\d+)""", RegexOption.IGNORE_CASE),
            Regex("""(?:episode|ep)[\s\-]*(\d+)""", RegexOption.IGNORE_CASE),
            Regex("""(?:^|\s)(\d+)(?:\s|$)""")
        )
        for (pattern in patterns) {
            val match = pattern.find(title)
            if (match != null && match.groupValues.size > 1) {
                match.groupValues[1].toIntOrNull()?.let { return it }
            }
        }
        return 999999
    }

    suspend fun search(query: String): List<DramaLink> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) return@withContext emptyList()

        try {
            val encoded = URLEncoder.encode(cleanQuery, "UTF-8")
            val targetUrl = "$BASE_URL/search.php?keywords=$encoded"

            val request = Request.Builder()
                .url(targetUrl)
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                )
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "ar,en-US;q=0.9,en;q=0.8")
                .header("Referer", "$BASE_URL/")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext emptyList()

            val html = response.body?.string() ?: return@withContext emptyList()
            parseHtmlResults(html)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseHtmlResults(html: String): List<DramaLink> {
        val results = mutableListOf<DramaLink>()
        val seen = mutableSetOf<String>()

        val anchorPattern = Pattern.compile("<a\\b([^>]*)>(.*?)</a>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        val hrefPattern = Pattern.compile("""href\s*=\s*["']([^"']+)["']""", Pattern.CASE_INSENSITIVE)
        val imagePattern = Pattern.compile(
            """(?:src|data-src|data-original|data-lazy-src|data-echo|data-image|data-thumb|data-thumbnail)\s*=\s*["']([^"']+)["']""",
            Pattern.CASE_INSENSITIVE
        )

        val matcher = anchorPattern.matcher(html)
        while (matcher.find()) {
            val attrs = matcher.group(1) ?: ""
            val inner = matcher.group(2) ?: ""

            val hrefMatcher = hrefPattern.matcher(attrs)
            if (!hrefMatcher.find()) continue

            val rawHref = unescapeHtml(hrefMatcher.group(1) ?: "").trim()
            if (rawHref.isEmpty() || rawHref.startsWith("#")) continue

            val hrefLower = rawHref.lowercase()
            if (hrefLower.contains("login.php") ||
                hrefLower.contains("register.php") ||
                hrefLower.contains("contact_us.php") ||
                hrefLower.contains("logout.php") ||
                hrefLower.contains("javascript:") ||
                hrefLower.contains("mailto:")
            ) continue

            val title = cleanText(inner)
            if (title.length < 3) continue

            val titleLower = title.lowercase()

            val isEpisode = Regex("""(?:الحلقة|الحلقه)\s*\d+""").containsMatchIn(title) ||
                    Regex("""\b(?:episode|ep)[\s\-_]*\d+\b""", RegexOption.IGNORE_CASE).containsMatchIn(titleLower) ||
                    Regex("""(?:^|\s)\d+(?:\s|$)""").containsMatchIn(title)

            if (!isEpisode) continue

            val contentLike = hrefLower.contains("/episode") ||
                    hrefLower.contains("/watch") ||
                    hrefLower.contains("/video") ||
                    hrefLower.contains("/show/") ||
                    hrefLower.contains("episode-") ||
                    hrefLower.contains("ep-") ||
                    title.contains("الحلقة")

            if (!contentLike) continue

            val fullUrl = absoluteUrl(rawHref)
            if (seen.contains(fullUrl)) continue
            seen.add(fullUrl)

            var image = ""
            val pos = html.indexOf(rawHref)
            if (pos >= 0) {
                val start = maxOf(0, pos - 3000)
                val end = minOf(html.length, pos + 3000)
                val around = html.substring(start, end)
                val imgMatcher = imagePattern.matcher(around)
                if (imgMatcher.find()) {
                    image = absoluteUrl(imgMatcher.group(1) ?: "")
                }
            }

            val epNum = extractEpisodeNumber(title)
            results.add(
                DramaLink(
                    title = title,
                    url = fullUrl,
                    image = image,
                    episodeNum = epNum
                )
            )
        }

        results.sortWith(compareBy({ it.episodeNum }, { it.title }))
        return results
    }
}
