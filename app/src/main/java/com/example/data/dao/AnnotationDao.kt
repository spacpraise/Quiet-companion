package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Annotation
import kotlinx.coroutines.flow.Flow

@Dao
interface AnnotationDao {

    @Query("SELECT * FROM annotations WHERE bookId = :bookId ORDER BY page ASC, position ASC, createdAt DESC")
    fun getAnnotationsForBook(bookId: Long): Flow<List<Annotation>>

    @Query("SELECT * FROM annotations WHERE bookId = :bookId AND page = :page ORDER BY position ASC, createdAt DESC")
    fun getAnnotationsForBookAndPage(bookId: Long, page: Int): Flow<List<Annotation>>

    @Query("SELECT * FROM annotations WHERE bookId = :bookId AND type = :type ORDER BY page ASC, createdAt DESC")
    fun getAnnotationsForBookByType(bookId: Long, type: String): Flow<List<Annotation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnotation(annotation: Annotation): Long

    @Update
    suspend fun updateAnnotation(annotation: Annotation)

    @Delete
    suspend fun deleteAnnotation(annotation: Annotation)

    @Query("DELETE FROM annotations WHERE id = :id")
    suspend fun deleteAnnotationById(id: Long)
}
