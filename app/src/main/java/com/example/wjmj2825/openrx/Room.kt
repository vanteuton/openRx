package com.example.wjmj2825.openrx

import android.arch.persistence.room.*
import io.reactivex.Flowable


@Dao
interface GithubUserDao {

    @Query("SELECT login FROM GithubUser")
    fun allNames() : Array<String>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(githubUser: GithubUser) : Long


    @Query("DELETE FROM GithubUser ")
    fun deleteAll() : Int
}

@Database(entities = arrayOf(GithubUser::class), version = 1)
abstract class GithubDatabase : RoomDatabase() {
    abstract fun githubUserDao(): GithubUserDao
}