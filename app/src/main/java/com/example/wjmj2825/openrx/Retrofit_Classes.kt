package com.example.wjmj2825.openrx

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

/**
 * L'interface GithubService permet à toute l'application de disposer d'un objet Retrofit capable d'executer des requêtes http
 * et d'en retourner le résultat au travers des fonction getFloowing et getUserInfo.
 */
interface GithubService {

    @GET("users/{username}/following")
    fun getFollowing(@Path("username") username: String): Observable<List<GithubUser>>

    @GET("/users/{username}")
    fun getUserInfos(@Path("username") username: String): Observable<GithubUserInfo>

    companion object {
        val retrofit = Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
    }
}

/**
 * L'objet GithubStreams contient les méthodes transformant les requêtes Retrofit(synchrones) en Observables(asynchrones)
 * L'utilisation d'objets permet également de s'assurer que les appels réseaux ne seront gérés que par une entité, il n'y aura donc pas conflits de ressources.
 */
object GithubStreams {

    fun streamFetchUserInfos(username: String): Observable<GithubUserInfo> {
        val gitHubService = GithubService.retrofit.create(GithubService::class.java)
        return gitHubService.getUserInfos(username)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS)
    }

    fun streamFetchUserFollowing(username: String): Observable<List<GithubUser>> {
        val gitHubService = GithubService.retrofit.create(GithubService::class.java)
        return gitHubService.getFollowing(username)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS)
    }

}

/**
 * Les classes suivantes sont les guides permettants à retrofit de
 * créer des objets à partir des informations récupérées sur l'API de github
 * Les getters et setters ne sont pas à écrire en kotlin, ils sont automatiquement implémentés.
 *
 * Le @Entity présent au dessus de la classe définit celle ci comme étant une table pour le framework Room
 */
@Entity
class GithubUser (var login : String ?= ""
    ){
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    override fun toString(): String {
        return "Github user login -> ${this.login}, id ${this.id}"
    }
}

class GithubUserInfo {

    var login: String? = null
    var id: Int? = null
    var type: String? = null
    var name: String? = null
    var company: String? = null
    var email: String? = null
    var bio: String? = null
    var publicRepos: Int? = null
    var followers: Int? = null
    var following: Int? = null

}