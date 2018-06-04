package com.example.wjmj2825.openrx

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
import android.arch.persistence.room.Room




class DuteilQuentin : AppCompatActivity() {

//    TODO("à faire -> enregistrer ses anciens choix (room ?) pour les proposer en auto-complete")
    
    /**
     * Une variable lateinit est une variable non définie mais pour laquelle le programme reserve une place mémoire. Il faut que je la définisse avant sa première utilisation dans le code
     * sous peine de lever une exeption.
     *
     * Les disposable sont des classes présentes dans le framework RxJava que j'utilise pour réaliser mes appels asynchrones.
     * Tout est expliqué ici :
     *  https://openclassrooms.com/courses/recuperez-et-affichez-des-donnees-distantes/chainer-differentes-requetes-reseaux-avec-rxjava
     */
    private lateinit var disposable: Disposable


    private var streamOnUser = ""
    private var previousUser = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_duteil_quentin)
        display_text.setScroller(object : Scroller(this){})

        //ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM
        val db = Room.databaseBuilder(applicationContext,AppDatabase::class.java, "database-name").build()
        //ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM


        fetch_followers.setOnClickListener {
            /**
             * userNameEditText est la référence au bouton ayant le même identifiant dans l'IHM. Je suis en capacité de l'appeler grâce aux exentions kotlin chargées dans la ligne 16 de ce fichier.
             */
            streamOnUser = user_name_edit_text.text.toString()
            getListOfSubscribers()
        }

        fetch_single_infos.setOnClickListener{
            streamOnUser = user_name_edit_text.text.toString()
            getDetailledInfos()
        }

    }

    /***
     * Cette fonction mets à jour l'UI pour indiquer un téléchargement. Puis souscrit à un stream de récupération d'une liste d'utilisateurs.
     */
    fun getListOfSubscribers() {
        updateUIWhenStartingHTTPRequest()
        this.disposable = GithubStreams.streamFetchUserFollowing(streamOnUser).subscribeWith(getSubscribers())
    }

    /***
     * Cette fonction mets à jour l'UI pour indiquer un téléchargement. Puis souscrit à un stream de récupération des informations détaillées d'un utilisateurs.
     */
    fun getDetailledInfos(){
        updateUIWhenStartingHTTPRequest()
        this.disposable = GithubStreams.streamFetchUserInfos(streamOnUser).subscribeWith(getInfos())
    }

    private fun updateUIWhenStartingHTTPRequest() {
        display_text.text = getText(R.string.downloading)
    }

    /**
     * disposeWhenDestroy permet de vérifier que le stream suivi par l'application est bien libéré
     */
    fun disposeWhenDestroy() {
        if (!this.disposable.isDisposed) this.disposable.dispose()
    }

    /**
     * getSubscribers retourne un observeur implémenté qui envoie les données du stream à la fonction updateUIWithListOfUsers
     */
    fun getSubscribers(): DisposableObserver<List<GithubUser>> = object : DisposableObserver<List<GithubUser>>() {
            override fun onComplete() {
                Log.e("TAG", "On Complete !!")
            }
            override fun onNext(t: List<GithubUser>) {
                updateUIWithListOfUsers(t)
            }
            override fun onError(e: Throwable) {
                Log.e("TAG", "On Error" + Log.getStackTraceString(e))
            }
    }

    /**
     * getSubscribers retourne un observeur implémenté qui envoie les données du stream à la fonction updateUIWithSingleUserInfo
     */
    fun getInfos(): DisposableObserver<GithubUserInfo> = object : DisposableObserver<GithubUserInfo>() {
            override fun onComplete() {
                Log.e("TAG", "On Complete !!")
            }
            override fun onNext(t: GithubUserInfo) {
                updateUIWithSingleUserInfo(t)
            }
            override fun onError(e: Throwable) {
                Log.e("TAG", "On Error" + Log.getStackTraceString(e))
            }
    }


    /**
     * updateUIWithListOfUsers extrait le nom des utilisateurs reçue en paramètres pour les formater et les insérer dans
     * l'affichage display_text de l'IHM. Le texte est rendu cliquable grâce aux Spannablestring
     */
    fun updateUIWithListOfUsers(users: List<GithubUser>) {
        class InfoClickableSpan(internal val user : GithubUser) : ClickableSpan() {
            override fun onClick(widget: View) {
                previousUser = streamOnUser
                streamOnUser = user.login.toString()
                getDetailledInfos()
            }
        }
        class CancelClickableSpan : ClickableSpan() {
            override fun onClick(widget: View) {
                resetApplication()
            }
        }

        val stringBuilder = SpannableStringBuilder()
        if (users.isEmpty()){
            stringBuilder.append("Ce ne sont pas ces droides que vous cherchez \n\n\n¯\\_(ツ)_/¯\n(Retour au départ)").setSpan(CancelClickableSpan(),48,57,0)
        }
        else {
            stringBuilder.append("Liste des followers de $streamOnUser :\n")
            var i = 28 + streamOnUser.length
            for (user in users) {
                stringBuilder.append("- ${user.login}\n").setSpan(InfoClickableSpan(user), i, (i+user.login!!.length), 0)
                i += user.login!!.length + 3
            }
        }
        display_text.text = SpannableString(stringBuilder)
        display_text.movementMethod = LinkMovementMethod.getInstance()
    }

    /**
     * updateUIWithSingleUserInfo extrait les details de l'utilisateur reçu en paramètres pour les formater et les insérer dans
     * l'affichage display_text de l'IHM. Le texte est rendu cliquable grâce aux Spannablestring
     */
    private fun updateUIWithSingleUserInfo(userInfo: GithubUserInfo) {
        class ListClickableSpan(internal val user : String) : ClickableSpan() {
            override fun onClick(widget: View) {
                previousUser = streamOnUser
                streamOnUser = user
                getListOfSubscribers()
            }
        }

        class DatabaseStoreClickableSpan(internal val user : String) : ClickableSpan() {
            override fun onClick(widget: View) {
                TODO("Implémenter l'insert dans la database")
            }
        }

        val string = SpannableStringBuilder().append("${userInfo.login}")
        if (!userInfo.name.isNullOrEmpty()) string.append(" (${userInfo.name}) ")
        if (previousUser != "")string.append(" est un follower de $previousUser, il")
        string.append(" possède ${userInfo.followers} followers. Il possède ${userInfo.publicRepos} dossiers publics")
        if (!userInfo.company.isNullOrEmpty()) string.append("\nIl travaille dans la compagnie ${userInfo.company}")
        if (!userInfo.email.isNullOrEmpty()) string.append("\nIl possède une l\'adresse mail suivante ${userInfo.email}")
        if (!userInfo.bio.isNullOrEmpty()) string.append("\nVoici sa biographie : ${userInfo.bio}")
        string.append("\nListe des followers").setSpan(ListClickableSpan(streamOnUser),(string.length - 19),string.length,0)
        string.append("\nEnregistrer l\'utilisateur dans la base").setSpan(DatabaseStoreClickableSpan(streamOnUser),(string.length - 38),string.length,0)
        string.append("\nRetour").setSpan(ListClickableSpan(previousUser),(string.length - 6),string.length,0)
        display_text.text = string
    }

    /**
     * resetApplication vide les champs présents dans l'IHM
     */
    fun resetApplication(){
        display_text.text = getText(R.string.instructions)
        user_name_edit_text.text.clear()
    }

    /**
     * onDestroy appelle la fonction disposeWHenDestroy pour s'assurer que le stream est bien libéré. Android ne le gérant pas toujours à 100%
     */
    override fun onDestroy() {
        super.onDestroy()
        disposeWhenDestroy()
    }

}
