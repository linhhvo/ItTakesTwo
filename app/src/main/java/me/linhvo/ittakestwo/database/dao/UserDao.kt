package me.linhvo.ittakestwo.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import me.linhvo.ittakestwo.database.model.User

@Dao
interface UserDao {
    @Query("select * from user")
    suspend fun getUsers(): List<User>

    @Query("select * from user where id = :userId")
    suspend fun loadUser(userId: String): User

    @Query("select * from user where id = :userId")
    fun observeUser(userId: String): Flow<User?>

    @Upsert
    suspend fun upsert(user: User)
}