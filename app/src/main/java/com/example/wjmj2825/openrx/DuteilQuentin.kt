package com.example.wjmj2825.openrx

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Scroller
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_duteil_quentin.*
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit
import android.text.method.LinkMovementMethod
import android.R
import android.widget.TextView
import android.text.Spanned
import android.text.TextPaint
import android.content.Intent
import android.graphics.Color
import android.text.style.ClickableSpan
import android.text.SpannableString
import android.view.View
import android.widget.Toast


class DuteilQuentin : AppCompatActivity() {

//    TODO("à faire -> Ajouter material design parce que c'est bien, permettre au joueur de choisir le pseudo du mec qu'il veut suivre et enregistrer ses anciens choix (room ?) pour les proposer en auto-complete")
    
    private lateinit var disposable: Disposable
    private var user = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_duteil_quentin)
        display_text.setScroller(object : Scroller(this){})
        stream_on.setOnClickListener {
            user = userNameEditText.text.toString()
            streamOn()
        }
        button2.setOnClickListener {
            executeSecondHttpRequestWithRetrofit()
        }

    }

    fun streamOn() {
        updateUIWhenStartingHTTPRequest()
        this.disposable = GithubStreams.streamFetchUserFollowing(user).subscribeWith(getSubscriber())
    }

    @SuppressLint("SetTextI18n")
    private fun updateUIWhenStartingHTTPRequest() {
        display_text.text = "Downloading ..."
    }

    fun disposeWhenDestroy() {
        if (!this.disposable.isDisposed) this.disposable.dispose()
    }

    fun getSubscriber(): DisposableObserver<List<GithubUser>> {
        val ret = object : DisposableObserver<List<GithubUser>>() {
            override fun onComplete() {
                Log.e("TAG", "On Complete !!");
            }

            override fun onNext(t: List<GithubUser>) {
                updateUIWithListOfUsers(t)
            }

            override fun onError(e: Throwable) {
                Log.e("TAG", "On Error" + Log.getStackTraceString(e));
            }
        }
        return ret
    }

    fun executeSecondHttpRequestWithRetrofit() {
        this.updateUIWhenStartingHTTPRequest()
        this.disposable = GithubStreams.streamFetchUserFollowingAndFetchFirstUserInfos(user).subscribeWith(object : DisposableObserver<GithubUserInfo>() {
            override fun onNext(users: GithubUserInfo) {
                Log.e("TAG", "On Next")
                updateUIWithUserInfo(users)
            }

            override fun onError(e: Throwable) {
                Log.e("TAG", "On Error" + Log.getStackTraceString(e))
            }

            override fun onComplete() {
                Log.e("TAG", "On Complete !!")
            }
        })
    }

    fun updateUIWithListOfUsers(users: List<GithubUser>) {

        val ss = SpannableString("Android is a Software stack")
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                Toast.makeText(this@DuteilQuentin,"prout",Toast.LENGTH_LONG).show()
            }

        }
        ss.setSpan(clickableSpan, 22, 27, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        display_text.text = ss
        display_text.movementMethod = LinkMovementMethod.getInstance()

//        val stringBuilder = StringBuilder()
//        if (users.isEmpty())stringBuilder.append("Ce ne sont pas ces droides que vous cherchez")
//        else {
//            for (user in users) {
//                stringBuilder.append("- ${user.login} \n")
//            }
//        }
//        updateUIWhenStopingHTTPRequests(stringBuilder.toString())
    }

    private fun updateUIWithUserInfo(userInfo: GithubUserInfo) {
        updateUIWhenStopingHTTPRequests("The first Following of $user is " + userInfo.name + " with " + userInfo.followers + " followers.")
    }

    fun updateUIWhenStopingHTTPRequests(response: String) {
        display_text.text = response
    }

    override fun onDestroy() {
        super.onDestroy()
        disposeWhenDestroy()
    }

}

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

    fun streamFetchUserFollowingAndFetchFirstUserInfos(username: String): Observable<GithubUserInfo> {
        return streamFetchUserFollowing(username).map { users -> users[0] }.flatMap { user -> streamFetchUserInfos(user.login.toString()) }
    }

}

class GithubUser {

    @SerializedName("login")
    @Expose
    var login: String? = null
    @SerializedName("id")
    @Expose
    var id: Int? = null
    @SerializedName("avatar_url")
    @Expose
    var avatarUrl: String? = null
    @SerializedName("gravatar_id")
    @Expose
    var gravatarId: String? = null
    @SerializedName("url")
    @Expose
    var url: String? = null
    @SerializedName("html_url")
    @Expose
    var htmlUrl: String? = null
    @SerializedName("followers_url")
    @Expose
    var followersUrl: String? = null
    @SerializedName("following_url")
    @Expose
    var followingUrl: String? = null
    @SerializedName("gists_url")
    @Expose
    var gistsUrl: String? = null
    @SerializedName("starred_url")
    @Expose
    var starredUrl: String? = null
    @SerializedName("subscriptions_url")
    @Expose
    var subscriptionsUrl: String? = null
    @SerializedName("organizations_url")
    @Expose
    var organizationsUrl: String? = null
    @SerializedName("repos_url")
    @Expose
    var reposUrl: String? = null
    @SerializedName("events_url")
    @Expose
    var eventsUrl: String? = null
    @SerializedName("received_events_url")
    @Expose
    var receivedEventsUrl: String? = null
    @SerializedName("type")
    @Expose
    var type: String? = null
    @SerializedName("site_admin")
    @Expose
    var siteAdmin: Boolean? = null

}

class GithubUserInfo {

    @SerializedName("login")
    @Expose
    var login: String? = null
    @SerializedName("id")
    @Expose
    var id: Int? = null
    @SerializedName("avatar_url")
    @Expose
    var avatarUrl: String? = null
    @SerializedName("gravatar_id")
    @Expose
    var gravatarId: String? = null
    @SerializedName("url")
    @Expose
    var url: String? = null
    @SerializedName("html_url")
    @Expose
    var htmlUrl: String? = null
    @SerializedName("followers_url")
    @Expose
    var followersUrl: String? = null
    @SerializedName("following_url")
    @Expose
    var followingUrl: String? = null
    @SerializedName("gists_url")
    @Expose
    var gistsUrl: String? = null
    @SerializedName("starred_url")
    @Expose
    var starredUrl: String? = null
    @SerializedName("subscriptions_url")
    @Expose
    var subscriptionsUrl: String? = null
    @SerializedName("organizations_url")
    @Expose
    var organizationsUrl: String? = null
    @SerializedName("repos_url")
    @Expose
    var reposUrl: String? = null
    @SerializedName("events_url")
    @Expose
    var eventsUrl: String? = null
    @SerializedName("received_events_url")
    @Expose
    var receivedEventsUrl: String? = null
    @SerializedName("type")
    @Expose
    var type: String? = null
    @SerializedName("site_admin")
    @Expose
    var siteAdmin: Boolean? = null
    @SerializedName("name")
    @Expose
    var name: String? = null
    @SerializedName("company")
    @Expose
    var company: String? = null
    @SerializedName("blog")
    @Expose
    var blog: String? = null
    @SerializedName("location")
    @Expose
    var location: String? = null
    @SerializedName("email")
    @Expose
    var email: String? = null
    @SerializedName("hireable")
    @Expose
    var hireable: Boolean? = null
    @SerializedName("bio")
    @Expose
    var bio: String? = null
    @SerializedName("public_repos")
    @Expose
    var publicRepos: Int? = null
    @SerializedName("public_gists")
    @Expose
    var publicGists: Int? = null
    @SerializedName("followers")
    @Expose
    var followers: Int? = null
    @SerializedName("following")
    @Expose
    var following: Int? = null
    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null
    @SerializedName("updated_at")
    @Expose
    var updatedAt: String? = null

}