package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Book
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Query("SELECT * FROM books ORDER BY lastReadAt DESC")
    fun getAllBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    fun getBookById(id: Long): Flow<Book?>

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    suspend fun getBookByIdDirect(id: Long): Book?

    @Query("SELECT * FROM books WHERE title = :title OR (fileUri != '' AND fileUri = :fileUri) LIMIT 1")
    suspend fun findBookByTitleOrUri(title: String, fileUri: String): Book?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<Book>)

    @Update
    suspend fun updateBook(book: Book)

    @Query("""
        UPDATE books 
        SET currentPage = :currentPage, 
            scrollPosition = :scrollPosition, 
            zoomLevel = :zoomLevel, 
            progress = :progress, 
            lastReadAt = :lastReadAt 
        WHERE id = :bookId
    """)
    suspend fun updateReadingPosition(
        bookId: Long,
        currentPage: Int,
        scrollPosition: Float,
        zoomLevel: Float,
        progress: Float,
        lastReadAt: Long = System.currentTimeMillis()
    )

    @Delete
    suspend fun deleteBook(book: Book)

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteBookById(id: Long)
}
