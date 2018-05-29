package com.example.wjmj2825.openrx

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.Scroller
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import kotlinx.android.synthetic.main.activity_duteil_quentin.*


class DuteilQuentin : AppCompatActivity() {

//    TODO("à faire -> Ajouter material design parce que c'est bien, permettre au joueur de choisir le pseudo du mec qu'il veut suivre et enregistrer ses anciens choix (room ?) pour les proposer en auto-complete")
    
    private lateinit var disposable: Disposable
    private var streamOnUser = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_duteil_quentin)
        display_text.setScroller(object : Scroller(this){})
        stream_on.setOnClickListener {
            streamOnUser = userNameEditText.text.toString()
            streamOn()
        }

    }

    fun streamOn() {
        updateUIWhenStartingHTTPRequest()
        this.disposable = GithubStreams.streamFetchUserFollowing(streamOnUser).subscribeWith(getSubscriber())
    }

    fun streamFORINFOSon(){
        updateUIWhenStartingHTTPRequest()
        this.disposable = GithubStreams.streamFetchUserInfos(streamOnUser).subscribeWith(getInfos())
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

    fun getInfos(): DisposableObserver<GithubUserInfo> {
        val ret = object : DisposableObserver<GithubUserInfo>() {
            override fun onComplete() {
                Log.e("TAG", "On Complete !!");
            }

            override fun onNext(t: GithubUserInfo) {
                updateUIWithSingleUserInfo(t)
            }

            override fun onError(e: Throwable) {
                Log.e("TAG", "On Error" + Log.getStackTraceString(e));
            }
        }
        return ret
    }

    fun updateUIWithListOfUsers(users: List<GithubUser>) {

        val stringBuilder = SpannableStringBuilder()

        class InfoClickableSpan(internal val user : GithubUser) : ClickableSpan() {

            override fun onClick(widget: View) {
                streamOnUser = user.login.toString()
                getInfos()
            }

        }


        class CancelClickableSpan : ClickableSpan() {

            override fun onClick(widget: View) {
                resetApplication()
            }

            fun resetApplication(){
                display_text.text = getText(R.string.instructions)
                userNameEditText.text.clear()
            }

        }
        if (users.isEmpty()){
            stringBuilder.append("Ce ne sont pas ces droides que vous cherchez \n\n\n¯\\_(ツ)_/¯\n(Retour au départ)")
            stringBuilder.setSpan(CancelClickableSpan(),48,57,0)
        }
        else {
            var i = 2
            var count = 1
            for (user in users) {
                println("Avant streamOnUser ${user.login}, i=$i, streamOnUser.login.lengh=${user.login!!.length}")
                stringBuilder.append("- ${user.login}\n")
                stringBuilder.setSpan(InfoClickableSpan(user), i, (i+user.login!!.length), 0)
                i += user.login!!.length + 3
                count ++
                print("Après i=$i")
            }
        }

        val finalString = SpannableString(stringBuilder)

        display_text.text = finalString
        display_text.movementMethod = LinkMovementMethod.getInstance()

    }

    private fun updateUIWithSingleUserInfo(userInfo: GithubUserInfo) {
        display_text.text = "The first Following of $streamOnUser is " + userInfo.name + " with " + userInfo.followers + " followers."
    }


    override fun onDestroy() {
        super.onDestroy()
        disposeWhenDestroy()
    }

}
