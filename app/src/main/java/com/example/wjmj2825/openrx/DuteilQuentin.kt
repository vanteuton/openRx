package com.example.wjmj2825.openrx

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.Scroller
import android.widget.Toast
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import kotlinx.android.synthetic.main.activity_duteil_quentin.*


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

        val stringBuilder = SpannableStringBuilder()

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                Toast.makeText(this@DuteilQuentin,"prout",Toast.LENGTH_LONG).show()
            }

        }
        if (users.isEmpty())stringBuilder.append("Ce ne sont pas ces droides que vous cherchez")
        else {
            var i = 2
            for (user in users) {
                println("Avant user ${user.login}, i=$i, user.login.lengh=${user.login!!.length}")
                stringBuilder.append("- ${user.login} \n")
                stringBuilder.setSpan(clickableSpan, i, (i+user.login!!.length), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                i += user.login!!.length + 2
                print("Après i=$i")
            }
        }

        val finalString = SpannableString(stringBuilder)

        finalString.setSpan(clickableSpan, 1, 4, Spanned.SPAN_USER)
        finalString.setSpan(clickableSpan, 5, 6, Spanned.SPAN_USER)
        display_text.text = finalString
        display_text.movementMethod = LinkMovementMethod.getInstance()

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
