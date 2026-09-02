package com.example.data.remote

import android.util.Log
import com.example.data.local.CategoryEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ListingEntity
import com.example.data.local.LocationEntity
import com.example.data.local.RechargeRequestEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object FirebaseConfig {
    const val API_KEY = "AIzaSyCltZ4OFzjwmd7qbCujYB8XAZLsRj59VqQ"
    const val AUTH_DOMAIN = "localbazar-cff07.firebaseapp.com"
    const val DATABASE_URL = "https://localbazar-cff07-default-rtdb.firebaseio.com"
    const val PROJECT_ID = "localbazar-cff07"
    const val STORAGE_BUCKET = "localbazar-cff07.firebasestorage.app"
    const val MESSAGING_SENDER_ID = "742758093547"
    const val APP_ID = "1:742758093547:web:24fdd12670d75ebe67a4ae"
    const val MEASUREMENT_ID = "G-54VCL74ZT9"
}

class FirebaseService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    // -------------------------------------------------------------
    // LISTINGS SYNC
    // -------------------------------------------------------------
    suspend fun fetchAllListings(): List<ListingEntity> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${FirebaseConfig.DATABASE_URL}/listings.json")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: return@withContext emptyList()

            if (bodyString == "null" || bodyString.isBlank()) return@withContext emptyList()

            val listings = mutableListOf<ListingEntity>()
            val jsonObject = JSONObject(bodyString)
            val keys = jsonObject.keys()

            while (keys.hasNext()) {
                val key = keys.next()
                val obj = jsonObject.optJSONObject(key) ?: continue
                listings.add(
                    ListingEntity(
                        id = obj.optString("id", key),
                        title = obj.optString("title", ""),
                        categoryId = obj.optString("categoryId", "cat_others"),
                        categoryName = obj.optString("categoryName", "Others"),
                        locationId = obj.optString("locationId", "loc_delhi"),
                        locationName = obj.optString("locationName", "Delhi"),
                        stateName = obj.optString("stateName", "Delhi"),
                        price = obj.optDouble("price", 0.0),
                        isNegotiable = obj.optBoolean("isNegotiable", true),
                        condition = obj.optString("condition", "Good"),
                        description = obj.optString("description", ""),
                        phone = obj.optString("phone", ""),
                        whatsapp = obj.optString("whatsapp", ""),
                        imagesJson = obj.optString("imagesJson", ""),
                        status = obj.optString("status", "active"),
                        isFeatured = obj.optBoolean("isFeatured", false),
                        isPro = obj.optBoolean("isPro", false),
                        sellerId = obj.optString("sellerId", "user_default"),
                        sellerName = obj.optString("sellerName", "Local Seller"),
                        sellerVerified = obj.optBoolean("sellerVerified", true),
                        sellerPhone = obj.optString("sellerPhone", ""),
                        sellerJoined = obj.optString("sellerJoined", "2024"),
                        viewsCount = obj.optInt("viewsCount", 1),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
            listings
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error fetching listings: ${e.message}")
            emptyList()
        }
    }

    suspend fun pushListing(listing: ListingEntity): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("id", listing.id)
                put("title", listing.title)
                put("categoryId", listing.categoryId)
                put("categoryName", listing.categoryName)
                put("locationId", listing.locationId)
                put("locationName", listing.locationName)
                put("stateName", listing.stateName)
                put("price", listing.price)
                put("isNegotiable", listing.isNegotiable)
                put("condition", listing.condition)
                put("description", listing.description)
                put("phone", listing.phone)
                put("whatsapp", listing.whatsapp)
                put("imagesJson", listing.imagesJson)
                put("status", listing.status)
                put("isFeatured", listing.isFeatured)
                put("isPro", listing.isPro)
                put("sellerId", listing.sellerId)
                put("sellerName", listing.sellerName)
                put("sellerVerified", listing.sellerVerified)
                put("sellerPhone", listing.sellerPhone)
                put("sellerJoined", listing.sellerJoined)
                put("viewsCount", listing.viewsCount)
                put("createdAt", listing.createdAt)
            }

            val request = Request.Builder()
                .url("${FirebaseConfig.DATABASE_URL}/listings/${listing.id}.json")
                .put(json.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error pushing listing: ${e.message}")
            false
        }
    }

    suspend fun deleteListing(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${FirebaseConfig.DATABASE_URL}/listings/$id.json")
                .delete()
                .build()
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error deleting listing: ${e.message}")
            false
        }
    }

    suspend fun updateListingStatus(id: String, status: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("status", status)
            }
            val request = Request.Builder()
                .url("${FirebaseConfig.DATABASE_URL}/listings/$id.json")
                .patch(json.toString().toRequestBody(jsonMediaType))
                .build()
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error updating listing status: ${e.message}")
            false
        }
    }

    // -------------------------------------------------------------
    // CHAT MESSAGES SYNC
    // -------------------------------------------------------------
    suspend fun pushChatMessage(message: ChatMessageEntity): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("id", message.id)
                put("chatId", message.chatId)
                put("listingId", message.listingId)
                put("listingTitle", message.listingTitle)
                put("listingPrice", message.listingPrice)
                put("listingImage", message.listingImage)
                put("senderName", message.senderName)
                put("message", message.message)
                put("timestamp", message.timestamp)
                put("isFromMe", message.isFromMe)
            }

            val request = Request.Builder()
                .url("${FirebaseConfig.DATABASE_URL}/chats/${message.chatId}/${message.id}.json")
                .put(json.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error sending chat to Firebase: ${e.message}")
            false
        }
    }

    suspend fun fetchChatMessages(chatId: String): List<ChatMessageEntity> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${FirebaseConfig.DATABASE_URL}/chats/$chatId.json")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: return@withContext emptyList()
            if (bodyString == "null" || bodyString.isBlank()) return@withContext emptyList()

            val list = mutableListOf<ChatMessageEntity>()
            val jsonObject = JSONObject(bodyString)
            val keys = jsonObject.keys()

            while (keys.hasNext()) {
                val key = keys.next()
                val obj = jsonObject.optJSONObject(key) ?: continue
                list.add(
                    ChatMessageEntity(
                        id = obj.optLong("id", System.currentTimeMillis()),
                        chatId = obj.optString("chatId", chatId),
                        listingId = obj.optString("listingId", ""),
                        listingTitle = obj.optString("listingTitle", ""),
                        listingPrice = obj.optDouble("listingPrice", 0.0),
                        listingImage = obj.optString("listingImage", ""),
                        senderName = obj.optString("senderName", "User"),
                        message = obj.optString("message", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        isFromMe = obj.optBoolean("isFromMe", false)
                    )
                )
            }
            list.sortedBy { it.timestamp }
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error fetching chats: ${e.message}")
            emptyList()
        }
    }

    // -------------------------------------------------------------
    // RECHARGE & PRO REQUESTS
    // -------------------------------------------------------------
    suspend fun pushRechargeRequest(req: RechargeRequestEntity): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("id", req.id)
                put("planName", req.planName)
                put("amount", req.amount)
                put("utrNumber", req.utrNumber)
                put("userName", req.userName)
                put("userEmail", req.userEmail)
                put("status", req.status)
                put("createdAt", req.createdAt)
            }

            val request = Request.Builder()
                .url("${FirebaseConfig.DATABASE_URL}/recharge_requests/${req.id}.json")
                .put(json.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error pushing recharge: ${e.message}")
            false
        }
    }

    suspend fun fetchRechargeRequests(): List<RechargeRequestEntity> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${FirebaseConfig.DATABASE_URL}/recharge_requests.json")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: return@withContext emptyList()
            if (bodyString == "null" || bodyString.isBlank()) return@withContext emptyList()

            val list = mutableListOf<RechargeRequestEntity>()
            val jsonObject = JSONObject(bodyString)
            val keys = jsonObject.keys()

            while (keys.hasNext()) {
                val key = keys.next()
                val obj = jsonObject.optJSONObject(key) ?: continue
                list.add(
                    RechargeRequestEntity(
                        id = obj.optString("id", key),
                        planName = obj.optString("planName", "PRO Plan"),
                        amount = obj.optDouble("amount", 50.0),
                        utrNumber = obj.optString("utrNumber", ""),
                        userName = obj.optString("userName", "User"),
                        userEmail = obj.optString("userEmail", ""),
                        status = obj.optString("status", "Pending"),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
            list.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error fetching recharges: ${e.message}")
            emptyList()
        }
    }

    suspend fun updateRechargeStatus(id: String, status: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("status", status)
            }
            val request = Request.Builder()
                .url("${FirebaseConfig.DATABASE_URL}/recharge_requests/$id.json")
                .patch(json.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error updating recharge status: ${e.message}")
            false
        }
    }

    // -------------------------------------------------------------
    // CATEGORIES SYNC
    // -------------------------------------------------------------
    suspend fun pushCategory(category: CategoryEntity): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("id", category.id)
                put("name", category.name)
                put("iconName", category.iconName)
                put("sortOrder", category.sortOrder)
                put("isActive", category.isActive)
            }
            val request = Request.Builder()
                .url("${FirebaseConfig.DATABASE_URL}/categories/${category.id}.json")
                .put(json.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error pushing category: ${e.message}")
            false
        }
    }

    suspend fun fetchCategories(): List<CategoryEntity> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${FirebaseConfig.DATABASE_URL}/categories.json")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: return@withContext emptyList()
            if (bodyString == "null" || bodyString.isBlank()) return@withContext emptyList()

            val list = mutableListOf<CategoryEntity>()
            val jsonObject = JSONObject(bodyString)
            val keys = jsonObject.keys()

            while (keys.hasNext()) {
                val key = keys.next()
                val obj = jsonObject.optJSONObject(key) ?: continue
                list.add(
                    CategoryEntity(
                        id = obj.optString("id", key),
                        name = obj.optString("name", "Category"),
                        iconName = obj.optString("iconName", "Tag"),
                        sortOrder = obj.optInt("sortOrder", 99),
                        isActive = obj.optBoolean("isActive", true)
                    )
                )
            }
            list.sortedBy { it.sortOrder }
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error fetching categories: ${e.message}")
            emptyList()
        }
    }

    // -------------------------------------------------------------
    // HEALTH / CONNECTIVITY TEST
    // -------------------------------------------------------------
    suspend fun testConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${FirebaseConfig.DATABASE_URL}/.json?shallow=true")
                .get()
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}
