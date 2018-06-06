package com.example.wjmj2825.openrx

import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.method.MovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.FocusFinder
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Scroller
import android.widget.Toast
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_duteil_quentin.*


class DuteilQuentin : AppCompatActivity() {

//    TODO("à faire -> enregistrer ses anciens choix (room ?) et mettre à jour l'adapteur qd ya un nouveau choix.")

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

    private var githubUsersToInsert = arrayOf(GithubUser("vanteuton"),
            GithubUser("arte-fact"),
            GithubUser("mathefab"),
            GithubUser("MrAEize"))

    private lateinit var usersLogins: Array<String>

    lateinit var db: GithubUserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_duteil_quentin)
        display_text.setScroller(object : Scroller(this) {})

        githubUsersToInsert[0].id = 1
        githubUsersToInsert[1].id = 2
        githubUsersToInsert[2].id = 3
        githubUsersToInsert[3].id = 4


        /**
         * Cette partie du code instancie un helper de la base de donées
         * Ensuite, de manière asynchrone l'application insère quelques pseudo puis lis tous les pseudo présents en base pour les afficher sur l'UI
         */
        //ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM
        db = Room.databaseBuilder(applicationContext, GithubDatabase::class.java, "GhitubUsersDatabase").build().githubUserDao()

        Completable.fromAction { githubUsersToInsert.forEach { db.insert(it) }}.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe {
            Single.fromCallable { db.allNames() }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe { names ->
                usersLogins = names
                val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names)
                user_name_edit_text.setAdapter(adapter)
                user_name_edit_text.isFocusableInTouchMode = true
                user_name_edit_text.isFocusable = true
            }
        }

        //ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM ROOM


        fetch_followers.setOnClickListener {
            /**
             * userNameEditText est la référence au bouton ayant le même identifiant dans l'IHM. Je suis en capacité de l'appeler grâce aux exentions kotlin chargées dans la ligne 16 de ce fichier.
             */
            streamOnUser = user_name_edit_text.text.toString()
            getListOfSubscribers()
        }

        fetch_single_infos.setOnClickListener {
            streamOnUser = user_name_edit_text.text.toString()
            getDetailledInfos()
        }

        reset.setOnClickListener {
            resetApplication()
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
    fun getDetailledInfos() {
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
            updateUIWithListOfUsers(emptyList())
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
            updateUIWithListOfUsers(emptyList())
            Log.e("TAG", "On Error" + Log.getStackTraceString(e))
        }
    }


    /**
     * updateUIWithListOfUsers extrait le nom des utilisateurs reçue en paramètres pour les formater et les insérer dans
     * l'affichage display_text de l'IHM. Le texte est rendu cliquable grâce aux Spannablestring
     */
    fun updateUIWithListOfUsers(users: List<GithubUser>) {
        class InfoClickableSpan(internal val user: GithubUser) : ClickableSpan() {
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
        if (users.isEmpty()) {
            stringBuilder.append("Ce ne sont pas ces droides que vous cherchez \n(aucune data trouvée)\n\n¯\\_(ツ)_/¯\n(Retour au départ)").setSpan(CancelClickableSpan(), 69, 78, 0)
        } else {
            stringBuilder.append("Liste des followers de $streamOnUser :\n")
            var i = 28 + streamOnUser.length
            for (user in users) {
                stringBuilder.append("- ${user.login}\n").setSpan(InfoClickableSpan(user), i, (i + user.login!!.length), 0)
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
        class ListClickableSpan(internal val user: String) : ClickableSpan() {
            override fun onClick(widget: View) {
                previousUser = streamOnUser
                streamOnUser = user
                getListOfSubscribers()
            }
        }

        class DatabaseStoreClickableSpan(internal val user: String) : ClickableSpan() {
            override fun onClick(widget: View) {
                if (!usersLogins.contains(user)){
                    Completable.fromAction { db.insert(GithubUser(user)) }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
                        Snackbar.make(main_linear, "Utilisateur sauvegardé", Snackbar.LENGTH_INDEFINITE).show()
                        usersLogins += user
                        val adapter = ArrayAdapter<String>(this@DuteilQuentin, android.R.layout.simple_list_item_1, usersLogins)
                        user_name_edit_text.setAdapter(adapter)
                    }, {
                        Snackbar.make(main_linear, "Echec de la sauvegarde de l'utilisateur \n $it", Snackbar.LENGTH_INDEFINITE).show()
                    })
                }
                else{
                    Snackbar.make(main_linear, "Utilisateur déjà présent en mémoire", Snackbar.LENGTH_INDEFINITE).show()
                }

            }
        }

        val string = SpannableStringBuilder().append("${userInfo.login}")
        if (!userInfo.name.isNullOrEmpty()) string.append(" (${userInfo.name}) ")
        if (previousUser != "") string.append(" est un follower de $previousUser, il")
        if (userInfo.publicRepos != null) string.append(" possède ${userInfo.followers} followers. Il possède ${userInfo.publicRepos} dossiers publics")
        else string.append(" possède ${userInfo.followers} followers. Il ne possède pas de dossiers publics")
        if (!userInfo.company.isNullOrEmpty()) string.append("\nIl travaille dans la compagnie ${userInfo.company}")
        if (!userInfo.email.isNullOrEmpty()) string.append("\nIl possède une l\'adresse mail suivante ${userInfo.email}")
        if (!userInfo.bio.isNullOrEmpty()) string.append("\nVoici sa biographie : ${userInfo.bio}")
        string.append("\nListe des followers").setSpan(ListClickableSpan(streamOnUser), (string.length - 19), string.length, 0)
        string.append("\nEnregistrer l\'utilisateur dans la base").setSpan(DatabaseStoreClickableSpan(streamOnUser), (string.length - 38), string.length, 0)
        string.append("\nRetour").setSpan(ListClickableSpan(previousUser), (string.length - 6), string.length, 0)
        display_text.text = string
        display_text.movementMethod = LinkMovementMethod.getInstance()
    }

    /**
     * resetApplication vide les champs présents dans l'IHM
     */
    fun resetApplication() {
        user_name_edit_text.isFocusable = false
        display_text.text = getText(R.string.instructions)
        user_name_edit_text.text.clear()
        user_name_edit_text.isFocusable = true
        user_name_edit_text.isFocusableInTouchMode = true
    }

    /**
     * onDestroy appelle la fonction disposeWHenDestroy pour s'assurer que le stream est bien libéré. Android ne le gérant pas toujours à 100%
     */
    override fun onDestroy() {
        super.onDestroy()
        disposeWhenDestroy()
    }

}
