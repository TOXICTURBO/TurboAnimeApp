package com.talent.animescrap.animesources

import com.talent.animescrap.model.AnimeDetails
import com.talent.animescrap.model.AnimeStreamLink
import com.talent.animescrap.model.SimpleAnime
import com.talent.animescrap.utils.Utils.getJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class AllAnimeSource : AnimeSource {

    private val mainUrl = "https://api.allanime.to"

    override suspend fun animeDetails(contentLink: String): AnimeDetails =
        withContext(Dispatchers.IO) {
            val hash = "9d7439c90f203e534ca778c4901f9aa2d3ad42c06243ab2c5e6b79612af32028"
            val url =
                "$mainUrl/allanimeapi?variables=%7B%22_id%22%3A%22${contentLink}%22%7D&extensions=%7B%22persistedQuery%22%3A%7B%22version%22%3A1%2C%22"+
                        """sha256Hash%22%3A%22$hash%22%7D%7D"""

            println(url)
            val res = getJson(url)!!.asJsonObject

            val data = res.asJsonObject["data"].asJsonObject["show"].asJsonObject
            val animeCover = allAnimeImage(data["thumbnail"].asString)
            println(animeCover)
            val animeName = data["name"].asString
            val animDesc =
                if (!data["description"].isJsonNull) Jsoup.parseBodyFragment(data["description"].asString)
                    .text() else "No Description"

            val subNum =
                data["lastEpisodeInfo"].asJsonObject["sub"].asJsonObject["episodeString"].asString
            val subEpMap = (1..subNum.toInt()).associate { it.toString() to it.toString() }
            val allEps = mutableMapOf("SUB" to subEpMap)
            try {
                val dubNum =
                    data["lastEpisodeInfo"].asJsonObject["dub"].asJsonObject["episodeString"].asString
                val dubEpMap = (1..dubNum.toInt()).associate { it.toString() to it.toString() }
                allEps["DUB"] = dubEpMap
            } catch (_: Exception) {
            }
            return@withContext AnimeDetails(animeName, animDesc, animeCover, allEps)
        }


    override suspend fun searchAnime(searchedText: String) = withContext(Dispatchers.IO) {
        val animeList = arrayListOf<SimpleAnime>()
        val hash = "06327bc10dd682e1ee7e07b6db9c16e9ad2fd56c1b769e47513128cd5c9fc77a"

        val url =
            """$mainUrl/allanimeapi?variables=%7B%22search%22%3A%7B%22allowAdult%22%3Afalse%2C%22allowUnknown%22%3Afalse%2C%22query%22%3A%22${
                searchedText.replace(
                    "+",
                    "%20"
                )
            }%22%7D%2C%22limit%22%3A26%2C%22page%22%3A1%2C%22translationType%22%3A%22sub%22%2C%22countryOrigin%22%3A%22ALL%22%7D&extensions=%7B%22persistedQuery%22%3A%7B%22version%22%3A1%2C%22"""+
                    """sha256Hash%22%3A%22$hash%22%7D%7D"""

        val res =
            getJson(url)!!.asJsonObject["data"].asJsonObject["shows"].asJsonObject["edges"].asJsonArray
        for (json in res) {
            val name = json.asJsonObject["name"].asString
            val image = allAnimeImage(json.asJsonObject["thumbnail"].asString)
            val id = json.asJsonObject["_id"].asString
            animeList.add(SimpleAnime(name, image, id))
        }
        return@withContext animeList
    }

    override suspend fun latestAnime(): ArrayList<SimpleAnime> =
        withContext(Dispatchers.IO) {
            val animeList = arrayListOf<SimpleAnime>()
            val hash = "06327bc10dd682e1ee7e07b6db9c16e9ad2fd56c1b769e47513128cd5c9fc77a"
            val url =
                "$mainUrl/allanimeapi?variables=%7B%22search%22%3A%7B%22allowAdult%22%3Afalse%2C%22allowUnknown%22%3Afalse%2C%22isManga%22%3Afalse%7D%2C%22limit%22%3A26%2C%22page%22%3A1%2C%22translationType%22%3A%22sub%22%2C%22countryOrigin%22%3A%22ALL%22%7D&extensions=%7B%22persistedQuery%22%3A%7B%22version%22%3A1%2C%22"+
                        "sha256Hash%22%3A%22$hash%22%7D%7D"
            println(url)
            val res =
                getJson(url)!!.asJsonObject["data"].asJsonObject["shows"].asJsonObject["edges"].asJsonArray
            for (json in res) {
                val name = json.asJsonObject["name"].asString
                val image = allAnimeImage(json.asJsonObject["thumbnail"].asString)
                val id = json.asJsonObject["_id"].asString
                animeList.add(SimpleAnime(name, image, id))
            }
            return@withContext animeList
        }

    override suspend fun trendingAnime(): ArrayList<SimpleAnime> =
        withContext(Dispatchers.IO) {
            val animeList = arrayListOf<SimpleAnime>()
            val hash = "1fc9651b0d4c3b9dfd2fa6e1d50b8f4d11ce37f988c23b8ee20f82159f7c1147"
            val url = """$mainUrl/allanimeapi?variables=%7B%22type%22%3A%22anime%22%2C%22size%22%3A30%2C%22dateRange%22%3A7%2C%22page%22%3A1%7D&extensions=%7B%22persistedQuery%22%3A%7B%22version%22%3A1%2C%22"""+
                        """sha256Hash%22%3A%22$hash%22%7D%7D"""
            val res =
                getJson(url)!!.asJsonObject["data"].asJsonObject["queryPopular"].asJsonObject["recommendations"].asJsonArray
            for (json in res) {
                val animeCard = json.asJsonObject["anyCard"].asJsonObject
                val name = animeCard["name"].asString
                val image = allAnimeImage(animeCard["thumbnail"].asString)
                val id = animeCard["_id"].asString
                animeList.add(SimpleAnime(name, image, id))
            }
            return@withContext animeList
        }

    override suspend fun streamLink(
        animeUrl: String,
        animeEpCode: String,
        extras: List<String>?
    ): AnimeStreamLink =
        withContext(Dispatchers.IO) {

            println(animeUrl)
            println(animeEpCode)

            val type = if (extras?.first() == "DUB") "dub" else "sub"
            println(type)
            val hash = "5f1a64b73793cc2234a389cf3a8f93ad82de7043017dd551f38f65b89daa65e0"
            val url =
                """$mainUrl/allanimeapi?variables=%7B%22showId%22%3A%22$animeUrl%22%2C%22translationType%22%3A%22$type%22%2C%22episodeString%22%3A%22$animeEpCode%22%7D&extensions=%7B%22persistedQuery%22%3A%7B%22version%22%3A1%2C%22"""+
                        """sha256Hash%22%3A%22$hash%22%7D%7D"""
            println(url)
            val res =
                getJson(url)!!.asJsonObject["data"].asJsonObject["episode"].asJsonObject["sourceUrls"].asJsonArray
            val sortedSources =
                res.sortedBy { if (!it.asJsonObject["priority"].isJsonNull) it.asJsonObject["priority"].asDouble else 0.0 }
                    .reversed()
            println(sortedSources)
            println("sorted")
            for (sourceUrlHolder in sortedSources) {
                println(sourceUrlHolder)
                val sourceUrl = sourceUrlHolder.asJsonObject["sourceUrl"].asString
                println(sourceUrl)
                if (isThese(sourceUrl)) continue
                if (sourceUrl.contains("apivtwo")) {
                    val apiUrl = "https://allanimenews.com"
                    println(apiUrl)
                    println(
                        "$apiUrl${
                            sourceUrl.replace("clock", "clock.json")
                        }"
                    )
                    val allLinks = getJson(
                        "$apiUrl${
                            sourceUrl.replace("clock", "clock.json")
                        }"
                    )!!.asJsonObject["links"].asJsonArray
                    println(allLinks)
                    val firstLink = allLinks.first().asJsonObject
                    println(firstLink)
                    if (firstLink.has("portData") && firstLink["portData"].asJsonObject.has("streams")) {
                        for (link in firstLink["portData"].asJsonObject["streams"].asJsonArray) {
                            if (link.toString().contains("dash")) continue
                            if (!link.asJsonObject["hardsub_lang"].asString.contains("en")) continue
                            return@withContext AnimeStreamLink(
                                link.asJsonObject["url"].asString,
                                "",
                                link.toString().contains("hls")
                            )
                        }
                    }
                    println("link = $firstLink")

                    val isHls = firstLink.has("hls") && firstLink["hls"].asBoolean
                    val streamUrl = if(firstLink.has("rawUrls")) firstLink["rawUrls"].asJsonObject["vids"].asJsonArray.first().asJsonObject["url"].asString else firstLink["link"].asString
                    println()
                    return@withContext AnimeStreamLink(streamUrl, "", isHls)
                }

                return@withContext AnimeStreamLink(
                    sourceUrl,
                    "",
                    res.first().toString().contains("hls")
                )
            }

            return@withContext AnimeStreamLink("", "", false)
        }

    private fun isThese(url: String): Boolean {
        val unwantedSources = listOf("goload", "streamsb", "ok.ru", "streamlare", "mp4upload")
        unwantedSources.forEach { source ->
            if (url.contains(source)) return true
        }
        return false
    }

    private fun allAnimeImage(imageUrl: String) =
        if (imageUrl.contains("kickassanime")) "https://wp.youtube-anime.com/${
            imageUrl.replace(
                "https://",
                ""
            )
        }?w=250"
        else if (imageUrl.contains("_Show_")) "https://wp.youtube-anime.com/aln.youtube-anime.com/images/${
            imageUrl.replaceAfterLast(
                ".",
                "css"
            )
        }"
        else imageUrl
}
