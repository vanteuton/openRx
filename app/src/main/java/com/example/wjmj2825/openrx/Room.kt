package com.example.wjmj2825.openrx

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.Database




@Dao
interface GithubUserDao {
    @get:Query("SELECT * FROM GithubUser")
    val all: List<GithubUser>

    @Query("SELECT * FROM GithubUser WHERE login LIKE :string LIMIT 1")
    fun findByName(string: String): GithubUser

    @Insert
    fun insertAll(vararg GithubUsers: GithubUser)


    @Insert
    fun insert(githubUser: GithubUser)

    @Delete
    fun delete(user: GithubUser)
}

@Database(entities = arrayOf(GithubUser::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fithubUserDao(): GithubUserDao
}