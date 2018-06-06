package com.example.wjmj2825.openrx

import android.arch.persistence.room.*

/**
 * Cette interface donne à l'application des méthodes d'accès à la base de données. Elle utilise le framework ROOM.
 * La classe GithubDatabase défini la structure de la base de donnnées (ligne 24)
 */
@Dao
interface GithubUserDao {

    @Query("SELECT login FROM GithubUser")
    fun allNames(): Array<String>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(githubUser: GithubUser): Long


    @Query("DELETE FROM GithubUser ")
    fun deleteAll(): Int
}

@Database(entities = [(GithubUser::class)], version = 1)
abstract class GithubDatabase : RoomDatabase() {
    abstract fun githubUserDao(): GithubUserDao
}