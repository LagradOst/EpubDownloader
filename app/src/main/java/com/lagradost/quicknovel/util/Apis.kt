package com.lagradost.quicknovel.util

import android.content.Context
import androidx.preference.PreferenceManager
import com.lagradost.quicknovel.APIRepository
import com.lagradost.quicknovel.CommonActivity.activity
import com.lagradost.quicknovel.MainAPI
import com.lagradost.quicknovel.R
import com.lagradost.quicknovel.providers.*

class Apis {
    companion object {
        val apis: List<MainAPI> = arrayOf(
            //AllProvider(),
//            NovelPassionProvider(), // Site gone
            BestLightNovelProvider(),
//            WuxiaWorldOnlineProvider(), // Site does not work
            RoyalRoadProvider(),
            LibReadProvider(),
            FreewebnovelProvider(),
            //AzynovelProvider(), // dont exist anymore
            ReadfromnetProvider(),
            AllNovelProvider(),
            //RanobesProvider(), // custom capcha
            NovelFullProvider(),
            NovelBinProvider(),
            //MNovelFreeProvider(), // same as NovelFullVipProvider
            //EngNovelProvider(),
            NovelsOnlineProvider(),
            //EfremnetProvider(), // domain is expired
            GraycityProvider(),
            MtlNovelProvider(),

            AnnasArchive(),

            // chapter captcha
//            WuxiaWorldSiteProvider(),
            ReadLightNovelProvider(),
            BoxNovelProvider(),
            ComrademaoProvider(),
//            LightNovelPubProvider(), // Got cloudflare, but probably bypassable
            ReadNovelFullProvider(),
            ScribblehubProvider(),
            KolNovelProvider(),
            //RewayatArProvider(), // removed url
//            ReadAnyBookProvider(), // Books locked behind login
            MeioNovelProvider(),
            MoreNovelProvider(),
            IndoWebNovelProvider(),
            SakuraNovelProvider(),
            WattpadProvider(),
            WtrLabProvider(),
            PawReadProver()
        ).sortedBy { it.name }

        fun getApiFromName(name: String): APIRepository {
            return getApiFromNameOrNull(name) ?: APIRepository(apis[1])
        }

        fun getApiFromNameNull(apiName: String?): MainAPI? {
            for (api in apis) {
                if (apiName == api.name)
                    return api
            }
            return null
        }

        fun getApiFromNameOrNull(name: String): APIRepository? {
            for (a in apis) {
                if (a.name == name) {
                    return APIRepository(a)
                }
            }
            if (name == RedditProvider().name) return APIRepository(RedditProvider())
            return null
        }

        fun printProviders() {
            var str = ""
            for (api in apis) {
                str += "- ${api.mainUrl}\n"
            }
            println(str)
        }

        fun Context.getApiSettings(): HashSet<String> {
            val settingsManager = PreferenceManager.getDefaultSharedPreferences(this)

            val hashSet = HashSet<String>()
            hashSet.addAll(apis.map { it.name })

            val set = settingsManager.getStringSet(
                this.getString(R.string.search_providers_list_key),
                hashSet
            )?.toHashSet() ?: hashSet

            val activeLangs = getApiProviderLangSettings()
            val list = HashSet<String>()
            for (name in set) {
                val api = getApiFromNameNull(name) ?: continue
                if (activeLangs.contains(api.lang)) {
                    list.add(name)
                }
            }
            if (list.isEmpty()) return hashSet
            return list
        }

        fun getApiProviderLangSettings(): HashSet<String> {
            return activity?.getApiProviderLangSettings() ?: hashSetOf()
        }

        fun Context.getApiProviderLangSettings(): HashSet<String> {
            val settingsManager = PreferenceManager.getDefaultSharedPreferences(this)
            val hashSet = HashSet<String>()
            hashSet.add("en") // def is only en
            val list = settingsManager.getStringSet(
                this.getString(R.string.provider_lang_key),
                hashSet.toMutableSet()
            )

            if (list.isNullOrEmpty()) return hashSet
            return list.toHashSet()
        }
    }
}
