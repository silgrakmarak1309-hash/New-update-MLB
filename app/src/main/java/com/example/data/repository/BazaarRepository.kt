package com.example.data.repository

import android.util.Log
import com.example.data.local.BazaarDatabase
import com.example.data.local.CategoryEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.FavoriteEntity
import com.example.data.local.IndiaLocations
import com.example.data.local.ListingEntity
import com.example.data.local.LocationEntity
import com.example.data.local.RechargeRequestEntity
import com.example.data.remote.FirebaseService
import kotlinx.coroutines.flow.Flow
import java.util.UUID

data class BannerItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val tag: String,
    val imageUrl: String,
    val bgGradientStart: Long,
    val bgGradientEnd: Long
)

class BazaarRepository(private val database: BazaarDatabase) {
    private val listingDao = database.listingDao()
    private val categoryDao = database.categoryDao()
    private val locationDao = database.locationDao()
    private val favoriteDao = database.favoriteDao()
    private val chatDao = database.chatDao()
    private val rechargeDao = database.rechargeDao()

    val firebaseService = FirebaseService()

    val allListings: Flow<List<ListingEntity>> = listingDao.getAllListings()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getCategories()
    val allLocations: Flow<List<LocationEntity>> = locationDao.getLocations()
    val favoriteListings: Flow<List<ListingEntity>> = listingDao.getFavoriteListings()
    val favoriteIds: Flow<List<String>> = favoriteDao.getAllFavoriteIds()
    val allMessages: Flow<List<ChatMessageEntity>> = chatDao.getAllMessages()
    val rechargeRequests: Flow<List<RechargeRequestEntity>> = rechargeDao.getAllRechargeRequests()

    fun getMyListings(sellerId: String = "user_default"): Flow<List<ListingEntity>> =
        listingDao.getMyListings(sellerId)

    fun getChatMessages(chatId: String): Flow<List<ChatMessageEntity>> =
        chatDao.getMessagesForChat(chatId)

    suspend fun getListingById(id: String): ListingEntity? =
        listingDao.getListingById(id)

    suspend fun toggleFavorite(listingId: String, isCurrentlyFavorite: Boolean) {
        if (isCurrentlyFavorite) {
            favoriteDao.removeFavorite(listingId)
        } else {
            favoriteDao.addFavorite(FavoriteEntity(listingId = listingId))
        }
    }

    suspend fun insertListing(listing: ListingEntity) {
        listingDao.insertListing(listing)
        try {
            firebaseService.pushListing(listing)
        } catch (e: Exception) {
            Log.e("BazaarRepository", "Firebase push error: ${e.message}")
        }
    }

    suspend fun updateListing(listing: ListingEntity) {
        listingDao.updateListing(listing)
        try {
            firebaseService.pushListing(listing)
        } catch (e: Exception) {
            Log.e("BazaarRepository", "Firebase update error: ${e.message}")
        }
    }

    suspend fun updateListingStatus(id: String, status: String) {
        listingDao.updateListingStatus(id, status)
        try {
            firebaseService.updateListingStatus(id, status)
        } catch (e: Exception) {
            Log.e("BazaarRepository", "Firebase update status error: ${e.message}")
        }
    }

    suspend fun deleteListing(id: String) {
        listingDao.deleteListing(id)
        try {
            firebaseService.deleteListing(id)
        } catch (e: Exception) {
            Log.e("BazaarRepository", "Firebase delete error: ${e.message}")
        }
    }

    suspend fun incrementViews(id: String) {
        listingDao.incrementViews(id)
    }

    suspend fun sendChatMessage(
        chatId: String,
        listingId: String,
        listingTitle: String,
        listingPrice: Double,
        listingImage: String,
        senderName: String,
        message: String,
        isFromMe: Boolean = true
    ) {
        val chatMessage = ChatMessageEntity(
            chatId = chatId,
            listingId = listingId,
            listingTitle = listingTitle,
            listingPrice = listingPrice,
            listingImage = listingImage,
            senderName = senderName,
            message = message,
            timestamp = System.currentTimeMillis(),
            isFromMe = isFromMe
        )
        chatDao.insertMessage(chatMessage)
        try {
            firebaseService.pushChatMessage(chatMessage)
        } catch (e: Exception) {
            Log.e("BazaarRepository", "Firebase chat push error: ${e.message}")
        }
    }

    suspend fun submitRecharge(
        planName: String,
        amount: Double,
        utr: String,
        userName: String = "User",
        userEmail: String = ""
    ) {
        val rechargeRequest = RechargeRequestEntity(
            id = UUID.randomUUID().toString(),
            planName = planName,
            amount = amount,
            utrNumber = utr,
            userName = userName,
            userEmail = userEmail,
            status = "Pending",
            createdAt = System.currentTimeMillis()
        )
        rechargeDao.insertRechargeRequest(rechargeRequest)
        try {
            firebaseService.pushRechargeRequest(rechargeRequest)
        } catch (e: Exception) {
            Log.e("BazaarRepository", "Firebase recharge push error: ${e.message}")
        }
    }

    suspend fun updateRechargeStatus(id: String, status: String) {
        rechargeDao.updateRechargeStatus(id, status)
        try {
            firebaseService.updateRechargeStatus(id, status)
        } catch (e: Exception) {
            Log.e("BazaarRepository", "Firebase recharge update error: ${e.message}")
        }
    }

    suspend fun addCategory(name: String, icon: String) {
        val category = CategoryEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            iconName = icon,
            sortOrder = 99,
            isActive = true
        )
        categoryDao.insertCategory(category)
        try {
            firebaseService.pushCategory(category)
        } catch (e: Exception) {
            Log.e("BazaarRepository", "Firebase category push error: ${e.message}")
        }
    }

    suspend fun deleteCategory(id: String) {
        categoryDao.deleteCategory(id)
    }

    suspend fun syncWithFirebase(): Boolean {
        return try {
            val remoteListings = firebaseService.fetchAllListings()
            if (remoteListings.isNotEmpty()) {
                listingDao.insertListings(remoteListings)
            }

            val remoteCategories = firebaseService.fetchCategories()
            if (remoteCategories.isNotEmpty()) {
                categoryDao.insertCategories(remoteCategories)
            }

            val remoteRecharges = firebaseService.fetchRechargeRequests()
            if (remoteRecharges.isNotEmpty()) {
                for (r in remoteRecharges) {
                    rechargeDao.insertRechargeRequest(r)
                }
            }
            true
        } catch (e: Exception) {
            Log.e("BazaarRepository", "Firebase sync failed: ${e.message}")
            false
        }
    }

    suspend fun uploadAllToFirebase() {
        try {
            val count = listingDao.getListingsCount()
            if (count > 0) {
                // Upload initial listings to Firebase so database is populated
                val remote = firebaseService.fetchAllListings()
                if (remote.isEmpty()) {
                    // Firebase is fresh, let's sync default initial listings up to Firebase
                    val sampleListings = defaultListingsList()
                    for (l in sampleListings) {
                        firebaseService.pushListing(l)
                    }
                    val sampleCategories = defaultCategoriesList()
                    for (c in sampleCategories) {
                        firebaseService.pushCategory(c)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("BazaarRepository", "Error uploading to Firebase: ${e.message}")
        }
    }

    fun getBanners(): List<BannerItem> {
        return listOf(
            BannerItem(
                id = "b1",
                title = "Mega Local Bazaar Dhamaka",
                subtitle = "Buy & Sell directly in your neighborhood with zero commission!",
                tag = "Zero Brokerage",
                imageUrl = "https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?w=800&auto=format&fit=crop&q=80",
                bgGradientStart = 0xFFF97316,
                bgGradientEnd = 0xFFEA580C
            ),
            BannerItem(
                id = "b2",
                title = "Verified Local Sellers",
                subtitle = "100% genuine products, instant WhatsApp & Call connect",
                tag = "Safe Trading",
                imageUrl = "https://images.unsplash.com/photo-1556742049-0a67e557224f?w=800&auto=format&fit=crop&q=80",
                bgGradientStart = 0xFF0D9488,
                bgGradientEnd = 0xFF047857
            ),
            BannerItem(
                id = "b3",
                title = "Get PRO Membership",
                subtitle = "Get verified PRO badge, 10x views & pin your ads on top for just ₹50",
                tag = "PRO Plan",
                imageUrl = "https://images.unsplash.com/photo-1526304640581-d334cdbbf45e?w=800&auto=format&fit=crop&q=80",
                bgGradientStart = 0xFFD97706,
                bgGradientEnd = 0xFFB45309
            )
        )
    }

    fun defaultCategoriesList(): List<CategoryEntity> {
        return listOf(
            CategoryEntity("cat_mobile", "Mobile Phones", "Smartphone", 1),
            CategoryEntity("cat_electronics", "Electronics", "Laptop", 2),
            CategoryEntity("cat_vehicles", "Vehicles", "Car", 3),
            CategoryEntity("cat_property", "Property / House", "Home", 4),
            CategoryEntity("cat_jobs", "Jobs", "Briefcase", 5),
            CategoryEntity("cat_fashion", "Fashion", "Shirt", 6),
            CategoryEntity("cat_services", "Services", "Wrench", 7),
            CategoryEntity("cat_furniture", "Furniture", "Sofa", 8),
            CategoryEntity("cat_appliances", "Home Appliances", "Refrigerator", 9),
            CategoryEntity("cat_agriculture", "Agriculture", "Wheat", 10),
            CategoryEntity("cat_others", "Others", "Tag", 11)
        )
    }

    fun defaultLocationsList(): List<LocationEntity> {
        return IndiaLocations.list
    }

    fun defaultListingsList(): List<ListingEntity> {
        return listOf(
            ListingEntity(
                id = "list_1",
                title = "iPhone 13 128GB Midnight - 90% Battery Health",
                categoryId = "cat_mobile",
                categoryName = "Mobile Phones",
                locationId = "loc_delhi",
                locationName = "Connaught Place, Delhi",
                stateName = "Delhi",
                price = 38500.0,
                isNegotiable = true,
                condition = "Like New",
                description = "Apple iPhone 13 128GB in immaculate condition. No scratches or dents. Includes original box, Apple 20W charger, and 2 premium cases. Bill available. Only used for 8 months. Serious buyers please.",
                phone = "9876543210",
                whatsapp = "9876543210",
                imagesJson = "https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=800&auto=format&fit=crop&q=80",
                status = "active",
                isFeatured = true,
                isPro = true,
                sellerName = "Amit Sharma",
                sellerVerified = true,
                sellerPhone = "9876543210",
                sellerJoined = "Feb 2023",
                viewsCount = 342,
                createdAt = System.currentTimeMillis() - 3600000 * 4
            ),
            ListingEntity(
                id = "list_2",
                title = "Royal Enfield Classic 350 - Stealth Black (2022)",
                categoryId = "cat_vehicles",
                categoryName = "Vehicles",
                locationId = "loc_lucknow",
                locationName = "Hazratganj, Lucknow",
                stateName = "Uttar Pradesh",
                price = 145000.0,
                isNegotiable = true,
                condition = "Good",
                description = "2022 Model Royal Enfield Classic 350 Dual Channel ABS. Driven only 11,200 kms. Single owner, timely showroom serviced with complete service record book. New tubeless rear tyre and crash guard installed.",
                phone = "9812345678",
                whatsapp = "9812345678",
                imagesJson = "https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=800&auto=format&fit=crop&q=80",
                status = "active",
                isFeatured = true,
                isPro = true,
                sellerName = "Vikramaditya Singh",
                sellerVerified = true,
                sellerPhone = "9812345678",
                sellerJoined = "Nov 2022",
                viewsCount = 520,
                createdAt = System.currentTimeMillis() - 3600000 * 8
            ),
            ListingEntity(
                id = "list_3",
                title = "2 BHK Semi-Furnished Flat with Balcony for Rent",
                categoryId = "cat_property",
                categoryName = "Property / House",
                locationId = "loc_noida",
                locationName = "Sector 62, Noida",
                stateName = "Uttar Pradesh",
                price = 18000.0,
                isNegotiable = false,
                condition = "Brand New",
                description = "Spacious 2 BHK 1150 sq.ft apartment in a gated society with 24x7 security, power backup, covered car parking, modular kitchen, and scenic park facing balcony. 5 mins walking distance from Metro Station.",
                phone = "9988776655",
                whatsapp = "9988776655",
                imagesJson = "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=800&auto=format&fit=crop&q=80",
                status = "active",
                isFeatured = true,
                isPro = false,
                sellerName = "Rakesh Verma",
                sellerVerified = true,
                sellerPhone = "9988776655",
                sellerJoined = "Aug 2023",
                viewsCount = 415,
                createdAt = System.currentTimeMillis() - 3600000 * 12
            ),
            ListingEntity(
                id = "list_4",
                title = "Dell Inspiron Core i7 12th Gen (16GB RAM / 512GB SSD)",
                categoryId = "cat_electronics",
                categoryName = "Electronics",
                locationId = "loc_mumbai",
                locationName = "Andheri West, Mumbai",
                stateName = "Maharashtra",
                price = 44000.0,
                isNegotiable = true,
                condition = "Like New",
                description = "Lightweight metallic body laptop, 15.6 inch FHD IPS display, Intel Core i7 1255U, 16GB DDR4, 512GB NVMe SSD, backlit keyboard, Windows 11 Home + MS Office Genuine. Perfect for programming and office work.",
                phone = "9765432109",
                whatsapp = "9765432109",
                imagesJson = "https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?w=800&auto=format&fit=crop&q=80",
                status = "active",
                isFeatured = false,
                isPro = true,
                sellerName = "Pooja Mehta",
                sellerVerified = true,
                sellerPhone = "9765432109",
                sellerJoined = "May 2023",
                viewsCount = 280,
                createdAt = System.currentTimeMillis() - 3600000 * 18
            ),
            ListingEntity(
                id = "list_5",
                title = "Solid Sheesham Wood 6-Seater Dining Table Set",
                categoryId = "cat_furniture",
                categoryName = "Furniture",
                locationId = "loc_jaipur",
                locationName = "Mansarovar, Jaipur",
                stateName = "Rajasthan",
                price = 21500.0,
                isNegotiable = true,
                condition = "Good",
                description = "Handcrafted pure Sheesham wood dining table with 6 cushioned chairs. Honey finish, heavy wood quality, scratch resistant top. Selling due to house shifting. Urgent sale.",
                phone = "9829012345",
                whatsapp = "9829012345",
                imagesJson = "https://images.unsplash.com/photo-1617806118233-18e1de247200?w=800&auto=format&fit=crop&q=80",
                status = "active",
                isFeatured = false,
                isPro = false,
                sellerName = "Kailash Crafts",
                sellerVerified = true,
                sellerPhone = "9829012345",
                sellerJoined = "Jan 2023",
                viewsCount = 195,
                createdAt = System.currentTimeMillis() - 3600000 * 24
            ),
            ListingEntity(
                id = "list_6",
                title = "LG 8.0 Kg Front Load Smart Inverter Washing Machine",
                categoryId = "cat_appliances",
                categoryName = "Home Appliances",
                locationId = "loc_patna",
                locationName = "Kankarbagh, Patna",
                stateName = "Bihar",
                price = 16500.0,
                isNegotiable = true,
                condition = "Good",
                description = "LG 8.0 Kg 5-Star rated Fully Automatic Front Load Washing Machine with Steam Wash and 10-year motor warranty. Working in 100% flawless condition, no rust, clean drum.",
                phone = "9431098765",
                whatsapp = "9431098765",
                imagesJson = "https://images.unsplash.com/photo-1626806787461-102c1bfaaea1?w=800&auto=format&fit=crop&q=80",
                status = "active",
                isFeatured = false,
                isPro = false,
                sellerName = "Sanjay Kumar",
                sellerVerified = true,
                sellerPhone = "9431098765",
                sellerJoined = "Jul 2023",
                viewsCount = 160,
                createdAt = System.currentTimeMillis() - 3600000 * 30
            ),
            ListingEntity(
                id = "list_7",
                title = "Certified Electrician & AC Repair / Installation Service",
                categoryId = "cat_services",
                categoryName = "Services",
                locationId = "loc_delhi",
                locationName = "Laxmi Nagar, Delhi",
                stateName = "Delhi",
                price = 350.0,
                isNegotiable = false,
                condition = "Brand New",
                description = "Doorstep Electrician, Inverter Repair, AC Gas Refill, Deep Chemical Cleaning, and House Wiring service. 15+ years experience, verified technician, genuine parts with 30-day service warranty.",
                phone = "9811223344",
                whatsapp = "9811223344",
                imagesJson = "https://images.unsplash.com/photo-1621905251189-08b45d6a269e?w=800&auto=format&fit=crop&q=80",
                status = "active",
                isFeatured = true,
                isPro = true,
                sellerName = "Rajesh Tech Services",
                sellerVerified = true,
                sellerPhone = "9811223344",
                sellerJoined = "Mar 2022",
                viewsCount = 680,
                createdAt = System.currentTimeMillis() - 3600000 * 36
            ),
            ListingEntity(
                id = "list_8",
                title = "Swaraj 744 FE Tractor (2020 Model) with Cultivator",
                categoryId = "cat_agriculture",
                categoryName = "Agriculture",
                locationId = "loc_lucknow",
                locationName = "Mohanlalganj, Lucknow",
                stateName = "Uttar Pradesh",
                price = 420000.0,
                isNegotiable = true,
                condition = "Good",
                description = "48 HP Powerful Swaraj 744 FE 4-Stroke Diesel Tractor in excellent running condition. 85% tyre condition. All papers up to date, insurance valid. Includes 9-tine heavy cultivator.",
                phone = "9792001122",
                whatsapp = "9792001122",
                imagesJson = "https://images.unsplash.com/photo-1592878904946-b3cd8ae243d0?w=800&auto=format&fit=crop&q=80",
                status = "active",
                isFeatured = false,
                isPro = false,
                sellerName = "Chaudhary Dharamvir",
                sellerVerified = true,
                sellerPhone = "9792001122",
                sellerJoined = "Sep 2021",
                viewsCount = 310,
                createdAt = System.currentTimeMillis() - 3600000 * 42
            )
        )
    }

    suspend fun seedInitialDataIfEmpty() {
        if (locationDao.getLocationsCount() < 50) {
            locationDao.insertLocations(defaultLocationsList())
        }

        if (listingDao.getListingsCount() > 0) return

        // 1. Categories
        val defaultCategories = defaultCategoriesList()
        categoryDao.insertCategories(defaultCategories)

        // 2. Locations
        val defaultLocations = defaultLocationsList()
        locationDao.insertLocations(defaultLocations)

        // 3. Initial Sample Marketplace Listings
        val defaultListings = defaultListingsList()
        listingDao.insertListings(defaultListings)

        // 4. Initial default chat seed for demo
        chatDao.insertMessage(
            ChatMessageEntity(
                chatId = "chat_list_1",
                listingId = "list_1",
                listingTitle = "iPhone 13 128GB Midnight",
                listingPrice = 38500.0,
                listingImage = "https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=800&auto=format&fit=crop&q=80",
                senderName = "Amit Sharma",
                message = "Namaste! Yes, the iPhone is available with all accessories.",
                timestamp = System.currentTimeMillis() - 1800000,
                isFromMe = false
            )
        )
    }
}
