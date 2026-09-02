package com.example.data.remote

import android.util.Log
import com.example.data.local.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object FirebaseConfig {
    const val DATABASE_URL = "https://haaatxxndggdfwizgmlo.supabase.co"
    const val RTDB_URL = "https://localbazar-cff07-default-rtdb.firebaseio.com"
}

class FirebaseService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    // -------------------------------------------------------------
    // LISTINGS REMOTE SYNC & MODERATION
    // -------------------------------------------------------------
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
                .url("${FirebaseConfig.RTDB_URL}/listings/${listing.id}.json")
                .put(json.toString().toRequestBody(jsonMediaType))
                .build()
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error pushing listing: ${e.message}")
            false
        }
    }

    suspend fun fetchListings(): List<ListingEntity> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${FirebaseConfig.RTDB_URL}/listings.json")
                .get()
                .build()
            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: return@withContext emptyList()
            if (bodyString == "null" || bodyString.isBlank()) return@withContext emptyList()

            val list = mutableListOf<ListingEntity>()
            val jsonObject = JSONObject(bodyString)
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val obj = jsonObject.optJSONObject(key) ?: continue
                list.add(
                    ListingEntity(
                        id = obj.optString("id", key),
                        title = obj.optString("title", "Marketplace Item"),
                        categoryId = obj.optString("categoryId", "cat_other"),
                        categoryName = obj.optString("categoryName", "General"),
                        locationId = obj.optString("locationId", "loc_all"),
                        locationName = obj.optString("locationName", "India"),
                        stateName = obj.optString("stateName", "India"),
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
                        sellerName = obj.optString("sellerName", "Seller"),
                        sellerVerified = obj.optBoolean("sellerVerified", true),
                        sellerPhone = obj.optString("sellerPhone", ""),
                        sellerJoined = obj.optString("sellerJoined", "2024"),
                        viewsCount = obj.optInt("viewsCount", 10),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
            list.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error fetching listings: ${e.message}")
            emptyList()
        }
    }

    suspend fun updateListingModerationStatus(id: String, status: String, isFeatured: Boolean? = null, isPro: Boolean? = null): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("status", status)
                isFeatured?.let { put("isFeatured", it) }
                isPro?.let { put("isPro", it) }
            }
            val request = Request.Builder()
                .url("${FirebaseConfig.RTDB_URL}/listings/$id.json")
                .patch(json.toString().toRequestBody(jsonMediaType))
                .build()
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error updating listing moderation: ${e.message}")
            false
        }
    }

    suspend fun deleteListing(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${FirebaseConfig.RTDB_URL}/listings/$id.json")
                .delete()
                .build()
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error deleting listing: ${e.message}")
            false
        }
    }

    // -------------------------------------------------------------
    // RECHARGE & PRO REQUESTS (TOP PRO & MONTHLY)
    // -------------------------------------------------------------
    suspend fun pushRechargeRequest(req: RechargeRequestEntity): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("id", req.id)
                put("planId", req.planId)
                put("planName", req.planName)
                put("planDurationDays", req.planDurationDays)
                put("amount", req.amount)
                put("utrNumber", req.utrNumber)
                put("userName", req.userName)
                put("userEmail", req.userEmail)
                put("userPhone", req.userPhone)
                put("status", req.status)
                put("isTopPro", req.isTopPro)
                put("listingId", req.listingId)
                put("listingTitle", req.listingTitle)
                put("paymentProofUrl", req.paymentProofUrl)
                put("rejectionReason", req.rejectionReason)
                put("rechargeDate", req.rechargeDate)
                put("expiryDate", req.expiryDate)
                put("reviewedAt", req.reviewedAt)
                put("createdAt", req.createdAt)
            }
            val request = Request.Builder()
                .url("${FirebaseConfig.RTDB_URL}/recharge_requests/${req.id}.json")
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
                .url("${FirebaseConfig.RTDB_URL}/recharge_requests.json")
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
                        planId = obj.optString("planId", if (obj.optBoolean("isTopPro", false)) "plan_single_top_pro" else "plan_1m"),
                        planName = obj.optString("planName", if (obj.optBoolean("isTopPro", false)) "⭐ Top PRO Boost" else "1 Month PRO"),
                        planDurationDays = obj.optInt("planDurationDays", if (obj.optBoolean("isTopPro", false)) 3 else 30),
                        amount = obj.optDouble("amount", 50.0),
                        utrNumber = obj.optString("utrNumber", ""),
                        userName = obj.optString("userName", "User"),
                        userEmail = obj.optString("userEmail", ""),
                        userPhone = obj.optString("userPhone", ""),
                        status = obj.optString("status", "Pending"),
                        isTopPro = obj.optBoolean("isTopPro", false),
                        listingId = obj.optString("listingId", ""),
                        listingTitle = obj.optString("listingTitle", ""),
                        paymentProofUrl = obj.optString("paymentProofUrl", ""),
                        rejectionReason = obj.optString("rejectionReason", ""),
                        rechargeDate = obj.optLong("rechargeDate", obj.optLong("createdAt", System.currentTimeMillis())),
                        expiryDate = obj.optLong("expiryDate", 0L),
                        reviewedAt = obj.optLong("reviewedAt", 0L),
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

    suspend fun approveRecharge(id: String, rechargeDate: Long, expiryDate: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("status", "Approved")
                put("rechargeDate", rechargeDate)
                put("expiryDate", expiryDate)
                put("reviewedAt", System.currentTimeMillis())
            }
            val request = Request.Builder()
                .url("${FirebaseConfig.RTDB_URL}/recharge_requests/$id.json")
                .patch(json.toString().toRequestBody(jsonMediaType))
                .build()
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error approving recharge: ${e.message}")
            false
        }
    }

    suspend fun rejectRecharge(id: String, reason: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("status", "Rejected")
                put("rejectionReason", reason)
                put("reviewedAt", System.currentTimeMillis())
            }
            val request = Request.Builder()
                .url("${FirebaseConfig.RTDB_URL}/recharge_requests/$id.json")
                .patch(json.toString().toRequestBody(jsonMediaType))
                .build()
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error rejecting recharge: ${e.message}")
            false
        }
    }

    // -------------------------------------------------------------
    // USERS MANAGEMENT
    // -------------------------------------------------------------
    suspend fun fetchUsers(): List<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${FirebaseConfig.RTDB_URL}/users.json")
                .get()
                .build()
            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: return@withContext emptyList()
            if (bodyString == "null" || bodyString.isBlank()) return@withContext emptyList()

            val list = mutableListOf<UserEntity>()
            val jsonObject = JSONObject(bodyString)
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val obj = jsonObject.optJSONObject(key) ?: continue
                list.add(
                    UserEntity(
                        id = obj.optString("id", key),
                        name = obj.optString("name", "User"),
                        email = obj.optString("email", ""),
                        phone = obj.optString("phone", ""),
                        whatsapp = obj.optString("whatsapp", ""),
                        city = obj.optString("city", ""),
                        role = obj.optString("role", "user"),
                        accountStatus = obj.optString("accountStatus", "active"),
                        isPro = obj.optBoolean("isPro", false),
                        proExpiresAt = obj.optLong("proExpiresAt", 0L),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
            list.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error fetching users: ${e.message}")
            emptyList()
        }
    }

    suspend fun pushUser(user: UserEntity): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("id", user.id)
                put("name", user.name)
                put("email", user.email)
                put("phone", user.phone)
                put("whatsapp", user.whatsapp)
                put("city", user.city)
                put("role", user.role)
                put("accountStatus", user.accountStatus)
                put("isPro", user.isPro)
                put("proExpiresAt", user.proExpiresAt)
                put("createdAt", user.createdAt)
            }
            val request = Request.Builder()
                .url("${FirebaseConfig.RTDB_URL}/users/${user.id}.json")
                .put(json.toString().toRequestBody(jsonMediaType))
                .build()
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error pushing user: ${e.message}")
            false
        }
    }

    suspend fun updateUserRole(id: String, role: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply { put("role", role) }
            val request = Request.Builder()
                .url("${FirebaseConfig.RTDB_URL}/users/$id.json")
                .patch(json.toString().toRequestBody(jsonMediaType))
                .build()
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateUserStatus(id: String, status: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply { put("accountStatus", status) }
            val request = Request.Builder()
                .url("${FirebaseConfig.RTDB_URL}/users/$id.json")
                .patch(json.toString().toRequestBody(jsonMediaType))
                .build()
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateUserProStatus(id: String, isPro: Boolean, expiresAt: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("isPro", isPro)
                put("proExpiresAt", expiresAt)
            }
            val request = Request.Builder()
                .url("${FirebaseConfig.RTDB_URL}/users/$id.json")
                .patch(json.toString().toRequestBody(jsonMediaType))
                .build()
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    // -------------------------------------------------------------
    // SETTINGS REMOTE SYNC (QR, UPI, ADMOB, TUTORIAL)
    // -------------------------------------------------------------
    suspend fun fetchSettings(): Map<String, String> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${FirebaseConfig.RTDB_URL}/settings.json")
                .get()
                .build()
            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: return@withContext emptyMap()
            if (bodyString == "null" || bodyString.isBlank()) return@withContext emptyMap()

            val map = mutableMapOf<String, String>()
            val jsonObject = JSONObject(bodyString)
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = jsonObject.optString(key, "")
            }
            map
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error fetching settings: ${e.message}")
            emptyMap()
        }
    }

    suspend fun saveSetting(key: String, value: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply { put(key, value) }
            val request = Request.Builder()
                .url("${FirebaseConfig.RTDB_URL}/settings.json")
                .patch(json.toString().toRequestBody(jsonMediaType))
                .build()
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error saving setting: ${e.message}")
            false
        }
    }

    // -------------------------------------------------------------
    // CATEGORIES & LOCATIONS CRUD
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
                .url("${FirebaseConfig.RTDB_URL}/categories/${category.id}.json")
                .put(json.toString().toRequestBody(jsonMediaType))
                .build()
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteCategory(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${FirebaseConfig.RTDB_URL}/categories/$id.json")
                .delete()
                .build()
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun fetchCategories(): List<CategoryEntity> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${FirebaseConfig.RTDB_URL}/categories.json")
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
            emptyList()
        }
    }

    suspend fun pushLocation(location: LocationEntity): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("id", location.id)
                put("name", location.name)
                put("state", location.state)
                put("level", location.level)
                put("sortOrder", location.sortOrder)
                put("isActive", location.isActive)
            }
            val request = Request.Builder()
                .url("${FirebaseConfig.RTDB_URL}/locations/${location.id}.json")
                .put(json.toString().toRequestBody(jsonMediaType))
                .build()
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteLocation(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${FirebaseConfig.RTDB_URL}/locations/$id.json")
                .delete()
                .build()
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun fetchLocations(): List<LocationEntity> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${FirebaseConfig.RTDB_URL}/locations.json")
                .get()
                .build()
            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: return@withContext emptyList()
            if (bodyString == "null" || bodyString.isBlank()) return@withContext emptyList()

            val list = mutableListOf<LocationEntity>()
            val jsonObject = JSONObject(bodyString)
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val obj = jsonObject.optJSONObject(key) ?: continue
                list.add(
                    LocationEntity(
                        id = obj.optString("id", key),
                        name = obj.optString("name", "City"),
                        state = obj.optString("state", "India"),
                        level = obj.optInt("level", 1),
                        sortOrder = obj.optInt("sortOrder", 99),
                        isActive = obj.optBoolean("isActive", true)
                    )
                )
            }
            list.sortedBy { it.sortOrder }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // -------------------------------------------------------------
    // CHATS REMOTE SYNC
    // -------------------------------------------------------------
    suspend fun pushChatMessage(msg: ChatMessageEntity): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("id", msg.id)
                put("chatId", msg.chatId)
                put("listingId", msg.listingId)
                put("listingTitle", msg.listingTitle)
                put("listingPrice", msg.listingPrice)
                put("listingImage", msg.listingImage)
                put("senderName", msg.senderName)
                put("message", msg.message)
                put("timestamp", msg.timestamp)
                put("isFromMe", msg.isFromMe)
            }
            val request = Request.Builder()
                .url("${FirebaseConfig.RTDB_URL}/chats/${msg.chatId}/${msg.id}.json")
                .put(json.toString().toRequestBody(jsonMediaType))
                .build()
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun fetchChatMessages(chatId: String): List<ChatMessageEntity> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${FirebaseConfig.RTDB_URL}/chats/$chatId.json")
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
            emptyList()
        }
    }

    // -------------------------------------------------------------
    // CONNECTIVITY TEST
    // -------------------------------------------------------------
    suspend fun testConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${FirebaseConfig.RTDB_URL}/.json?shallow=true")
                .get()
                .build()
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}
