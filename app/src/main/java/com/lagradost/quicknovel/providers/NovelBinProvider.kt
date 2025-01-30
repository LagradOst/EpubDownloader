package com.lagradost.quicknovel.providers
import com.lagradost.quicknovel.ErrorLoadingException
import com.lagradost.quicknovel.HeadMainPageResponse
import com.lagradost.quicknovel.LoadResponse
import com.lagradost.quicknovel.MainAPI
import com.lagradost.quicknovel.MainActivity.Companion.app
import com.lagradost.quicknovel.R
import com.lagradost.quicknovel.STATUS_COMPLETE
import com.lagradost.quicknovel.STATUS_NULL
import com.lagradost.quicknovel.STATUS_ONGOING
import com.lagradost.quicknovel.SearchResponse
import com.lagradost.quicknovel.fixUrlNull
import com.lagradost.quicknovel.newChapterData
import com.lagradost.quicknovel.newSearchResponse
import com.lagradost.quicknovel.newStreamResponse
import kotlin.math.roundToInt


class NovelBinProvider : MainAPI() {
    override val name = "NovelBin"
    override val mainUrl = "https://novelbin.com"
    override val hasMainPage = true

    override val iconId = R.drawable.icon_novelbin

    override val tags = listOf(
        "All" to "All",
        "Action" to "action",
        "Adventure" to "adventure",
        "Anime & comics" to "anime-&-comics",
        "Comedy" to "comedy",
        "Drama" to "drama",
        "Eastern" to "eastern",
        "Fanfiction" to "fanfiction",
        "Fantasy" to "fantasy",
        "Game" to "game",
        "Gender bender" to "gender-bender",
        "Harem" to "harem",
        "Historical" to "historical",
        "Horror" to "horror",
        "Isekai" to "isekai",
        "Josei" to "josei",
        "Litrpg" to "litrpg",
        "Magic" to "magic",
        "Magical realism" to "magical-realism",
        "Martial arts" to "martial-arts",
        "Mature" to "mature",
        "Mecha" to "mecha",
        "Modern life" to "modern-life",
        "Mystery" to "mystery",
        "Other" to "other",
        "Psychological" to "psychological",
        "Reincarnation" to "reincarnation",
        "Romance" to "romance",
        "School life" to "school-life",
        "Sci-fi" to "sci-fi",
        "Seinen" to "seinen",
        "Shoujo" to "shoujo",
        "Shoujo ai" to "shoujo-ai",
        "Shounen" to "shounen",
        "Shounen ai" to "shounen-ai",
        "Slice of life" to "slice-of-life",
        "Smut" to "smut",
        "Sports" to "sports",
        "Supernatural" to "supernatural",
        "System" to "system",
        "Tragedy" to "tragedy",
        "Urban life" to "urban-life",
        "Video games" to "video-games",
        "Wuxia" to "wuxia",
        "Xianxia" to "xianxia",
        "Xuanhuan" to "xuanhuan",
        "Yaoi" to "yaoi",
        "Yuri" to "yuri",
    )

    override val orderBys = listOf(
        "Genre" to "",
        "Latest Release" to "sort/latest",
        "Hot Novel" to "sort/top-hot-novel",
        "Completed Novel" to "sort/completed",
        "Most Popular" to "sort/top-view-novel",
        "Store" to "store",
    )

    val fullPosterRegex = Regex("/novel_[0-9]*_[0-9]*/")

    override suspend fun loadMainPage(
        page: Int,
        mainCategory: String?,
        orderBy: String?,
        tag: String?
    ): HeadMainPageResponse {
        val url =
            if (orderBy == "" && tag != "All") "$mainUrl/genre/$tag?page=$page" else "$mainUrl/${if (orderBy.isNullOrBlank()) "sort/top-hot-novel" else orderBy}?page=$page"
        val document = app.get(url).document

        return HeadMainPageResponse(
            url,
            list = document.select("div.list>div.row").mapNotNull { element ->
                val a =
                    element.selectFirst("div > div > h3.novel-title > a") ?: return@mapNotNull null
                SearchResponse(
                    name = a.text(),
                    url = fixUrlNull(a.attr("href")) ?: return@mapNotNull null,
                    fixUrlNull(element.selectFirst("div > div > img")?.attr("data-src")?.replace( fullPosterRegex, "/novel/")),
                    null,
                    null,
                    this.name
                )
            })
    }

    override suspend fun loadHtml(url: String): String? {
        val document = app.get(url).document
        val content = (document.selectFirst("#chapter-content")
            ?: document.selectFirst("#chr-content"))
        if (content == null) return null

        return content.html()
            .replace(
                "<iframe .* src=\"//ad.{0,2}-ads.com/.*\" style=\".*\"></iframe>".toRegex(),
                " "
            ).replace(
                " If you find any errors ( broken links, non-standard content, etc.. ), Please let us know &lt; report chapter &gt; so we can fix it as soon as possible.",
                " "
            ).replace(
                "If you find any errors ( Ads popup, ads redirect, broken links, non-standard content, etc.. ), Please let us know &lt; report chapter &gt; so we can fix it as soon as possible.",
                " "
            ).replace("[Updated from F r e e w e b n o v e l. c o m]", "")

    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document =
            app.get("$mainUrl/search?keyword=$query").document // AJAX, MIGHT ADD QUICK SEARCH

        return document.select("#list-page>.archive>.list>.row").mapNotNull { h ->
            val title = h.selectFirst(">div>div>.truyen-title>a")
                ?: h.selectFirst(">div>div>.novel-title>a") ?: return@mapNotNull null
            newSearchResponse(title.text(), title.attr("href") ?: return@mapNotNull null) {
                posterUrl = fixUrlNull(h.selectFirst(">div>div>img")?.attr("src")?.replace( fullPosterRegex, "/novel/"))
            }
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document

        val name =
            document.selectFirst("h3.title")?.text() ?: throw ErrorLoadingException("invalid name")

        val dataNovelId = document.select("#rating").attr("data-novel-id")
        val ajaxUrl = "$mainUrl/ajax/chapter-archive?novelId=$dataNovelId"
        val chapterData = app.get(ajaxUrl).document
        var parsed = chapterData.select("select > option")
        if (parsed.isEmpty()) {
            parsed = chapterData.select(".list-chapter>li>a")
        }

        val data = parsed.mapNotNull { c ->
            var cUrl = c.attr("value")
            if (cUrl.isNullOrBlank()) {
                cUrl = c.attr("href")
            }
            if (cUrl.isNullOrBlank()) {
                return@mapNotNull null
            }
            val cName = c.text().ifEmpty {
                "chapter $c"
            }
            newChapterData(cName, cUrl)
        }

        return newStreamResponse(name, url, data) {
            tags = document.select("ul.info > li:nth-child(5) a").map {
                it.text()
            }
            author = document.selectFirst("ul.info > li:nth-child(1) > a")?.text()
            posterUrl = fixUrlNull(document.select("div.book > img").attr("data-src"))
            synopsis = document.selectFirst("div.desc-text")?.text()
            peopleVoted =
                document.selectFirst(" div.small > em > strong:nth-child(3) > span")?.text()
                    ?.toIntOrNull() ?: 0
            rating = document.selectFirst("div.small > em > strong:nth-child(1) > span")?.text()
                ?.toFloatOrNull()?.times(100)?.roundToInt()

            this.status =
                when (document.selectFirst("ul.info > li:nth-child(3) > a")?.selectFirst("a")
                    ?.text()) {
                    "Ongoing" -> STATUS_ONGOING
                    "Completed" -> STATUS_COMPLETE
                    else -> STATUS_NULL
                }
        }
    }
}