package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ListingDao {
    @Query("SELECT * FROM listings WHERE status != 'deleted' ORDER BY isFeatured DESC, createdAt DESC")
    fun getAllListings(): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE id = :id LIMIT 1")
    suspend fun getListingById(id: String): ListingEntity?

    @Query("SELECT * FROM listings WHERE sellerId = :sellerId AND status != 'deleted' ORDER BY createdAt DESC")
    fun getMyListings(sellerId: String): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE id IN (SELECT listingId FROM favorites) AND status != 'deleted'")
    fun getFavoriteListings(): Flow<List<ListingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: ListingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListings(listings: List<ListingEntity>)

    @Update
    suspend fun updateListing(listing: ListingEntity)

    @Query("UPDATE listings SET status = :status WHERE id = :id")
    suspend fun updateListingStatus(id: String, status: String)

    @Query("UPDATE listings SET viewsCount = viewsCount + 1 WHERE id = :id")
    suspend fun incrementViews(id: String)

    @Query("DELETE FROM listings WHERE id = :id")
    suspend fun deleteListing(id: String)

    @Query("SELECT COUNT(*) FROM listings")
    suspend fun getListingsCount(): Int
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE isActive = 1 ORDER BY sortOrder ASC")
    fun getCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategory(id: String)
}

@Dao
interface LocationDao {
    @Query("SELECT * FROM locations WHERE isActive = 1 ORDER BY sortOrder ASC, name ASC")
    fun getLocations(): Flow<List<LocationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locations: List<LocationEntity>)

    @Query("SELECT COUNT(*) FROM locations")
    suspend fun getLocationsCount(): Int
}

@Dao
interface FavoriteDao {
    @Query("SELECT listingId FROM favorites")
    fun getAllFavoriteIds(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE listingId = :listingId)")
    fun isFavorite(listingId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE listingId = :listingId")
    suspend fun removeFavorite(listingId: String)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT DISTINCT chatId FROM chat_messages")
    fun getActiveChatIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)
}

@Dao
interface RechargeDao {
    @Query("SELECT * FROM recharge_requests ORDER BY createdAt DESC")
    fun getAllRechargeRequests(): Flow<List<RechargeRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRechargeRequest(request: RechargeRequestEntity)

    @Query("UPDATE recharge_requests SET status = :status WHERE id = :id")
    suspend fun updateRechargeStatus(id: String, status: String)
}
