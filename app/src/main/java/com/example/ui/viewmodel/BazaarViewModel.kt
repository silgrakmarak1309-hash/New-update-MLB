package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BazaarDatabase
import com.example.data.local.CategoryEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ListingEntity
import com.example.data.local.LocationEntity
import com.example.data.local.RechargeRequestEntity
import com.example.data.repository.BannerItem
import com.example.data.repository.BazaarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppScreen {
    HOME,
    SEARCH,
    POST_AD,
    CHATS,
    CHAT_DETAIL,
    MY_ADS,
    FAVORITES,
    ACCOUNT,
    PRO_MEMBERSHIP,
    ADMIN_PANEL,
    LISTING_DETAIL
}

enum class SortOption {
    NEWEST,
    PRICE_LOW_TO_HIGH,
    PRICE_HIGH_TO_LOW
}

class BazaarViewModel(application: Application) : AndroidViewModel(application) {
    private val database = BazaarDatabase.getDatabase(application)
    private val repository = BazaarRepository(database)

    // Current Navigation Screen
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Selected items for detail views
    private val _selectedListing = MutableStateFlow<ListingEntity?>(null)
    val selectedListing: StateFlow<ListingEntity?> = _selectedListing.asStateFlow()

    private val _activeChatId = MutableStateFlow<String?>(null)
    val activeChatId: StateFlow<String?> = _activeChatId.asStateFlow()

    // Search and Filter States
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<CategoryEntity?>(null)
    val selectedCategory: StateFlow<CategoryEntity?> = _selectedCategory.asStateFlow()

    private val _selectedLocation = MutableStateFlow<LocationEntity?>(null)
    val selectedLocation: StateFlow<LocationEntity?> = _selectedLocation.asStateFlow()

    private val _selectedCondition = MutableStateFlow<String>("All")
    val selectedCondition: StateFlow<String> = _selectedCondition.asStateFlow()

    private val _minPrice = MutableStateFlow<Double?>(null)
    val minPrice: StateFlow<Double?> = _minPrice.asStateFlow()

    private val _maxPrice = MutableStateFlow<Double?>(null)
    val maxPrice: StateFlow<Double?> = _maxPrice.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.NEWEST)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    // User Profile
    val userName = MutableStateFlow("Rahul Kumar")
    val userPhone = MutableStateFlow("9876543210")
    val userEmail = MutableStateFlow("rahul.kumar@example.com")
    val isProUser = MutableStateFlow(true)
    val userCity = MutableStateFlow("Delhi NCR")

    // Notification message
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Firebase Sync Status
    val isSyncing = MutableStateFlow(false)
    val isFirebaseConnected = MutableStateFlow(true)
    val lastSyncTime = MutableStateFlow<Long>(System.currentTimeMillis())

    // Data sources
    val banners: List<BannerItem> = repository.getBanners()
    val categories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val locations: StateFlow<List<LocationEntity>> = repository.allLocations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val favoriteIds: StateFlow<List<String>> = repository.favoriteIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val favoriteListings: StateFlow<List<ListingEntity>> = repository.favoriteListings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val myListings: StateFlow<List<ListingEntity>> = repository.getMyListings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allMessages: StateFlow<List<ChatMessageEntity>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val rechargeRequests: StateFlow<List<RechargeRequestEntity>> = repository.rechargeRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Listings
    val filteredListings: StateFlow<List<ListingEntity>> = combine(
        repository.allListings,
        _searchQuery,
        _selectedCategory,
        _selectedLocation,
        _selectedCondition,
        _minPrice,
        _maxPrice,
        _sortOption
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val rawListings = args[0] as List<ListingEntity>
        val query = (args[1] as String).trim().lowercase()
        val category = args[2] as? CategoryEntity
        val location = args[3] as? LocationEntity
        val condition = args[4] as String
        val minP = args[5] as? Double
        val maxP = args[6] as? Double
        val sort = args[7] as SortOption

        rawListings.filter { listing ->
            val matchesQuery = query.isEmpty() ||
                    listing.title.lowercase().contains(query) ||
                    listing.description.lowercase().contains(query) ||
                    listing.categoryName.lowercase().contains(query) ||
                    listing.locationName.lowercase().contains(query)

            val matchesCategory = category == null || listing.categoryId == category.id
            val matchesLocation = location == null || listing.locationId == location.id
            val matchesCondition = condition == "All" || listing.condition.equals(condition, ignoreCase = true)
            val matchesMinPrice = minP == null || listing.price >= minP
            val matchesMaxPrice = maxP == null || listing.price <= maxP
            val matchesStatus = listing.status == "active"

            matchesQuery && matchesCategory && matchesLocation && matchesCondition && matchesMinPrice && matchesMaxPrice && matchesStatus
        }.sortedWith { a, b ->
            when (sort) {
                SortOption.NEWEST -> {
                    // Featured first, then newest
                    if (a.isFeatured != b.isFeatured) {
                        if (a.isFeatured) -1 else 1
                    } else {
                        b.createdAt.compareTo(a.createdAt)
                    }
                }
                SortOption.PRICE_LOW_TO_HIGH -> a.price.compareTo(b.price)
                SortOption.PRICE_HIGH_TO_LOW -> b.price.compareTo(a.price)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            syncFirebaseData(silent = true)
        }
    }

    fun syncFirebaseData(silent: Boolean = false) {
        viewModelScope.launch {
            isSyncing.value = true
            try {
                // Test connection
                val isConnected = repository.firebaseService.testConnection()
                isFirebaseConnected.value = isConnected

                // Upload default data to Firebase if cloud is completely fresh
                repository.uploadAllToFirebase()

                // Sync data from Firebase
                val success = repository.syncWithFirebase()
                lastSyncTime.value = System.currentTimeMillis()
                if (!silent) {
                    if (success) {
                        showSnackbar("☁️ Synced with Firebase (localbazar-cff07)")
                    } else {
                        showSnackbar("Offline mode: using local cached database")
                    }
                }
            } catch (e: Exception) {
                isFirebaseConnected.value = false
                if (!silent) {
                    showSnackbar("Sync failed, using offline local database")
                }
            } finally {
                isSyncing.value = false
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun selectListing(listing: ListingEntity) {
        _selectedListing.value = listing
        _currentScreen.value = AppScreen.LISTING_DETAIL
        viewModelScope.launch {
            repository.incrementViews(listing.id)
        }
    }

    fun openChatForListing(listing: ListingEntity) {
        val chatId = "chat_${listing.id}"
        _activeChatId.value = chatId
        _selectedListing.value = listing
        _currentScreen.value = AppScreen.CHAT_DETAIL
    }

    fun openChatById(chatId: String) {
        _activeChatId.value = chatId
        _currentScreen.value = AppScreen.CHAT_DETAIL
    }

    fun toggleFavorite(listingId: String) {
        viewModelScope.launch {
            val isFav = favoriteIds.value.contains(listingId)
            repository.toggleFavorite(listingId, isFav)
            showSnackbar(if (isFav) "Removed from Saved Ads" else "Saved to Favorites ❤️")
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: CategoryEntity?) {
        _selectedCategory.value = if (_selectedCategory.value?.id == category?.id) null else category
    }

    fun selectLocation(location: LocationEntity?) {
        _selectedLocation.value = location
        userCity.value = location?.name ?: "All India"
    }

    fun setCondition(condition: String) {
        _selectedCondition.value = condition
    }

    fun setPriceRange(min: Double?, max: Double?) {
        _minPrice.value = min
        _maxPrice.value = max
    }

    fun setSortOption(sort: SortOption) {
        _sortOption.value = sort
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = null
        _selectedLocation.value = null
        _selectedCondition.value = "All"
        _minPrice.value = null
        _maxPrice.value = null
        _sortOption.value = SortOption.NEWEST
    }

    fun postNewAd(
        title: String,
        categoryId: String,
        categoryName: String,
        locationId: String,
        locationName: String,
        price: Double,
        isNegotiable: Boolean,
        condition: String,
        description: String,
        phone: String,
        whatsapp: String,
        imageUrl: String
    ) {
        viewModelScope.launch {
            val newListing = ListingEntity(
                id = "ad_${UUID.randomUUID()}",
                title = title.trim(),
                categoryId = categoryId,
                categoryName = categoryName,
                locationId = locationId,
                locationName = locationName,
                stateName = locationName.split(",").lastOrNull()?.trim() ?: "India",
                price = price,
                isNegotiable = isNegotiable,
                condition = condition,
                description = description.trim(),
                phone = phone.trim(),
                whatsapp = whatsapp.trim().ifEmpty { phone.trim() },
                imagesJson = imageUrl.ifEmpty { "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=800&auto=format&fit=crop&q=80" },
                status = "active",
                isFeatured = isProUser.value,
                isPro = isProUser.value,
                sellerId = "user_default",
                sellerName = userName.value,
                sellerVerified = true,
                sellerPhone = phone.trim(),
                sellerJoined = "Active Seller",
                viewsCount = 1,
                createdAt = System.currentTimeMillis()
            )
            repository.insertListing(newListing)
            showSnackbar("🎉 Ad published successfully on Meri Local Bazaar!")
            _currentScreen.value = AppScreen.MY_ADS
        }
    }

    fun updateAdStatus(id: String, status: String) {
        viewModelScope.launch {
            repository.updateListingStatus(id, status)
            showSnackbar("Ad status updated to $status")
        }
    }

    fun deleteAd(id: String) {
        viewModelScope.launch {
            repository.deleteListing(id)
            showSnackbar("Ad deleted")
        }
    }

    fun sendMessage(chatId: String, listing: ListingEntity?, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val title = listing?.title ?: "Marketplace Item"
            val price = listing?.price ?: 0.0
            val image = listing?.imagesJson ?: ""
            repository.sendChatMessage(
                chatId = chatId,
                listingId = listing?.id ?: "ad_generic",
                listingTitle = title,
                listingPrice = price,
                listingImage = image,
                senderName = userName.value,
                message = text.trim(),
                isFromMe = true
            )
        }
    }

    fun submitRecharge(planName: String, amount: Double, utr: String) {
        viewModelScope.launch {
            repository.submitRecharge(
                planName = planName,
                amount = amount,
                utr = utr,
                userName = userName.value,
                userEmail = userEmail.value
            )
            isProUser.value = true
            showSnackbar("✅ Payment request submitted! PRO features activated.")
            _currentScreen.value = AppScreen.ACCOUNT
        }
    }

    fun activateFreeMonthPro() {
        isProUser.value = true
        showSnackbar("🎉 1 Month FREE PRO Plan Activated! Enjoy priority listings.")
    }

    fun approveRecharge(id: String) {
        viewModelScope.launch {
            repository.updateRechargeStatus(id, "Approved")
            showSnackbar("Recharge request approved")
        }
    }

    fun rejectRecharge(id: String) {
        viewModelScope.launch {
            repository.updateRechargeStatus(id, "Rejected")
            showSnackbar("Recharge request rejected")
        }
    }

    fun addCategory(name: String, icon: String = "Tag") {
        viewModelScope.launch {
            repository.addCategory(name, icon)
            showSnackbar("Category '$name' added successfully")
        }
    }

    fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}
